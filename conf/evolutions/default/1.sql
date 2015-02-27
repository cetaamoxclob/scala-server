# Users schema

# --- !Ups

CREATE TABLE app_user
(
    userID INT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) DEFAULT '',
    name VARCHAR(100),
    registered DATETIME,
    lastLogin DATETIME
);

CREATE UNIQUE INDEX username ON app_user (username);

# --- !Downs

DROP TABLE app_user;
