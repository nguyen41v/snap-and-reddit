
SELECT s.state, name, s.type, day, condition1, condition2, condition3, condition4, condition5
FROM (SELECT * FROM States WHERE state="AZ") as S
LEFT JOIN (Benefits as B)
ON B.state=S.state;

SELECT name, state_hotline, state_only_hotline
FROM (SELECT * FROM States WHERE state="AZ) as S
    NATURAL JOIN State_specific;

SELECT name, phone_number, street, city, state, zip_code, county
FROM (SELECT * FROM Local_offices WHERE state="AZ") as L NATURAL JOIN States

SELECT * FROM Stores WHERE 30 > (57 * SQRT(POW(longitude - -118.211904, 2) + POW(lattitude - 34.126813, 2)));

SELECT COUNT(*) FROM Stores WHERE 30 > (57 * SQRT(POW(longitude - -118.211904, 2) + POW(lattitude - 34.126813, 2)));

SELECT current_balance, average_meals FROM Users WHERE name = "my_username";

SELECT * FROM Transactions WHERE name = "my_username";

SELECT SUM(*)
FROM Transactions
WHERE name = "my_username" AND month(date) = month(NOW()) - 1 AND number < 0;

SELECT average_meals FROM Users WHERE name = "my_username";

SELECT current_balance, average_meals, current_balance/(average_meals * (DAY(LAST_DAY(yourdate)) - DAY(NOW())) as money_per_meals_left
FROM Users
WHERE name = "my_username";
