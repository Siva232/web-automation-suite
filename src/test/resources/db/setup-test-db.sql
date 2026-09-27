-- ------------------------------------------------------------------
-- Sample test databases for the @db scenarios (MySQL 8+).
-- Run once as an admin user, e.g.:
--   mysql -u root -p < src/test/resources/db/setup-test-db.sql
-- Replace with your application's real QA/UAT databases when ready.
-- ------------------------------------------------------------------

-- App1 databases
CREATE DATABASE IF NOT EXISTS qa_db;
CREATE DATABASE IF NOT EXISTS uat_db;
-- App2 databases
CREATE DATABASE IF NOT EXISTS qa_app2_db;
CREATE DATABASE IF NOT EXISTS uat_app2_db;

-- Least-privilege account used by the automation suite (matches config.properties)
CREATE USER IF NOT EXISTS 'qa_automation'@'localhost' IDENTIFIED BY 'ChangeMe_123';
GRANT SELECT, INSERT, UPDATE, DELETE ON qa_db.*       TO 'qa_automation'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON uat_db.*      TO 'qa_automation'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON qa_app2_db.*  TO 'qa_automation'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON uat_app2_db.* TO 'qa_automation'@'localhost';
FLUSH PRIVILEGES;

-- ------------------------- App1 - QA ------------------------------
USE qa_db;
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT IGNORE INTO users (username, email, status) VALUES
    ('tomsmith',  'tomsmith@example.com',  'ACTIVE'),
    ('janedoe',   'janedoe@example.com',   'ACTIVE'),
    ('olduser',   'olduser@example.com',   'INACTIVE');

-- ------------------------- App1 - UAT -----------------------------
USE uat_db;
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT IGNORE INTO users (username, email, status) VALUES
    ('tomsmith',  'tomsmith@example.com',  'ACTIVE'),
    ('uatuser',   'uatuser@example.com',   'ACTIVE');

-- ------------------------- App2 - QA ------------------------------
USE qa_app2_db;
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT IGNORE INTO users (username, email, status) VALUES
    ('standard_user',   'standard_user@example.com',   'ACTIVE'),
    ('locked_out_user', 'locked_out_user@example.com', 'LOCKED');

-- ------------------------- App2 - UAT -----------------------------
USE uat_app2_db;
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT IGNORE INTO users (username, email, status) VALUES
    ('standard_user',   'standard_user@example.com',   'ACTIVE'),
    ('locked_out_user', 'locked_out_user@example.com', 'LOCKED');
