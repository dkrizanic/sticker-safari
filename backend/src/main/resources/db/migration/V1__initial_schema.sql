-- Initial database schema for Sticker Safari
-- Created with Flyway migration

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS user_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS collector_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS album_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS sticker_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS notification_id_seq START WITH 1 INCREMENT BY 1;

-- User account table (for authentication)
CREATE TABLE user_account (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Collector table
CREATE TABLE collector (
    id BIGINT PRIMARY KEY DEFAULT nextval('collector_id_seq'),
    user_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(255),
    last_pack_open_date DATE,
    daily_sticker_count INTEGER,
    stars INTEGER,
    CONSTRAINT fk_collector_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- Album table
CREATE TABLE album (
    id BIGINT PRIMARY KEY DEFAULT nextval('album_id_seq'),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sticker table
CREATE TABLE sticker (
    id BIGINT PRIMARY KEY DEFAULT nextval('sticker_id_seq'),
    name VARCHAR(255) NOT NULL,
    latin_name VARCHAR(255),
    description TEXT,
    habitat VARCHAR(255),
    src VARCHAR(500),
    album BIGINT NOT NULL,
    CONSTRAINT fk_sticker_album FOREIGN KEY (album) REFERENCES album(id) ON DELETE CASCADE
);

-- Collector-Sticker junction table (many-to-many with quantity)
CREATE TABLE collector_sticker (
    collector_id BIGINT NOT NULL,
    sticker_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (collector_id, sticker_id),
    CONSTRAINT fk_collector_sticker_collector FOREIGN KEY (collector_id) REFERENCES collector(id) ON DELETE CASCADE,
    CONSTRAINT fk_collector_sticker_sticker FOREIGN KEY (sticker_id) REFERENCES sticker(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

-- Trade Offer table
CREATE TABLE trade_offer (
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    offered_sticker_id BIGINT NOT NULL,
    received_sticker_id BIGINT NOT NULL,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sender_id, receiver_id, offered_sticker_id, received_sticker_id),
    CONSTRAINT fk_trade_sender FOREIGN KEY (sender_id) REFERENCES collector(id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_receiver FOREIGN KEY (receiver_id) REFERENCES collector(id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_offered FOREIGN KEY (offered_sticker_id) REFERENCES sticker(id) ON DELETE CASCADE,
    CONSTRAINT fk_trade_received FOREIGN KEY (received_sticker_id) REFERENCES sticker(id) ON DELETE CASCADE,
    CONSTRAINT chk_different_collectors CHECK (sender_id != receiver_id),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REFUSED'))
);

-- Notification table
CREATE TABLE notification (
    id BIGINT PRIMARY KEY DEFAULT nextval('notification_id_seq'),
    recipient_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES collector(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_sticker_album ON sticker(album);
CREATE INDEX idx_collector_sticker_collector ON collector_sticker(collector_id);
CREATE INDEX idx_collector_sticker_sticker ON collector_sticker(sticker_id);
CREATE INDEX idx_trade_sender ON trade_offer(sender_id);
CREATE INDEX idx_trade_receiver ON trade_offer(receiver_id);
CREATE INDEX idx_trade_status ON trade_offer(status);
CREATE INDEX idx_notification_recipient ON notification(recipient_id);
CREATE INDEX idx_notification_read ON notification(read);
CREATE INDEX idx_notification_created_at ON notification(created_at);
