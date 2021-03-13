CREATE TABLE IF NOT EXISTS `session_store` (
  `session_id` VARCHAR(36) NOT NULL,
  `idle_timeout` DOUBLE DEFAULT NULL,
  `absolute_timeout` DOUBLE DEFAULT NULL,
  `value` BLOB,
  PRIMARY KEY (`session_id`)
)