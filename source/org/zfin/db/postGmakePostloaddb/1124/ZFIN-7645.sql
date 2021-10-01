--liquibase formatted sql
--changeset christian:ZFIN-7645

update marker set mrkr_comments = (
    select replace(mrkr_comments, 'https://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=', '/')
)
where mrkr_comments like '%webdriver%';

update marker set mrkr_comments = (
    select replace(mrkr_comments, 'http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=', '/')
)
where mrkr_comments like '%webdriver%';

update marker set mrkr_comments = (
    select replace(mrkr_comments, '/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=HTTP://ZFIN.ORG/CGI-BIN/WEBDRIVER?MIVAL=AA-PUBVIEW2.APG&OID=', '/')
)
where mrkr_comments like '%webdriver%';
