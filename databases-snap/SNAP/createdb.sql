DROP TABLE Transactions;
DROP TABLE Users;
DROP TABLE Benefits;
DROP TABLE Stores;
DROP TABLE State_specific;
DROP TABLE Counties;
DROP TABLE States;


CREATE TABLE States (
    state CHAR(2) NOT NULL,
    name VARCHAR(30) NOT NULL,
    state_hotline CHAR(12) NOT NULL,
    eligibility BOOLEAN NOT NULL,
    type CHAR(1) NOT NULL,
    uniform BOOLEAN NOT NULL,
    first_day INT NOT NULL,
    last_day INT NOT NULL,
    PRIMARY KEY (state)
);

CREATE TABLE State_specific (
    state CHAR(2) NOT NULL,
    state_only_hotline VARCHAR(12) NOT NULL,
    PRIMARY KEY (state, state_only_hotline),
    FOREIGN KEY (state) REFERENCES States(state)
);

CREATE TABLE Benefits(
    state CHAR(2) NOT NULL,
    day TINYINT NOT NULL,
    condition1 VARCHAR(3),
    condition2 VARCHAR(3),
    condition3 VARCHAR(3),
    condition4 VARCHAR(3),
    condition5 VARCHAR(3),
    PRIMARY KEY (state, day),
    FOREIGN KEY (state) REFERENCES States(state)
);

CREATE TABLE Counties (
    phone_number CHAR(16) NOT NULL,
    street VARCHAR(50) NOT NULL,
    city VARCHAR(40) NOT NULL,
    state CHAR(2) NOT NULL,
    zip_code CHAR(5) NOT NULL,
    county VARCHAR(30) NOT NULL,
    PRIMARY KEY (phone_number),
    UNIQUE KEY (county, state),
    FOREIGN KEY (state) REFERENCES States(state)
);

CREATE TABLE Stores (
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    name VARCHAR(80) NOT NULL,
    street VARCHAR(50) NOT NULL,
    address_line2 VARCHAR(40),
    city VARCHAR(40) NOT NULL,
    state CHAR(2) NOT NULL,
    zip_code CHAR(5) NOT NULL,
    zip4 CHAR(4),
    county VARCHAR(30) NOT NULL,
    PRIMARY KEY (name, longitude, latitude),
    FOREIGN KEY (state) REFERENCES States(state),
    FOREIGN KEY (county) REFERENCES Counties(county)
);

CREATE TABLE Users (
    name VARCHAR(16) NOT NULL,
    password VARCHAR(50) NOT NULL,
    phone_number CHAR(12) NOT NULL,
    email VARCHAR(50),
    current_balance DECIMAL(10, 2) DEFAULT 0,
    last_two_ssn CHAR(2),
    average_meals SMALLINT DEFAULT 1,
    case_number VARCHAR(10),
    first_name VARCHAR(20),
    middle_initial CHAR(1),
    last_name VARCHAR(30),
    birthday DATE,
    state CHAR(2),
    county VARCHAR(20),
    num_transactions INT DEFAULT 0,
    PRIMARY KEY (name),
    UNIQUE KEY (phone_number),
    FOREIGN KEY (state) REFERENCES States(state),
    FOREIGN KEY (county) REFERENCES Counties(county)
);

CREATE TABLE Transactions (
    name VARCHAR(16) NOT NULL,
    number INT NOT NULL,
    spend BOOLEAN NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    date DATETIME DEFAULT NOW(),
    PRIMARY KEY (name, number),
    FOREIGN KEY (name) REFERENCES Users(name)
);



DELIMITER $$
CREATE TRIGGER update_balance BEFORE INSERT ON Transactions FOR EACH ROW
    BEGIN
    IF new.spend THEN
        UPDATE Users
        SET current_balance = Users.current_balance - new.amount, num_transactions = Users.num_transactions + 1
        WHERE Users.name = new.name;
    ELSE
        UPDATE Users
        SET current_balance = Users.current_balance + new.amount, num_transactions = Users.num_transactions + 1
        WHERE Users.name = new.name;
    END IF;

END$$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER update_balance1 BEFORE UPDATE ON Transactions FOR EACH ROW
    BEGIN
    IF new.spend THEN
        IF old.spend THEN
            UPDATE Users
            SET current_balance = Users.current_balance - new.amount + old.amount
            WHERE Users.name = new.name;
        ELSE
            UPDATE Users
            SET current_balance = Users.current_balance - new.amount - old.amount
            WHERE Users.name = new.name;
        END IF;
    ELSE
        IF old.spend THEN
            UPDATE Users
            SET current_balance = Users.current_balance + new.amount + old.amount
            WHERE Users.name = new.name;
        ELSE
            UPDATE Users
            SET current_balance = Users.current_balance + new.amount - old.amount
            WHERE Users.name = new.name;
        END IF;
    END IF;
END$$
DELIMITER ;

drop table transactions; drop table users;




INSERT INTO Users (name, password, phone_number, current_balance, average_meals)
VALUES ("bob", "hrfb", "123-890-3456", 21.50, 3);
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 1, FALSE, 70.00,  "2019-03-02");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 1, TRUE, 2.00,  "2019-03-17");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 2, TRUE, 21.00,  "2019-03-17");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 3, TRUE, 32.00,  "2019-03-17");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 4, TRUE, 2.00,  "2019-03-17");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 5, TRUE, 2.00,  "2019-03-17");
INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 6, TRUE, 2.00,  "2019-03-17");


INSERT INTO Transactions (name, number, spend, amount, date) VALUES ("bob", 7, FALSE, 42.00,  "2019-04-17");
