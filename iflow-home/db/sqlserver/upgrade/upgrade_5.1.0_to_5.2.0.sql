CREATE TABLE application (
  id INT NOT NULL IDENTITY(1,1),
  name VARCHAR(128)  constraint application_name_nn not null,
  description VARCHAR(256) NULL,
  constraint application_pk primary key (id)
  )
GO
  
INSERT INTO application (name, description) VALUES ('gdpr', 'GDPR')
GO
INSERT INTO application (name, description) VALUES ('compliance', 'Compliance')
GO
INSERT INTO application (name, description) VALUES ('financeira', 'Financeira')
GO


ALTER TABLE application 
ADD noregister INT NULL DEFAULT 0
GO