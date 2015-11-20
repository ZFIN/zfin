create procedure p_marker_sequence_unique (vseq_mrkr_zdb_id varchar(50), vseq_sequence varchar(255), vseq_sequence_2 varchar(255))

define ok boolean;
define objtype varchar(30);

let objtype = get_obj_type(vseq_mrkr_zdb_id);

if exists (select 'x' from marker_sequence where seq_sequence = vseq_sequence
   	  	      and seq_sequence_2 = vseq_sequence_2
		      and get_obj_type(seq_mrkr_Zdb_id) = objtype)
 then 
 raise exception -746,0,'FAIL!: a marker already exists with this sequence!';
end if;


end procedure;