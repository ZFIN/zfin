------------------------------------------------------
-- FILE: verify_stage_abbrev.sql
-- 
-- INPUT:
--       AO_translation.unl
-- OUPUT:
--       None
-- EFFECT:
--       Verify the stage abbreviation in AO_translation.unl
-----------------------------------------------------


create temp table anatitem_stg_change_tmp (
        t_oldanat_zdb_id	varchar(50),
	t_start_stg_zdb_id	varchar(50),
	t_end_stg_zdb_id	varchar(50),
	t_newanat_zdb_id 	varchar(50)
) with no log;

load from "AO_translation.unl" insert into anatitem_stg_change_tmp;

select * 
  from anatitem_stg_change_tmp
 where t_start_stg_zdb_id not in (select stg_abbrev from stage);

select * 
  from anatitem_stg_change_tmp
 where t_end_stg_zdb_id not in (select stg_abbrev from stage);
