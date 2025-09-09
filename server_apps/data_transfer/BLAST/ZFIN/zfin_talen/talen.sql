-- Keeping for historical purposes
-- This file isn't used anymore (uses informix syntax)
unload to /opt/zfin/www_homes/zfin.org/server_apps/data_transfer/BLAST/ZFIN/zfin_talen/zfin_talen.fa DELIMITER " " select ">lcl|",mrkr_zdb_id||" sequence1",mrkr_name||"|", "
"||seq_sequence
from marker, marker_sequence
 where mrkr_zdb_id = seq_mrkr_zdb_id
 and mrkr_zdb_id like "ZDB-TALEN%"
union
select ">lcl|",mrkr_zdb_id||" sequence2",mrkr_name||"|", "
"||seq_sequence_2
from marker, marker_sequence
 where mrkr_zdb_id = seq_mrkr_zdb_id
 and mrkr_zdb_id like "ZDB-TALEN%";