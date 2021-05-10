--liquibase formatted sql
--changeset christian:ZARC-2247

update linkage set lnkg_comments = replace(lnkg_comments, 'http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=', 'http://zfin.org/')
where lnkg_comments like '%.apg%';

update linkage set lnkg_comments = replace(lnkg_comments, '/cgi-bin/webdriver?MIval=aa-crossview.apg&OID=', '/action/mapping/pnael-detail/')
where lnkg_comments like '%.apg%';

