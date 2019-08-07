ALTER TABLE user_notifications ADD [suspend] DATETIME NULL;
alter table user_notifications add [picktask] smallint;
alter table user_notifications add [isread] smallint;
alter table user_notifications add [showdetail] smallint;
alter table user_notifications add [externallink] varchar(512);
ALTER TABLE user_notifications ADD [activedate] DATETIME NULL;
