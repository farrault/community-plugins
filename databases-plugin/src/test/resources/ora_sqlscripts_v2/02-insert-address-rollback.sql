WHENEVER SQLERROR EXIT SQL.SQLCODE

delete from address;
commit;
exit;