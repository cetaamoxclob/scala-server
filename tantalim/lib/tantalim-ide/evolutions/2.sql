# Tables schema

# --- !Ups

CREATE TABLE db_table
(
    tableID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) DEFAULT '' NOT NULL,
    dbName VARCHAR(50),
    databaseName VARCHAR(50),
    primaryKey VARCHAR(50),
    allowInsert TINYINT,
    allowUpdate TINYINT,
    allowDelete TINYINT
);
CREATE UNIQUE INDEX naturalKey ON db_table (name);
CREATE INDEX databaseName ON db_table (databaseName);

CREATE TABLE db_column
(
    columnID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    tableID INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    required TINYINT UNSIGNED DEFAULT 0,
    dataType VARCHAR(20),
    displayOrder INT DEFAULT 0,
    dbName VARCHAR(50),
    label VARCHAR(50),
    updateable TINYINT UNSIGNED DEFAULT 0
);
ALTER TABLE db_column ADD FOREIGN KEY (tableID) REFERENCES db_table (tableID) ON UPDATE CASCADE;
CREATE UNIQUE INDEX naturalKey ON db_column (tableID, name);

CREATE TABLE db_join
(
    joinID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    fromTableID INT NOT NULL,
    toTableName VARCHAR(50) DEFAULT '' NOT NULL,
    name VARCHAR(50) DEFAULT '' NOT NULL,
    required TINYINT DEFAULT 1
);
CREATE TABLE db_join_column
(
    joinColumnID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    joinID INT NOT NULL,
    fromColumn VARCHAR(50) NOT NULL,
    fromText VARCHAR(50),
    toColumn VARCHAR(50)
);
ALTER TABLE db_join ADD FOREIGN KEY (fromTableID) REFERENCES db_table (tableID) ON UPDATE CASCADE;
CREATE UNIQUE INDEX naturalKey ON db_join (fromTableID, toTableName, name);
CREATE INDEX toTableID ON db_join (toTableName);
ALTER TABLE db_join_column ADD FOREIGN KEY (joinID) REFERENCES db_join (joinID) ON UPDATE CASCADE;
CREATE UNIQUE INDEX naturalKey ON db_join_column (toColumn, fromColumn, fromText);

# --- !Downs

DROP TABLE db_join_column;
DROP TABLE db_join;
DROP TABLE db_column;
DROP TABLE db_table;
