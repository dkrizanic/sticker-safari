-- Seed initial test data for Sticker Safari
-- This provides sample data for development and testing

-- Insert collectors with explicit IDs
INSERT INTO collector (id, username, created_at) VALUES
    (1, 'Bob', CURRENT_TIMESTAMP),
    (2, 'Jack', CURRENT_TIMESTAMP),
    (3, 'Anne', CURRENT_TIMESTAMP),
    (4, 'Ema', CURRENT_TIMESTAMP);

-- Update sequence to start after seed data (H2 compatible)
ALTER SEQUENCE collector_id_seq RESTART WITH 5;

-- Insert albums with explicit IDs
INSERT INTO album (id, name, created_at) VALUES
    (1, 'African Safari', CURRENT_TIMESTAMP),
    (2, 'Ocean Life', CURRENT_TIMESTAMP);

-- Update sequence to start after seed data (H2 compatible)
ALTER SEQUENCE album_id_seq RESTART WITH 3;

-- Insert stickers for African Safari album (IDs 1-5)
INSERT INTO sticker (id, name, latin_name, description, habitat, src, album) VALUES
    (1, 'Lion', 'Panthera leo', 'The king of the jungle', 'Savanna', 'https://images.unsplash.com/photo-1546182990-dffeafbe841d', 1),
    (2, 'Elephant', 'Loxodonta africana', 'Largest land animal', 'Grasslands', 'https://images.unsplash.com/photo-1564760055775-d63b17a55c44', 1),
    (3, 'Giraffe', 'Giraffa camelopardalis', 'Tallest mammal', 'Savanna', 'https://images.unsplash.com/photo-1547721064-da6cfb341d50', 1),
    (4, 'Zebra', 'Equus quagga', 'Striped horse', 'Plains', 'https://images.unsplash.com/photo-1505932794465-147d1f1b2c97', 1),
    (5, 'Cheetah', 'Acinonyx jubatus', 'Fastest land animal', 'Grasslands', 'https://images.unsplash.com/photo-1551969014-7d2c4cddf0b6', 1);

-- Insert stickers for Ocean Life album (IDs 6-10)
INSERT INTO sticker (id, name, latin_name, description, habitat, src, album) VALUES
    (6, 'Dolphin', 'Delphinus delphis', 'Intelligent marine mammal', 'Ocean', 'https://images.unsplash.com/photo-1570481662006-a3a1374699e8', 2),
    (7, 'Whale', 'Balaenoptera musculus', 'Largest animal on Earth', 'Deep Ocean', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19', 2),
    (8, 'Shark', 'Carcharodon carcharias', 'Apex predator', 'Coastal Waters', 'https://images.unsplash.com/photo-1560275619-4662e36fa65c', 2),
    (9, 'Sea Turtle', 'Chelonia mydas', 'Ancient marine reptile', 'Coral Reefs', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19', 2),
    (10, 'Octopus', 'Octopus vulgaris', 'Eight-armed mollusk', 'Rocky Reefs', 'https://images.unsplash.com/photo-1545671913-b89ac1b4ac10', 2);

-- Update sequence to start after seed data (H2 compatible)
ALTER SEQUENCE sticker_id_seq RESTART WITH 11;

-- Give Bob some initial stickers
INSERT INTO collector_sticker (collector_id, sticker_id, quantity) VALUES
    (1, 1, 2),  -- Bob has 2 Lions
    (1, 2, 1),  -- Bob has 1 Elephant
    (1, 6, 1);  -- Bob has 1 Dolphin

-- Give Bob some initial stickers
INSERT INTO collector_sticker (collector_id, sticker_id, quantity) VALUES
    (1, 1, 2),  -- Bob has 2 Lions
    (1, 2, 1),  -- Bob has 1 Elephant
    (1, 6, 1);  -- Bob has 1 Dolphin
