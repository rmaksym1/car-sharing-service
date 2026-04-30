INSERT INTO users (email, first_name, last_name, password, role, is_deleted)
VALUES ('user@gmail.com', 'John', 'Wick', '$2a$08$eOLv98pkXuh5MFsEt2en7OWx8TVAlsCjJh1nAHw8q.e3X55u3ll96', 'CUSTOMER', false);

INSERT INTO cars (model, brand, car_type, inventory, daily_fee, is_deleted)
VALUES ('Audi', 'R8', 'SEDAN', 12, 145, false);

INSERT INTO rentals (rental_date, return_date, actual_return_date, car_id, user_id)
VALUES ('2026-04-20', '2026-04-25', NULL, 1, 1);