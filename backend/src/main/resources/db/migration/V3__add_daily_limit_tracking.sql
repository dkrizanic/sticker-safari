-- Add daily pack opening limit tracking
ALTER TABLE collector ADD COLUMN IF NOT EXISTS last_pack_open_date DATE;
ALTER TABLE collector ADD COLUMN IF NOT EXISTS daily_sticker_count INTEGER DEFAULT 0;
