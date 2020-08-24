begin work;

select tscript_mrkr_zdb_id as tscriptid
into tmp_tscriptid
from transcript, probe_library, genotype, marker_relationship,clone
where  clone_probelib_zdb_id=probelib_zdb_id
and probelib_strain_zdb_id=geno_zdb_id
and geno_display_name='AB'
and mrel_mrkr_1_zdb_id=clone_mrkr_zdb_id
and mrel_mrkr_2_zdb_id=tscript_mrkr_zdb_id
and mrel_type='clone contains transcript';

select count(*) from tmp_tscriptid;

select tscript_mrkr_zdb_id
from transcript
where tscript_load_id like '%OTTDART%'
and tscript_ensdart_id  is null
and tscript_status_id!=1
and tscript_mrkr_zdb_id not in (Select tscriptid from tmp_tscriptid); 

select tscript_mrkr_zdb_id
from transcript
where tscript_load_id like '%OTTDART%'
and tscript_ensdart_id  is null
and tscript_status_id is null
and tscript_mrkr_zdb_id not in (Select tscriptid from tmp_tscriptid); 
rollback work;
