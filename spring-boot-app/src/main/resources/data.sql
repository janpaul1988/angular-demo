INSERT INTO user (name, email)
VALUES ('tester', 'test@test.com');
INSERT INTO product (user_id, external_id, name, description)
VALUES ((select id from user where name = 'tester'), 1, 'Paraplu', 'Een storm paraplu, handig in de regen!');
INSERT INTO product (user_id, external_id, name, description)
VALUES ((select id from user where name = 'tester'), 2, 'Bal', 'Een bal, leuk voor op het strand!');
