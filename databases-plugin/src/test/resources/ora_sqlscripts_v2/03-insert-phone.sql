WHENEVER SQLERROR EXIT SQL.SQLCODE

insert into phone (person_id, address_id, phone_no) values (1, 1, 123455);
insert into phone (person_id, address_id, phone_no) values (2, 2, 2345687);
insert into phone (person_id, address_id, phone_no) values (3, 3, 09345245);
commit;
exit;