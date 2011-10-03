-- Script to create an html page that links to all marker pages.

-- download file Case 4693 as reuqested by uniprot
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/ExternalSearch/markers.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/ExternalSearch/markers.txt'
 DELIMITER "	"
select '<a href="/action/marker/view/'||m.mrkr_zdb_id || '" title="'||m.mrkr_name||'">' || m.mrkr_abbrev || '</a><br>' 
from marker m 
order by m.mrkr_type, m.mrkr_abbrev  
;

