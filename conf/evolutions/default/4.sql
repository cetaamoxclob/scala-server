# --- !Ups

CREATE TABLE slhpClaim
(
  ClaimID   INT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT,
  PatientID INT UNSIGNED             NOT NULL,
  ClaimNumber VARCHAR(50),
  ServiceDate      DATE,
  VendorID INT,
  ProviderID  INT
);

# --- !Downs

DROP TABLE slhpClaim;
