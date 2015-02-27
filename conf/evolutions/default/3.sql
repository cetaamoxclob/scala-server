# Users schema

# --- !Ups

CREATE TABLE slhpPatient
(
    PatientID INT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
    Name VARCHAR(100),
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    BirthDate DATE
);

# --- !Downs

DROP TABLE slhpPatient;
