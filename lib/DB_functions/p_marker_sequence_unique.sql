
create or replace function p_marker_sequence_unique (vseq_mrkr_zdb_id text, vseq_sequence text, vseq_sequence_2 text)
returns void as $$

declare ok boolean;
 objtype varchar(30) := get_obj_type(vseq_mrkr_zdb_id);
 foundId text := (select seq_mrkr_zdb_id from only(marker_sequence) 
				where seq_sequence = vseq_sequence 
                      and seq_sequence_2 = vseq_sequence_2
                      and seq_sequence_2 is not null
                      and get_obj_type(seq_mrkr_Zdb_id) = 'TALEN');

begin
if vseq_sequence_2 is not null
then

	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is not null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'TALEN'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception 'FAIL!: a marker already exists with this sequence!';
	end if;

elsif objtype = 'CRISPR' then
   
	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'CRISPR'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception 'FAIL!: a marker already exists with this sequence!';
	end if;
else 
	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'MRPHLNO'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception 'FAIL!: a marker already exists with this sequence!';
	end if;

end if;
end

$$ LANGUAGE plpgsql
