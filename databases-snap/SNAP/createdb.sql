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
    type VARCHAR(2) NOT NULL,
    uniform BOOLEAN NOT NULL,
    first_day INT NOT NULL,
    last_day INT NOT NULL,
    application VARCHAR(100) NOT NULL,
    UNIQUE (name),
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
    FOREIGN KEY (state) REFERENCES States(state)
);

CREATE TABLE Users (
    username VARCHAR(16) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone_number CHAR(12) NOT NULL,
    email VARCHAR(50),
    current_balance DECIMAL(10, 2) DEFAULT 0,
    average_meals SMALLINT DEFAULT 1,
    state CHAR(2),
    county VARCHAR(20),
    num_transactions INT DEFAULT 0,
    PRIMARY KEY (username),
    UNIQUE KEY (phone_number),
    FOREIGN KEY (state) REFERENCES States(state),
    FOREIGN KEY (county) REFERENCES Counties(county)
);

CREATE TABLE Transactions (
    username VARCHAR(16) NOT NULL,
    number INT NOT NULL,
    spend BOOLEAN NOT NULL,
    amount DECIMAL(10, 2) UNSIGNED NOT NULL,
    description VARCHAR(45) NOT NULL,
    date DATETIME DEFAULT NOW(),
    PRIMARY KEY (username, number),
    FOREIGN KEY (username) REFERENCES Users(username)
);



DELIMITER $$
CREATE TRIGGER update_balance BEFORE INSERT ON Transactions FOR EACH ROW
    BEGIN
    IF new.spend THEN
        UPDATE Users
        SET current_balance = Users.current_balance - new.amount, num_transactions = Users.num_transactions + 1
        WHERE Users.username = new.username;
    ELSE
        UPDATE Users
        SET current_balance = Users.current_balance + new.amount, num_transactions = Users.num_transactions + 1
        WHERE Users.username = new.username;
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
            WHERE Users.username = new.username;
        ELSE
            UPDATE Users
            SET current_balance = Users.current_balance - new.amount - old.amount
            WHERE Users.username = new.username;
        END IF;
    ELSE
        IF old.spend THEN
            UPDATE Users
            SET current_balance = Users.current_balance + new.amount + old.amount
            WHERE Users.username = new.username;
        ELSE
            UPDATE Users
            SET current_balance = Users.current_balance + new.amount - old.amount
            WHERE Users.username = new.username;
        END IF;
    END IF;
END$$
DELIMITER ;