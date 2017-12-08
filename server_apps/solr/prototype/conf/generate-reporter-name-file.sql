copy (
select mrkr_abbrev
from marker
where mrkr_zdb_id[1,7] = 'ZDB-EFG' ) to '@TARGETROOT@/server_apps/solr/prototype/conf/reporter-names.txt' delimiter '|'
;
