
unload to talen_seq_2.fa_line delimiter " "
select ">ID=;Name=" || m.mrkr_abbrev || ";zdb_id="|| m.mrkr_zdb_id || ";~" || s.seq_sequence_2
from marker m
inner join marker_sequence s on s.seq_mrkr_zdb_id = m.mrkr_zdb_id
where m.mrkr_type = 'TALEN'