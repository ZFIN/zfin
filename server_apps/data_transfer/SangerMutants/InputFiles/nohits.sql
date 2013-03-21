begin work;

unload to 'nohits.unl' select distinct  cnd_suggested_name
from candidate, run_candidate, blast_hit, blast_query, run
where bhit_alignment is null
and bqry_zdb_id=bhit_bqry_zdb_id
and bqry_runcan_zdb_id=runcan_zdb_id
and runcan_cnd_zdb_id=cnd_zdb_id
and runcan_run_zdb_id = run_zdb_id
and run_name like 'Sanger%';


create temp table sangerInput (feature varchar(50), ensdargID varchar(50), background varchar(3)) with no log;

load from sangerEnsdarg.unl insert into sangerInput;

create temp table noHits (nohitEnsdarg varchar(50)) with no log;

load from nohits.unl insert into noHits;

unload to removeFromRenoRun.unl select distinct feature, ensdargID from sangerInput, noHits where ensdargID=nohitEnsdarg;

create temp table tmp_deleteFromReno (feature varchar(50), ensdargID varchar(50)) with no log;

load from removeFromRenoRun.unl insert into tmp_deleteFromReno;

select runcan_zdb_id as id from run_Candidate, run, candidate, blast_query, blast_hit
where runcan_cnd_zdb_id = cnd_zdb_id
and bqry_runcan_zdb_id = runcan_zdb_id
and bhit_bqry_zdb_id = bqry_zdb_id
and runcan_run_zdb_id = run_zdb_id
and run_name like 'Sanger%'
and bhit_alignment is null
into temp tmp_delete;

delete from run_candidate
where exists (Select 'x' from tmp_Delete where id = runcan_Zdb_id);
--rollback work;
commit work;
