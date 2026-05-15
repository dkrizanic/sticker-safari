INSERT INTO collector (id, username)
VALUES (1, 'John'),
       (2, 'Jane'),
       (3, 'Julie');

INSERT INTO album (id, name)
VALUES (1, 'ANIMAL KINGDOM');

INSERT INTO sticker (id, name, latin_name, description, habitat, album)
VALUES (1, 'Blue Butterfly', 'Papilio azureus', 'A beautiful blue butterfly species.', 'Forest', 1),
       (2, 'Red Ladybug', 'Coccinella septempunctata', 'A small red ladybug with black spots.', 'Garden', 1),
       (3, 'Tigar', 'Biggus tigrus', 'Big tigers', 'Asia', 1);

INSERT INTO collector_sticker (sticker_id, collector_id, quantity)
VALUES (1, 1, 1),
       (3, 3, 2);

INSERT INTO trade_offer (sender_id, receiver_id, offered_sticker_id, received_sticker_id, message, status)
VALUES (1, 2, 1, 2, 'Hello', 'PENDING'),



