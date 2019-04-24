DROP TABLE Transactions;
DROP TABLE Benefits;
DROP TABLE Local_offices;
DROP TABLE Stores;
DROP TABLE Users;
DROP TABLE States;


CREATE TABLE States (
    state CHAR(2) NOT NULL,
    name VARCHAR(30) NOT NULL,
    state_hotline CHAR(12) NOT NULL,
    eligibility BOOLEAN NOT NULL,
    type VARCHAR(10) NOT NULL,
    uniform BOOLEAN NOT NULL,
    first_day INT NOT NULL,
    last_day INT NOT NULL,
    PRIMARY KEY (state)
);

CREATE TABLE State_specific (
    state CHAR(2) NOT NULL,
    state_only_hotline VARCHAR(12) DEFAULT NULL,
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

CREATE TABLE Local_offices (
    phone_number CHAR(16) NOT NULL,
    street VARCHAR(50) NOT NULL,
    city VARCHAR(40) NOT NULL,
    state CHAR(2) NOT NULL,
    zip_code CHAR(5) NOT NULL,
    county VARCHAR(20) NOT NULL,
    PRIMARY KEY (phone_number),
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
    name VARCHAR(16) NOT NULL,
    password VARCHAR(50) NOT NULL,
    phone_number CHAR(16) NOT NULL,
    email VARCHAR(50),
    current_balance DECIMAL(10, 2) DEFAULT 0,
    average_meals SMALLINT,
    case_number VARCHAR(10),
    first_name VARCHAR(20),
    middle_initial CHAR(1),
    last_name VARCHAR(30),
    city VARCHAR(40),
    zip_code CHAR(5),
    zip4 CHAR(4),
    state CHAR(2),
    county VARCHAR(20),
    PRIMARY KEY (name),
    UNIQUE KEY (phone_number),
    FOREIGN KEY (state) REFERENCES States(state)
);

CREATE TABLE Transactions (
    name VARCHAR(16) NOT NULL,
    number INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    date DATE DEFAULT NOW(),
    PRIMARY KEY (name, number),
    FOREIGN KEY (name) REFERENCES Users(name)
);


DELIMITER $$
CREATE TRIGGER update_balance BEFORE INSERT ON Transactions FOR EACH ROW
    BEGIN
    IF new.type = "benefits" THEN
        UPDATE Users
        SET current_balance = Users.current_balance + new.amount
        WHERE Users.name = new.name;
    END IF;
END$$
DELIMITER ;

