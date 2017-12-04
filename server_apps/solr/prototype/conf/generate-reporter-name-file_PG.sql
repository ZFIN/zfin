copy (
select mrkr_abbrev
from marker
where substring(mrkr_zdb_id from 1 for 7) = 'ZDB-EFG' ) to 'reporter-names.txt' delimiter '|'
;