# --- !Ups

DROP TABLE IF EXISTS `slhpAttachmentType`;

CREATE TABLE `slhpAttachmentType` (
  `AttachmentTypeCode` VARCHAR(10) NOT NULL PRIMARY KEY,
  `AttachmentType` VARCHAR(50) NOT NULL,
  `ActiveFlag` TINYINT NOT NULL,
  `DisplaySequence` INT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE slhpAttachmentType;
