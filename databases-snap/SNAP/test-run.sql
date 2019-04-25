
SELECT s.state, name, s.type, day, condition1, condition2, condition3, condition4, condition5
FROM (SELECT * FROM States WHERE state="AZ") as S
LEFT JOIN (Benefits as B)
ON B.state=S.state;


SELECT s.state, name, s.type, day, condition1, condition2, condition3, condition4, condition5
FROM (SELECT * FROM States WHERE state="MO") as S
LEFT JOIN (Benefits as B)
ON B.state=S.state;


SELECT s.state, name, s.type, day, condition1, condition2, condition3, condition4, condition5
FROM (SELECT * FROM States WHERE state="AL") as S
LEFT JOIN (Benefits as B)
ON B.state=S.state;


SELECT s.state, name, s.type, day, condition1, condition2, condition3, condition4, condition5
FROM (SELECT * FROM States WHERE state="CA") as S
LEFT JOIN (Benefits as B)
ON B.state=S.state;



// get state and hotlines + state only hotlines (may not exist)
SELECT name, state_hotline, state_only_hotline
FROM (SELECT * FROM States WHERE state="AZ") as S
LEFT OUTER JOIN State_specific
ON State_specific.state = S.state;

// get state and state only hotlines if they exist
SELECT name, state_hotline, state_only_hotline
FROM (SELECT * FROM States WHERE state="AZ") as S
    NATURAL JOIN State_specific;

SELECT name, phone_number, street, city, state, zip_code, county
FROM (SELECT * FROM Local_offices WHERE state="AZ") as L NATURAL JOIN States

// get stores in a 5 mile radius
SELECT * FROM Stores WHERE 5 > (5 * SQRT(POW(longitude - -118.211904, 2) + POW(latitude - 34.126813, 2)));

// get number of stores in a 5 mile radius
SELECT COUNT(*) FROM Stores WHERE 5 > (57 * SQRT(POW(longitude - -118.211904, 2) + POW(latitude - 34.126813, 2)));

SELECT current_balance, average_meals FROM Users WHERE name = "bob";

SELECT * FROM Transactions WHERE name = "bob";

// get all purchases over last month
SELECT SUM(amount) total_spent
FROM Transactions
WHERE name = "bob" AND MONTH(date) = MONTH(NOW()) - 1 AND spend;

// get average purchases/day from previous month
SELECT SUM(amount) / DAY(LAST_DAY(NOW() - INTERVAL 1 MONTH)) as average_spent
FROM Transactions
WHERE name = "bob" AND MONTH(date) = MONTH(NOW()) - 1 AND spend;

// get average purchases/meal from previous month
SELECT CONVERTROUND(SUM(amount) / (DAY(LAST_DAY(now() - INTERVAL 1 MONTH)) * average_meals),2) as past_spent
FROM Transactions, (SELECT average_meals FROM Users WHERE name ="bob") as A
WHERE name = "bob" AND MONTH(date) = MONTH(NOW()) - 1 AND spend;

// get total benefits from last month
SELECT SUM(amount) as past_benefits
FROM Transactions
WHERE name = "bob" AND MONTH(date) = MONTH(NOW()) - 1 AND NOT spend;

// get average $$/meal for remainder of month
SELECT current_balance as balance, average_meals, ROUND(current_balance/(average_meals * (DAY(LAST_DAY(NOW())) - DAY(NOW()))),2) as average
FROM Users
WHERE name = "bob";
