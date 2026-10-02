package com.example.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes JPEG frames as a Motion-JPEG .avi file (RIFF AVI 1.0 with an idx1 index), using only the JDK.
 * The result plays in VLC, Windows Media Player and most other desktop players.
 */
final class MjpegAviWriter {

    private static final int AVIF_HASINDEX = 0x10;
    private static final int AVIIF_KEYFRAME = 0x10;

    private MjpegAviWriter() {
    }

    static void write(Path file, List<byte[]> frames, int fps, int width, int height) throws IOException {
        int frameCount = frames.size();
        int maxFrameSize = frames.stream().mapToInt(f -> f.length).max().orElse(0);

        int moviSize = 4; // "movi"
        for (byte[] frame : frames) {
            moviSize += 8 + frame.length + (frame.length & 1);
        }
        int idx1Size = 16 * frameCount;
        int hdrlSize = 4 + (8 + 56) + (8 + 4 + (8 + 56) + (8 + 40)); // hdrl + avih + strl(strh, strf)
        int riffSize = 4 + (8 + hdrlSize) + (8 + moviSize) + (8 + idx1Size);

        Files.createDirectories(file.toAbsolutePath().getParent());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            fourCc(out, "RIFF");
            int32(out, riffSize);
            fourCc(out, "AVI ");

            fourCc(out, "LIST");
            int32(out, hdrlSize);
            fourCc(out, "hdrl");

            // Main AVI header
            fourCc(out, "avih");
            int32(out, 56);
            int32(out, 1_000_000 / fps);        // microseconds per frame
            int32(out, maxFrameSize * fps);     // max bytes per second
            int32(out, 0);                      // padding granularity
            int32(out, AVIF_HASINDEX);
            int32(out, frameCount);
            int32(out, 0);                      // initial frames
            int32(out, 1);                      // streams
            int32(out, maxFrameSize);           // suggested buffer size
            int32(out, width);
            int32(out, height);
            int32(out, 0);
            int32(out, 0);
            int32(out, 0);
            int32(out, 0);

            fourCc(out, "LIST");
            int32(out, 4 + (8 + 56) + (8 + 40));
            fourCc(out, "strl");

            // Stream header
            fourCc(out, "strh");
            int32(out, 56);
            fourCc(out, "vids");
            fourCc(out, "MJPG");
            int32(out, 0);                      // flags
            int16(out, 0);                      // priority
            int16(out, 0);                      // language
            int32(out, 0);                      // initial frames
            int32(out, 1);                      // scale
            int32(out, fps);                    // rate  (rate / scale = frames per second)
            int32(out, 0);                      // start
            int32(out, frameCount);             // length
            int32(out, maxFrameSize);           // suggested buffer size
            int32(out, -1);                     // quality (default)
            int32(out, 0);                      // sample size
            int16(out, 0);                      // frame rectangle: left, top, right, bottom
            int16(out, 0);
            int16(out, width);
            int16(out, height);

            // Stream format (BITMAPINFOHEADER)
            fourCc(out, "strf");
            int32(out, 40);
            int32(out, 40);
            int32(out, width);
            int32(out, height);
            int16(out, 1);                      // planes
            int16(out, 24);                     // bits per pixel
            fourCc(out, "MJPG");
            int32(out, width * height * 3);
            int32(out, 0);
            int32(out, 0);
            int32(out, 0);
            int32(out, 0);

            fourCc(out, "LIST");
            int32(out, moviSize);
            fourCc(out, "movi");
            for (byte[] frame : frames) {
                fourCc(out, "00dc");
                int32(out, frame.length);
                out.write(frame);
                if ((frame.length & 1) == 1) {
                    out.write(0);               // chunks are word aligned
                }
            }

            // Index: offsets are relative to the "movi" fourcc
            fourCc(out, "idx1");
            int32(out, idx1Size);
            int offset = 4;
            for (byte[] frame : frames) {
                fourCc(out, "00dc");
                int32(out, AVIIF_KEYFRAME);
                int32(out, offset);
                int32(out, frame.length);
                offset += 8 + frame.length + (frame.length & 1);
            }
        }
    }

    private static void fourCc(OutputStream out, String code) throws IOException {
        out.write(code.getBytes(StandardCharsets.US_ASCII));
    }

    private static void int32(OutputStream out, int value) throws IOException {
        out.write(value);
        out.write(value >>> 8);
        out.write(value >>> 16);
        out.write(value >>> 24);
    }

    private static void int16(OutputStream out, int value) throws IOException {
        out.write(value);
        out.write(value >>> 8);
    }
}
