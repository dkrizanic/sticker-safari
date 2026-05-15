-- Add stars column to collector
ALTER TABLE collector ADD COLUMN IF NOT EXISTS stars INTEGER DEFAULT 0;

-- Create completed_album table
CREATE TABLE IF NOT EXISTS completed_album (
    id BIGSERIAL PRIMARY KEY,
    collector_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    FOREIGN KEY (collector_id) REFERENCES collector(id) ON DELETE CASCADE,
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_completed_album_collector ON completed_album(collector_id);
CREATE INDEX IF NOT EXISTS idx_completed_album_date ON completed_album(completed_at);
