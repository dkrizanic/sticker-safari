-- Add status column to album table
ALTER TABLE album ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Set existing albums to PUBLISHED status (they already have stickers)
UPDATE album SET status = 'PUBLISHED' WHERE id IN (SELECT DISTINCT album FROM sticker);
