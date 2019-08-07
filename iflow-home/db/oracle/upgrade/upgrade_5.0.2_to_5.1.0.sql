ALTER TABLE user_notifications ADD COLUMN suspend DATETIME NULL;
ALTER TABLE user_notifications ADD COLUMN picktask smallint;
ALTER TABLE user_notifications ADD COLUMN isread smallint;
ALTER TABLE user_notifications ADD COLUMN showdetail smallint;
ALTER TABLE user_notifications ADD COLUMN externallink varchar(512);
ALTER TABLE user_notifications ADD COLUMN activedate DATETIME NULL;