------------------------------------------------------------------------
-- FILE: check_thisse_keywords.sql
--
-- After each ZFIN AO update, we check and manually update keyword definition 
-- in Thisse template.
--  
-- INPUT:
--       stageKeyword.unl
-- OUTPUT:
--     	 keywordDead.err
--       stageKeyword.err
------------------------------------------------------------------------

create temp table stage_keyword_tmp (
	t_stg_start	varchar(80) not null,
	t_stg_end 	varchar(80) not null,
	t_keyword 	varchar(80) not null, 
	t_stg_start_zdb_id	varchar(50),
	t_stg_end_zdb_id 	varchar(50),
	t_keyword_zdb_id 	varchar(50)

)with no log;

load from "stageKeyword.unl" insert into stage_keyword_tmp;

update stage_keyword_tmp 
	set t_stg_start_zdb_id = (select stg_zdb_id 
				    from stage
			           where stg_name = t_stg_start);
update stage_keyword_tmp 
	set t_stg_end_zdb_id = (select stg_zdb_id 
				  from stage
			         where stg_name = t_stg_end);
	
update stage_keyword_tmp 
	set t_keyword_zdb_id = (select anatitem_zdb_id 
				  from anatomy_item
			         where anatitem_name = t_keyword);
	
unload to "kwdNameNotPrim.err" 
	select t_stg_start, t_stg_end, t_keyword 
	  from stage_keyword_tmp
	 where t_keyword_zdb_id is null;

delete from stage_keyword_tmp where t_keyword_zdb_id is null;

unload to "kwdStageInconsis.err" 
	select t_stg_start, t_stg_end, t_keyword 
	  from stage_keyword_tmp
	 where anatitem_overlaps_stg_window(t_keyword_zdb_id,t_stg_start_zdb_id,t_stg_end_zdb_id) = "f";
