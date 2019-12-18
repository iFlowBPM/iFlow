CREATE TABLE `application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `description` VARCHAR(256) NULL,
  PRIMARY KEY (`id`));
  
INSERT INTO `application` (`name`, `description`) VALUES ('gdpr', 'gdpr');
INSERT INTO `application` (`name`, `description`) VALUES ('compliance', 'compliance');
INSERT INTO `application` (`name`, `description`) VALUES ('finance', 'finance');
 