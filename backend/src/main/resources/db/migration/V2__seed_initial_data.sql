-- Seed initial test data for Sticker Safari
-- This provides sample data for development and testing

-- Insert collectors
INSERT INTO collector (id, username, created_at) VALUES
    (nextval('collector_id_seq'), 'Bob', CURRENT_TIMESTAMP),
    (nextval('collector_id_seq'), 'Jack', CURRENT_TIMESTAMP),
    (nextval('collector_id_seq'), 'Anne', CURRENT_TIMESTAMP),
    (nextval('collector_id_seq'), 'Ema', CURRENT_TIMESTAMP);

-- Insert albums
INSERT INTO album (id, name, created_at) VALUES
    (nextval('album_id_seq'), 'African Safari', CURRENT_TIMESTAMP),
    (nextval('album_id_seq'), 'Ocean Life', CURRENT_TIMESTAMP);

-- Insert stickers for African Safari album
INSERT INTO sticker (id, name, latin_name, description, habitat, src, album) VALUES
    (nextval('sticker_id_seq'), 'Lion', 'Panthera leo', 'The king of the jungle', 'Savanna', 'https://images.unsplash.com/photo-1546182990-dffeafbe841d', 1),
    (nextval('sticker_id_seq'), 'Elephant', 'Loxodonta africana', 'Largest land animal', 'Grasslands', 'https://images.unsplash.com/photo-1564760055775-d63b17a55c44', 1),
    (nextval('sticker_id_seq'), 'Giraffe', 'Giraffa camelopardalis', 'Tallest mammal', 'Savanna', 'https://images.unsplash.com/photo-1547721064-da6cfb341d50', 1),
    (nextval('sticker_id_seq'), 'Zebra', 'Equus quagga', 'Striped horse', 'Plains', 'https://images.unsplash.com/photo-1505932794465-147d1f1b2c97', 1),
    (nextval('sticker_id_seq'), 'Cheetah', 'Acinonyx jubatus', 'Fastest land animal', 'Grasslands', 'https://images.unsplash.com/photo-1551969014-7d2c4cddf0b6', 1);

-- Insert stickers for Ocean Life album
INSERT INTO sticker (id, name, latin_name, description, habitat, src, album) VALUES
    (nextval('sticker_id_seq'), 'Dolphin', 'Delphinus delphis', 'Intelligent marine mammal', 'Ocean', 'https://images.unsplash.com/photo-1570481662006-a3a1374699e8', 2),
    (nextval('sticker_id_seq'), 'Whale', 'Balaenoptera musculus', 'Largest animal on Earth', 'Deep Ocean', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19', 2),
    (nextval('sticker_id_seq'), 'Shark', 'Carcharodon carcharias', 'Apex predator', 'Coastal Waters', 'https://images.unsplash.com/photo-1560275619-4662e36fa65c', 2),
    (nextval('sticker_id_seq'), 'Sea Turtle', 'Chelonia mydas', 'Ancient marine reptile', 'Coral Reefs', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19', 2),
    (nextval('sticker_id_seq'), 'Octopus', 'Octopus vulgaris', 'Eight-armed mollusk', 'Rocky Reefs', 'https://images.unsplash.com/photo-1545671913-b89ac1b4ac10', 2);

-- Give Bob some initial stickers
INSERT INTO collector_sticker (collector_id, sticker_id, quantity) VALUES
    (1, 1, 2),  -- Bob has 2 Lions
    (1, 2, 1),  -- Bob has 1 Elephant
    (1, 6, 1);  -- Bob has 1 Dolphin
