create trigger marker_sequence_update_trigger 
  update of seq_mrkr_Zdb_id, seq_sequence, seq_sequence_2
  on marker_sequence
  referencing new as new_seq
    for each row (
        execute procedure p_marker_sequence_unique(new_seq.seq_mrkr_zdb_id, new_seq.seq_sequence, new_seq.seq_sequence_2)
);