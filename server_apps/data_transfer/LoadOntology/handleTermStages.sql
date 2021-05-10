!echo "populate the TERM_STAGE table with new start/end stages";

create temp table  term_stage_temp  (
	term_zdb_id_temp varchar(50),
	start_zdb_id_temp varchar(50),
	end_zdb_id_temp varchar(50)
) with no log ;

 select termrel_term_2_zdb_id, stage.stg_zdb_id,  '' from tmp_zfin_rels, stage as stage, term as term
  where termrel_type = 'start stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = termrel_term_1_zdb_id
  AND exists (
    select 'x' from stage, term
    where
    term_zdb_id = termrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    );

-- insert start stage info
insert into term_stage_temp
 select termrel_term_2_zdb_id, stage.stg_zdb_id,  '' from tmp_zfin_rels, stage as stage, term as term
  where termrel_type = 'start stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = termrel_term_1_zdb_id
  AND exists (
    select 'x' from stage, term
    where
    term_zdb_id = termrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    );

 select termrel_term_2_zdb_id, stage.stg_zdb_id,  '' from tmp_zfin_rels, stage as stage, term as term
  where termrel_type = 'end stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = termrel_term_1_zdb_id
  AND exists (
    select 'x' from stage, term
    where
    term_zdb_id = termrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    );

-- update end stage info
update term_stage_temp
 set end_zdb_id_temp = (
 select stage.stg_zdb_id from tmp_zfin_rels, stage as stage, term as term
  where termrel_type = 'end stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = termrel_term_1_zdb_id AND
    term_zdb_id_temp = termrel_term_2_zdb_id AND
   exists (
    select 'x' from stage, term
    where
    term_zdb_id = termrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    ) );

--insert into term_stage
 select term_zdb_id_temp, start_zdb_id_temp,end_zdb_id_temp
 from term_stage_temp
 where not exists (
  select 'x' from term_stage as tt
  where tt.ts_term_zdb_id = term_zdb_id_temp
  );

insert into term_stage
 select term_zdb_id_temp, start_zdb_id_temp,end_zdb_id_temp
 from term_stage_temp
 where not exists (
  select 'x' from term_stage as tt
  where tt.ts_term_zdb_id = term_zdb_id_temp
  );

select count(*) from term_stage;
