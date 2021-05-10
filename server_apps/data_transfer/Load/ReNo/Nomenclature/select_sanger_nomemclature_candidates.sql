--
begin work;

create table nomenclature_candidate (
    nc_mrkr_zdb_id text,
    nc_mrkr_abbrev varchar(25),
    nc_acc_num varchar(20),
    nc_seq_type varchar(20),
    nc_seq_db  varchar (20),
    nc_priority integer
);


delete from nomenclature_candidate;

\echo 'find unnamed x:genes hopefuly with expression patterns';
\echo 'and no orthology; added 2007 Nov 6';

create temp table tmp_xpat_genes (mrkr_zdb_id text, mrkr_abbrev varchar(100), priority int)
;

insert into tmp_xpat_genes(mrkr_zdb_id, mrkr_abbrev, priority)
select distinct mrkr_zdb_id, mrkr_abbrev,160 priority
 from marker
 where substring(mrkr_type,1,4) = 'GENE'
 and  mrkr_abbrev like 'zmp:%' ; --or mrkr_name like  '% like');

insert into tmp_xpat_genes (mrkr_Zdb_id, mrkr_Abbrev, priority)
select mrkr_zdb_id, mrkr_abbrev, 160
  from marker
 where mrkr_abbrev like '%:%' 
 and mrkr_type = 'GENE'
 and mrkr_Abbrev not like 'zmp:%'
 and exists (Select 'x' from feature_marker_relationship, feature
     	    	    where fmrel_Ftr_zdb_id = feature_zdb_id
		    and fmrel_mrkr_zdb_id = mrkr_Zdb_id
		    and feature_abbrev like 'sa%');


\copy (select mrkr_abbrev from tmp_xpat_genes) to 'candidatesTest.txt'; 

\echo 'find the longest protein associated with each gene'


create temp table tmp_can_pp (mrkr_zdb_id text,
       	    	  	      mrkr_abbrev varchar(100),
			      dblink_acc_num varchar(100),
			      dblink_length int,	
			      priority int,
			      db_name varchar(100),
			      fdata_type varchar(100))
;

insert into tmp_can_pp (mrkr_zdb_id, mrkr_abbrev, dblink_acc_num, dblink_length, priority, db_name, fdata_type)
select mrkr_zdb_id, mrkr_abbrev, ensp_ensdarp_id , ensp_length, 160, 'Ensembl','Polypeptide'
  from marker, ensdarg_ensdarp_mapping, db_link
  where mrkr_zdb_id = dblink_linked_recid
 and dblink_acc_num = ensp_ensdarg_id
 and (mrkr_Abbrev like 'zmp%'
  or ((mrkr_abbrev like '%:%') and exists (Select 'x' from feature_marker_relationship, feature
     	    	    where fmrel_Ftr_zdb_id = feature_zdb_id
		    and fmrel_mrkr_zdb_id = mrkr_Zdb_id
		    and feature_abbrev like 'sa%'))
      );




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
 where priority between 150  AND 223; -- TWIDDEL THIS

 --order by priority DESC


---------------------------------------------------------------------------
-- #######################################################################
---------------------------------------------------------------------------

\echo 'select_nomenclature_candidate.sql -> nomenclature_candidate_pp.unl'
\copy (select * from nomenclature_candidate where nc_seq_type = 'Polypeptide' order by nc_priority,nc_acc_num,nc_mrkr_zdb_id,nc_seq_type desc) to 'sanger_mutant_nomenclature_candidate_pp.unl';

drop table nomenclature_candidate;

\echo 'this roll back is expected';

rollback work;

