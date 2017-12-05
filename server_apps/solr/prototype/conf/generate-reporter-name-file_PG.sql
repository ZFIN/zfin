copy (
select mrkr_abbrev
from marker
where substring(mrkr_zdb_id from 1 for 7) = 'ZDB-EFG' ) to '<!--|TARGETROOT|-->/server_apps/solr/prototype/conf/reporter-names.txt' delimiter '|'
;