------------------------------------------------------
-- FILE: make_annot_translation.sql
-- 
-- INPUT:
--       AO_translation.unl
-- OUPUT:
--       None
-- EFFECT:
--       Adjust annotation in expression_result table
-----------------------------------------------------


create temp table anatitem_stg_change_tmp (
        t_oldanat_zdb_id	varchar(50),
	t_start_stg_zdb_id	varchar(50),
	t_end_stg_zdb_id	varchar(50),
	t_newanat_zdb_id 	varchar(50)
);

load from "AO_translation.unl" insert into anatitem_stg_change_tmp;

execute function batch_xpat_annot_adjust();

drop table anatitem_stg_change_tmp;
