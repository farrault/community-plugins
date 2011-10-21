WHENEVER SQLERROR EXIT SQL.SQLCODE

delete from person where id = 4;
commit;
exit;
