
create procedure p_marker_sequence_unique (vseq_mrkr_zdb_id varchar(50), vseq_sequence varchar(255), vseq_sequence_2 varchar(255))


define ok boolean;
define objtype varchar(30);
define foundId varchar(50);
--trace on;
let objtype = get_obj_type(vseq_mrkr_zdb_id);
let foundid = (select seq_mrkr_zdb_id from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is not null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'TALEN');

if vseq_sequence_2 is not null
then

	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is not null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'TALEN'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception -746,0,'FAIL!: a marker already exists with this sequence!';
	end if;

elif objtype = 'CRISPR' then
   
	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'CRISPR'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception -746,0,'FAIL!: a marker already exists with this sequence!';
	end if;
else 
	if exists (select 'x' from only(marker_sequence) where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and seq_sequence_2 is null
		      and get_obj_type(seq_mrkr_Zdb_id) = 'MRPHLNO'
		      and foundid != seq_mrkr_Zdb_id)
        then 
 	     raise exception -746,0,'FAIL!: a marker already exists with this sequence!';
	end if;

end if;

end procedure;
