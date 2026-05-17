-- Migration V6: Make sticker.album column nullable
-- Stickers can optionally be assigned to an album (per requirements)

ALTER TABLE sticker ALTER COLUMN album DROP NOT NULL;
