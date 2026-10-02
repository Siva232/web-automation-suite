package com.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.json.Json;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Records the browser tab of one scenario using the Chrome DevTools screencast (Chrome and Edge only),
 * through Selenium's built-in DevTools support - no extra libraries or screen capture, so it works
 * headless and with parallel execution (one recorder per thread / browser).
 * <p>
 * Uses raw CDP method names instead of Selenium's versioned classes (v151, v152...) so it keeps
 * working when the browser updates.
 */
public final class VideoRecorder {

    private static final ThreadLocal<VideoRecorder> CURRENT = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    private static final Event<Map<String, Object>> SCREENCAST_FRAME =
            new Event<>("Page.screencastFrame", input -> (Map<String, Object>) input.read(Json.MAP_TYPE));

    private record Frame(long nanoTime, byte[] jpeg) {
    }

    private final DevTools devTools;
    private final List<Frame> frames = new ArrayList<>();
    private volatile boolean stopped;
    private final ExecutorService acknowledger = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "video-frame-ack");
        thread.setDaemon(true);
        return thread;
    });

    private VideoRecorder(DevTools devTools) {
        this.devTools = devTools;
    }

    /** Starts recording the driver's current tab when video.recording=true. Never fails the test. */
    public static void start(WebDriver driver) {
        if (!ConfigReader.getBoolean("video.recording")) {
            return;
        }
        if (!(driver instanceof HasDevTools hasDevTools)) {
            System.out.println("[Video] Recording is supported on Chrome and Edge only - skipped for "
                    + ConfigReader.get("browser"));
            return;
        }
        try {
            VideoRecorder recorder = new VideoRecorder(hasDevTools.getDevTools());
            recorder.begin();
            CURRENT.set(recorder);
        } catch (RuntimeException e) {
            System.out.println("[Video] Could not start recording: " + e.getMessage());
        }
    }

    /** Stops the recorder of the current thread; returns it (for saving) or null when not recording. */
    public static VideoRecorder stop() {
        VideoRecorder recorder = CURRENT.get();
        CURRENT.remove();
        if (recorder != null) {
            recorder.end();
        }
        return recorder;
    }

    private void begin() {
        devTools.createSessionIfThereIsNotOne();
        devTools.addListener(SCREENCAST_FRAME, this::onFrame);
        devTools.send(new Command<>("Page.startScreencast", Map.of(
                "format", "jpeg",
                "quality", ConfigReader.getInt("video.quality"),
                "maxWidth", 1280,
                "maxHeight", 720,
                "everyNthFrame", 1)));
    }

    private void onFrame(Map<String, Object> event) {
        if (stopped) {
            return;
        }
        byte[] jpeg = Base64.getDecoder().decode((String) event.get("data"));
        synchronized (frames) {
            frames.add(new Frame(System.nanoTime(), jpeg));
        }
        // Chrome sends the next frame only after this one is acknowledged.
        // Acknowledge on a separate thread so the DevTools event thread is never blocked.
        Object sessionId = event.get("sessionId");
        try {
            acknowledger.execute(() -> {
                try {
                    devTools.send(new Command<>("Page.screencastFrameAck", Map.of("sessionId", sessionId)));
                } catch (RuntimeException ignored) {
                    // browser closing - nothing to acknowledge
                }
            });
        } catch (RejectedExecutionException ignored) {
            // a late frame arrived while stopping
        }
    }

    private void end() {
        stopped = true;
        devTools.clearListeners();
        try {
            devTools.send(new Command<>("Page.stopScreencast", Map.of()));
        } catch (RuntimeException ignored) {
            // browser may already be gone
        }
        acknowledger.shutdown();
        try {
            acknowledger.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes the recording as an MJPEG .avi at the given frame rate. Chrome only sends a frame when
     * the page changes, so each frame is repeated until the next one to keep real-time speed.
     * The last frame is held for one extra second so the final state (e.g. a failure) is visible.
     *
     * @return true when a video was written, false when no frames were captured
     */
    public boolean save(Path file, int fps) throws IOException {
        List<Frame> captured;
        synchronized (frames) {
            captured = List.copyOf(frames);
        }
        if (captured.isEmpty()) {
            return false;
        }
        BufferedImage first = ImageIO.read(new ByteArrayInputStream(captured.get(0).jpeg()));

        long start = captured.get(0).nanoTime();
        long end = captured.get(captured.size() - 1).nanoTime() + TimeUnit.SECONDS.toNanos(1);
        long frameNanos = TimeUnit.SECONDS.toNanos(1) / fps;

        List<byte[]> timeline = new ArrayList<>();
        int index = 0;
        for (long t = start; t <= end; t += frameNanos) {
            while (index + 1 < captured.size() && captured.get(index + 1).nanoTime() <= t) {
                index++;
            }
            timeline.add(captured.get(index).jpeg());
        }
        MjpegAviWriter.write(file, timeline, fps, first.getWidth(), first.getHeight());
        return true;
    }
}
