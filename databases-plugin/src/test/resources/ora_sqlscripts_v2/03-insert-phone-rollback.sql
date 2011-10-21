WHENEVER SQLERROR EXIT SQL.SQLCODE

delete from phone;
commit;
exit;