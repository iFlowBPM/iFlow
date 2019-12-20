CREATE TABLE application (
  id int GENERATED BY DEFAULT ON NULL AS IDENTITY,,
  name varchar2(128)  constraint application_name_nn not null,
  description varchar2(256) NULL,
  constraint application_pk primary key (id)
  );
  
INSERT INTO application (name, description) VALUES ('gdpr', 'gdpr');
INSERT INTO application (name, description) VALUES ('compliance', 'compliance');
INSERT INTO application (name, description) VALUES ('finance', 'finance');