WHENEVER SQLERROR EXIT SQL.SQLCODE

delete from person;
commit;
exit;