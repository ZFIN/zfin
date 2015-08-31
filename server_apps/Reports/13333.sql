select dnote_data_zdb_id, dnote_text, seq_sequence, seq_sequence_2
from data_note
inner join marker_sequence on dnote_data_zdb_id = seq_mrkr_zdb_id
where (dnote_text like '%' || seq_sequence || '%' or dnote_text like '%' || seq_sequence_2 || '%')
and dnote_text like 'Reported Sequence:%'
and (dnote_text like '%reverse%' or dnote_text like '%complement%')

;


