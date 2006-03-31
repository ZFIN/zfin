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

-- update old anatomy obo id to zdb id 
update anatitem_stg_change_tmp
   set t_oldanat_zdb_id = (select anatitem_zdb_id
                             from anatomy_item
                            where anatitem_obo_id = t_oldanat_zdb_id)
 where t_oldanat_zdb_id in (select anatitem_obo_id 
                              from anatomy_item);

-- update new anatomy obo id to zdb id 
update anatitem_stg_change_tmp
   set t_newanat_zdb_id = (select anatitem_zdb_id
                             from anatomy_item
                            where anatitem_obo_id = t_newanat_zdb_id)
 where t_newanat_zdb_id in (select anatitem_obo_id 
                              from anatomy_item);

-- update start stage abbrev to stage id
select * 
  from anatitem_stg_change_tmp
 where t_start_stg_zdb_id not in (select stg_abbrev from stage);

update anatitem_stg_change_tmp 
   set t_start_stg_zdb_id = (select stg_zdb_id 
                               from stage 
                              where stg_abbrev = t_start_stg_zdb_id)
 where t_start_stg_zdb_id in (select stg_abbrev
                                from stage);

-- update end stage abbrev to stage id
select * 
  from anatitem_stg_change_tmp
 where t_end_stg_zdb_id not in (select stg_abbrev from stage);

update anatitem_stg_change_tmp 
   set t_end_stg_zdb_id = (select stg_zdb_id 
                             from stage 
                            where stg_abbrev = t_end_stg_zdb_id)
 where t_end_stg_zdb_id in (select stg_abbrev
                                from stage);
 
execute function batch_xpat_annot_adjust();

drop table anatitem_stg_change_tmp;
