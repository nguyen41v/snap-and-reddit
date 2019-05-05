//SET TRANSACTION ISOLATION_LEVEL SERIALIZABLE

DROP TABLE PReaction;
DROP TABLE CReaction;

DROP TABLE MReaction;

DROP TABLE Follow;
DROP TABLE Message;
DROP TABLE Room;
DROP TABLE Comment;
DROP TABLE Post;
DROP TABLE Subforum;
DROP TABLE User;



CREATE TABLE User (
    username VARCHAR(16) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(15),
    PRIMARY KEY (username)
);

CREATE TABLE Subforum (
    sub_name VARCHAR(30) NOT NULL,
    info TEXT,
    PRIMARY KEY (sub_name)
);


CREATE TABLE Post (
    p_number INT UNSIGNED NOT NULL AUTO_INCREMENT,
    sub_name VARCHAR(30),
    title TINYTEXT NOT NULL,
    date DATETIME(6) NOT NULL DEFAULT NOW(6),
    username VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    edit_date DATETIME(6),
    num_of_comments INT UNSIGNED DEFAULT 0,
    r_sparkle INT UNSIGNED DEFAULT 1,
    r_cry INT UNSIGNED DEFAULT 0,
    r_angry INT UNSIGNED DEFAULT 0,
    PRIMARY KEY (p_number),
    FOREIGN KEY (sub_name) REFERENCES Subforum(sub_name),
    FOREIGN KEY (username) REFERENCES User(username)
);

CREATE TABLE Comment (
    p_number INT UNSIGNED NOT NULL,
    number INT UNSIGNED NOT NULL,
    c_number INT UNSIGNED,
    date DATETIME(6) NOT NULL DEFAULT NOW(6),
    edit_date DATETIME(6) ON UPDATE NOW(6),
    username VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    r_sparkle INT UNSIGNED DEFAULT 1,
    r_cry INT UNSIGNED DEFAULT 0,
    r_angry INT UNSIGNED DEFAULT 0,
    PRIMARY KEY (p_number, number),
    FOREIGN KEY (p_number) REFERENCES Post(p_number),
    FOREIGN KEY (p_number, c_number) REFERENCES Comment(p_number, number),
    FOREIGN KEY (username) REFERENCES User(username)
);



CREATE TABLE Follow (
    item ENUM('u','s','r') NOT NULL,
    name VARCHAR(30) NOT NULL,
    username VARCHAR(30) NOT NULL,
    PRIMARY KEY (item, name, username),
    FOREIGN KEY (username) REFERENCES User(username)
);

CREATE TABLE Room (
    name VARCHAR(30) NOT NULL,
    PRIMARY KEY (name)
);

CREATE TABLE Message (
    name VARCHAR(30) NOT NULL,
    date DATETIME(6) NOT NULL DEFAULT NOW(6),
    content BLOB NOT NULL,
    username VARCHAR(30) NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    edit_date DATETIME(6) ON UPDATE NOW(6),
    PRIMARY KEY (name, date),
    FOREIGN KEY (name) REFERENCES Room(name),
    FOREIGN KEY (username) REFERENCES User(username)
);

CREATE TABLE PReaction (
    p_number INT UNSIGNED NOT NULL,
    reaction ENUM('s','c','a') NOT NULL,
    username VARCHAR(16) NOT NULL,
    PRIMARY KEY (p_number, reaction, username),
    FOREIGN KEY (p_number) REFERENCES Post(p_number),
    FOREIGN KEY (username) REFERENCES User(username)
);

CREATE TABLE CReaction (
    p_number INT UNSIGNED NOT NULL,
    number INT UNSIGNED NOT NULL,
    reaction ENUM('s','c','a') NOT NULL,
    username VARCHAR(16) NOT NULL,
    PRIMARY KEY (p_number, number, reaction, username),
    FOREIGN KEY (p_number, number) REFERENCES Comment(p_number, number),
    FOREIGN KEY (username) REFERENCES User(username)
);

CREATE TABLE MReaction (
    name VARCHAR(30) NOT NULL,
    date DATETIME NOT NULL,
    reaction SMALLINT NOT NULL,
    amount SMALLINT NOT NULL DEFAULT 1,
    PRIMARY KEY (name, date, reaction),
    FOREIGN KEY (name, date) REFERENCES Message(name, date)
);


INSERT INTO Room VALUES ('hello'), ('fun'), ('bleh');
INSERT INTO User VALUES ('[deleted]', '', '', '[deleted]');
INSERT INTO Subforum VALUES ('announcements', 'Get announcements about new and upcoming updates to the app!');

DELIMITER $$
CREATE TRIGGER edit_comment BEFORE UPDATE ON Comment FOR EACH ROW
  BEGIN IF new.edited IS TRUE THEN
    SET new.edit_date = NOW(6);
  END IF;
END$$
CREATE TRIGGER default_comment BEFORE INSERT ON Comment FOR EACH ROW
  BEGIN IF new.c_number IS NULL THEN
    SET new.c_number = new.number;
  END IF;
    UPDATE Post
    SET num_of_comments = Post.num_of_comments + 1
    WHERE Post.p_number = new.p_number;
END$$
CREATE TRIGGER edit_post BEFORE UPDATE ON Post FOR EACH ROW
  BEGIN IF new.edited IS TRUE THEN
    SET new.edit_date = NOW(6);
  END IF;
END$$
CREATE TRIGGER user_sub BEFORE INSERT ON Post FOR EACH ROW
  BEGIN IF new.sub_name IS NULL THEN
    SET new.sub_name = new.username;
  END IF;
END$$

CREATE TRIGGER new_user AFTER INSERT ON User FOR EACH ROW
  BEGIN
    INSERT INTO Follow
    VALUES ('s', 'announcements', new.username);
END$$
DELIMITER ;



