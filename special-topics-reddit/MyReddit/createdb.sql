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
    username VARCHAR(30) NOT NULL,
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
    sub_name VARCHAR(30) NOT NULL,
    title TEXT NOT NULL,
    date DATETIME(6) NOT NULL DEFAULT NOW(6),
    username VARCHAR(30) NOT NULL,
    content BLOB NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    edit_date DATETIME(6),
    num_of_comments INT UNSIGNED DEFAULT 0,
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
    content BLOB NOT NULL,
    edited BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (p_number, number),
    FOREIGN KEY (p_number) REFERENCES Post(p_number),
    FOREIGN KEY (p_number, c_number) REFERENCES Comment(p_number, number),
    FOREIGN KEY (username) REFERENCES User(username)
);

DELIMITER $$
CREATE TRIGGER edit_comment BEFORE UPDATE ON Comment FOR EACH ROW
  BEGIN IF new.edited IS TRUE THEN
    SET new.edit_date := NOW(6);
  END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER default_comment BEFORE INSERT ON Comment FOR EACH ROW
  BEGIN IF new.c_number IS NULL THEN
    SET new.c_number := new.number;
  END IF;
    UPDATE Post
    SET num_of_comments = Post.num_of_comments + 1
    WHERE Post.p_number = new.p_number;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER edit_post BEFORE UPDATE ON Post FOR EACH ROW
  BEGIN IF new.edited IS TRUE THEN
    SET new.edit_date := NOW(6);
  END IF;
END$$
DELIMITER ;

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
    sub_name VARCHAR(30) NOT NULL,
    p_number INT UNSIGNED NOT NULL,
    reaction SMALLINT NOT NULL,
    amount SMALLINT NOT NULL DEFAULT 1,
    PRIMARY KEY (p_number, reaction),
    FOREIGN KEY (p_number) REFERENCES Post(p_number),
    FOREIGN KEY (sub_name) REFERENCES Subforum(sub_name)
);

CREATE TABLE CReaction (
    p_number INT UNSIGNED NOT NULL, 
    c_number INT UNSIGNED NOT NULL,
    reaction SMALLINT NOT NULL, 
    amount SMALLINT NOT NULL DEFAULT 1,
    PRIMARY KEY (p_number, c_number, reaction),
    FOREIGN KEY (p_number, c_number) REFERENCES Comment(p_number, number)
);

CREATE TABLE MReaction (
    name VARCHAR(30) NOT NULL,
    date DATETIME NOT NULL,
    reaction SMALLINT NOT NULL,
    amount SMALLINT NOT NULL DEFAULT 1,
    PRIMARY KEY (name, date, reaction),
    FOREIGN KEY (name, date) REFERENCES Message(name, date)
);


INSERT INTO User (username, email, password) VALUES 
('bob', 'bob@gmail.com', 'hetbsf'),
('amy', 'amy@gmail.com', 'hrgv'),
('jack', 'jack@gmail.com', 'gwrd');

INSERT INTO Room VALUES ('hello'), ('fun'), ('bleh');

INSERT INTO Message (name, content, username) VALUES ('hello', 'lallala','bob');
INSERT INTO Message (name, content, username) VALUES ('hello', 'hi friends', 'amy');
INSERT INTO Message (name, content, username) VALUES ('fun', 'no im alone', 'jack');
INSERT INTO Message (name, content, username) VALUES ('hello', 'sucks', 'bob');


SELECT * FROM Message;

SELECT name, date FROM Message WHERE name = 'hello';


SELECT m.name, m.date, content, username, edited, edit_date, reaction, amount FROM (SELECT * FROM Message WHERE name = 'hello') as m LEFT OUTER JOIN (MReaction as mr) ON m.name = mr.name AND m.date = mr.date;
SELECT * FROM Message WHERE name = 'hello' ORDER BY date DESC;
SELECT m.name, m.date, reaction, amount FROM (SELECT name, date FROM Message WHERE name = 'hello') as m LEFT OUTER JOIN (MReaction as mr) ON m.name = mr.name AND m.date = mr.date;
SELECT m.name, m.date, reaction, amount FROM (SELECT name, date FROM Message WHERE name = 'hello') as m NATURAL JOIN MReaction;


INSERT INTO Subforum VALUES ('hehe', 'hwr hwt whtf'), ('haha', 'hertdhgaeth');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'a title', 'amy', 'hwtr nononon'), ('hehe', 'bob is great', 'bob', "i'm awesome");
INSERT INTO Comment (p_number, number, username, content) VALUES (1, 1, 'bob', 'comment on my post');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'nanananananananana', 'amy', 'hwtr nononon'), ('hehe', 'bob is not great', 'jack', "lalalala");
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'need more posts', 'amy', 'hwtr nononon'), ('hehe', 'i am great', 'jack', 'lalalala');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'need lots more posts', 'amy', 'hwtr nononon'), ('hehe', 'i am the best', 'bob', 'lalalala');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'nanananananananana', 'amy', 'hwtr nononon'), ('hehe', 'bob is not great', 'jack', "lalalala");
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'need more posts', 'amy', 'hwtr nononon'), ('hehe', 'i am great', 'jack', 'lalalala');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'need lots more posts', 'amy', 'hwtr nononon'), ('hehe', 'i am the best', 'bob', 'lalalala');
INSERT INTO Post (sub_name, title, username, content) VALUES ('hehe', 'need lots more posts. Im making a super long post because', 'amy', 'hwtr nononon');

INSERT INTO Comment ( p_number, number, username, content) VALUES ( 1, 2, 'amy', 'comment on my post');
INSERT INTO Comment ( p_number, number, c_number, username, content) VALUES ( 1, 3, 2, 'bob', 'comment on my post');
INSERT INTO Comment ( p_number, number, username, content) VALUES ( 1, 4, 'amy', 'hahah comment on my post');
INSERT INTO Comment ( p_number, number, username, content) VALUES ( 1, 5, 'bob', 'need to change these commnets');
INSERT INTO Comment ( p_number, number, username, content) VALUES ( 1, 6, 'bob', 'I AM BOB');

INSERT INTO Comment ( p_number, number, username, content) VALUES ( 1, 7, 'bob', 'bobesto is the best');
INSERT INTO Comment ( p_number, number,username, content) VALUES ( 1, 8, 'bob', 'no one can beat bob');
INSERT INTO Comment ( p_number, number, c_number, username, content) VALUES ( 1, 9, 3, 'bob', 'yea, bob is the best');

select * from Comment;


INSERT INTO PReaction (sub_name, p_number, reaction, amount) VALUES ('hehe', 1, 1, 1);
SELECT * FROM (SELECT * FROM Post WHERE p_number = 1) as p NATURAL JOIN PReaction;


//INSERT INTO MReaction (name, date) VALUES (2);
SELECT * FROM Post;
