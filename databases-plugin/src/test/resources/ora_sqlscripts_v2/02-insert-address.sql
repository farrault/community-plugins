WHENEVER SQLERROR EXIT SQL.SQLCODE

insert into address (id, person_id, street, city, zip, country) values (1, 1, 'laapserveld', 'hilversum', '1213VM', 'Netherland');
insert into address (id, person_id, street, city, zip, country) values (2, 2, 'vaartweg', 'hilversum', '1234SX', 'Netherland');
insert into address (id, person_id, street, city, zip, country) values (3, 3, 'havenstraat', 'hilversum', '3456AB', 'Netherland');
commit;
exit;