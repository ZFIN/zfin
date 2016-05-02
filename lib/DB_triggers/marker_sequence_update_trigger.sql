create trigger marker_sequence_insert_trigger 
  insert on marker_sequence
  referencing new as new_seq
     for each row (
         execute procedure p_marker_sequence_unique (new_seq.seq_mrkr_zdb_id, new_seq.seq_sequence, new_seq.seq_sequence_2));