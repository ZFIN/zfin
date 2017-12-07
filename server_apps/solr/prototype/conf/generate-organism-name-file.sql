copy (
select organism_common_name
from organism ) to '<!--|TARGETROOT|-->/server_apps/solr/prototype/conf/organism-name.txt' delimiter '|'
;
