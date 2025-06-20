INSERT INTO user (email)
VALUES ('test@test.com');
INSERT INTO product (user_id, external_id, name, description)
VALUES ((select id from user where email = 'test@test.com'), 1, 'Paraplu', 'Een storm paraplu, handig in de regen!');
INSERT INTO product (user_id, external_id, name, description)
VALUES ((select id from user where email = 'test@test.com'), 2, 'Bal', 'Een bal, leuk voor op het strand!');
