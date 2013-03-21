--
begin work;

-- priority
-- xpat = 128, si: = 64 zgc: = 32,  wu: = 4

create table nomenclature_candidate (
    nc_mrkr_zdb_id varchar(50),
    nc_mrkr_abbrev varchar(25),
    nc_acc_num varchar(20),
    nc_seq_type varchar(20),
    nc_seq_db  varchar (20),
    nc_priority integer
);


delete from nomenclature_candidate;

! echo "find unnamed unm_sa genes";

select distinct mrkr_zdb_id, mrkr_abbrev,0 priority
 from marker
 where mrkr_type[1,4] = 'GENE'
 and  mrkr_abbrev like "unm_sa%"  --or mrkr_name like  "unm_sa%")
 and not exists (
 	select 1 from orthologue
 	where mrkr_zdb_id  = c_gene_id
 )
 into temp tmp_xpat_genes with no log;


! echo "find the longest protein associated with each gene"

select mrkr_zdb_id, g.mrkr_abbrev as mrkr_abbrev, ensm_ensdarp_id as dblink_acc_num, g.priority as priority, "Ensembl" db_name,"Polypeptide" fdata_type
 from   tmp_xpat_genes g, db_link, ensdar_mapping
 where  g.mrkr_zdb_id  =  dblink_linked_Recid
 and dblink_acc_num = ensm_ensdarg_id
 into temp tmp_can_pp with no log
;

unload to missingENSDARPs.unl
select * from tmp_can_pp where dblink_acc_num = 'NULL';

delete from tmp_can_pp
  where dblink_acc_num = 'NULL';


insert into nomenclature_candidate(
    nc_mrkr_zdb_id,
    nc_mrkr_abbrev,
    nc_acc_num,
    nc_seq_type,
    nc_seq_db,
    nc_priority
)  select
    mrkr_zdb_id,
    mrkr_abbrev,
    dblink_acc_num,
    fdata_type,
    db_name,
    priority
 from  tmp_can_pp

 --order by priority DESC
 ;
{
-- xpat+si 		= 192 
-- xpat+si+zgc  = 224  
-- zpat+zgc    == 160

}



---------------------------------------------------------------------------
-- #######################################################################
---------------------------------------------------------------------------

! echo "select_nomenclature_candidate.sql -> nomenclature_candidate_pp.unl"
unload to 'nomenclature_candidate_pp.unl'
 select  *
 --nc_mrkr_zdb_id, nc_priority
 from nomenclature_candidate
 where nc_seq_type = 'Polypeptide'
 --and nc_priority >= 160
 order by nc_priority,nc_acc_num,nc_mrkr_zdb_id,nc_seq_type desc
;

drop table nomenclature_candidate;

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

! echo "this roll back is expected"
--
--rollback work;
commit work;
