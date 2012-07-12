-- load_entrez.sql
{
The point of the Entrez Gene load is to maintain "extra" links from Zfin 
gene pages to various NCBI databases that may have their own load process.

so these links
	Are only on ZFIN genes with Entrez Gene links. 
	Strive to not shadow existing links, directly or pulled from clones.
	Are good to be stolen by other scripts/curation.

}

begin work;

-- Entrez Gene & ZFIN
create table entrez_zdbid (
	ez_eid   varchar(10) not null primary key,
	ez_zdbid varchar(30) not null,
	--ez_chr   varchar(3) not null, -- is multiple sometimes
	ez_type  varchar(25) not null
);

load from 'entrezid_zdbid_lg_type.unl' 
 insert into entrez_zdbid;

-- GenBank
create table entrez_gb (
	eg_eid varchar(10) not null,
	eg_rna varchar(16),
	eg_aa  varchar(16),
	eg_dna varchar(16) 
);

load from 'entrezid_GBnt_GBaa_GBdna.tab' delimiter "	"
 insert into entrez_gb;

-- RefSeq
create table entrez_rs (
	er_eid varchar(10) not null,
	er_rs  varchar(16),
	er_rp  varchar(16)
);

load from 'entrezid_refseq_refpept.tab' delimiter "	"
 insert into entrez_rs;

-- UniGene
create table entrez_ug (
	eu_eid varchar(10) not null,
	eu_ug  varchar(16) not null
);

load from 'entrezid_ugc.tab' delimiter "	"
 insert into entrez_ug;

------------------------------------------------------------------------
-- Indexes
create index entrez_zdbid_ez_zdbid_idx on entrez_zdbid(ez_zdbid) in idxdbs3;
create index entrez_gb_eg_eid_idx on entrez_gb(eg_eid) in idxdbs2;
create index entrez_rs_er_eid_idx on entrez_rs(er_eid) in idxdbs1;
create index entrez_ug_eu_eid_idx on entrez_ug(eu_eid) in idxdbs3;
create index entrez_ug_eu_ug_idx  on entrez_ug(eu_ug)  in idxdbs2;
create index entrez_rs_er_rs_idx  on entrez_rs(er_rs)  in idxdbs1;
create index entrez_rs_er_rp_idx  on entrez_rs(er_rp)  in idxdbs3;
create index entrez_gb_eg_rna_idx on entrez_gb(eg_rna) in idxdbs2;
create index entrez_gb_eg_aa_idx  on entrez_gb(eg_aa)  in idxdbs1;
create index entrez_gb_eg_dna_idx on entrez_gb(eg_dna) in idxdbs3;

! echo "update Entrez zdbids from zdb_replaced_data"
  
update entrez_zdbid set ez_zdbid = (
	select zrepld_new_zdb_id from zdb_replaced_data 
	 where ez_zdbid == zrepld_old_zdb_id
)
where exists (
	select 't' from zdb_replaced_data 
	 where ez_zdbid == zrepld_old_zdb_id
);

! echo "Are any of entrez zdbid unknown"
select ez_zdbid very_bad 
 from entrez_zdbid 
 where not exists (
	select 't' from marker 
	 where ez_zdbid == mrkr_zdb_id
	  and mrkr_zdb_id[1,8] ==  "ZDB-GENE"
);
delete from entrez_zdbid 
 where not exists (
	select 't' from marker 
	 where ez_zdbid == mrkr_zdb_id
	  and mrkr_zdb_id[1,8] ==  "ZDB-GENE"
);

! echo "what Existing EntrezGene links are not in this load?"
select dblink_linked_recid[1,25] geneid, dblink_acc_num[1,20] entrezid  
 from db_link 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "EntrezGene"
   and not exists (
	select 't' from entrez_zdbid 
	 where dblink_acc_num == ez_eid
	   and dblink_linked_recid == ez_zdbid
);

! echo "Delete dropped Entrez Gene links ******************************"
delete from zdb_active_data where exists (
	select 't' from db_link 
	join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	where fdb_db_name == "EntrezGene"
	and not exists (
		select 't' from entrez_zdbid 
		 where dblink_acc_num == ez_eid
		   and dblink_linked_recid == ez_zdbid
	)
   and dblink_zdb_id == zactvd_zdb_id	
);
-- could delete other links based on 
-- their load attribuion and being gene w/o EntrezGene link
-- or use as later check 

! echo "what Entrez Gene links are oddly attributed"
select dblink_linked_recid[1,25],dblink_acc_num[1,20], 
		recattrib_source_zdb_id[1,25], dblink_info
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
  join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "EntrezGene"
  and recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
;

! echo "Are there any other NCBI links attributed to dropped Entrez?"
--######################################################################
! echo "*** Delete dropped UniGene links attributed to NCBI Gene load ***"
select dblink_zdb_id, dblink_linked_recid, dblink_acc_num 
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "UniGene"
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
   and not exists (
	select 't' from entrez_zdbid 
	 join entrez_ug on ez_eid == eu_eid
	 where dblink_linked_recid == ez_zdbid 
	   and dblink_acc_num == eu_ug
 ) into temp tmp_ug with no log
;

-- To check if there may be an error in what is to be deleated



--######################################################################
! echo "*** Delete dropped RefSeq links attributed to NCBI Gene load ***"
select dblink_zdb_id, dblink_linked_recid, dblink_acc_num 
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "RefSeq"
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
   and not exists (
	select 't' from entrez_zdbid 
	 join entrez_rs on ez_eid == er_eid
	 where dblink_linked_recid == ez_zdbid 
	   and (dblink_acc_num == er_rs OR dblink_acc_num == er_rp)
 ) into temp tmp_rs with no log
;

! echo "Unmaintainable links, see your DBA"
select dblink_linked_recid[1,25], dblink_acc_num[1,25] 
 from tmp_rs 
 join expression_experiment on xpatex_dblink_zdb_id == dblink_zdb_id
;
-- the stupid ... it burns.
delete from tmp_rs where exists (
	select 't' from  expression_experiment 
	where xpatex_dblink_zdb_id == dblink_zdb_id
);

! echo "Remove unsupported attributions"
delete from record_attribution where exists (
	select 't' from tmp_rs 
	 where dblink_zdb_id == recattrib_data_zdb_id 
	   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"
);

! echo "Avoid dropping links with other attribution" 
delete from tmp_rs where exists (
	select 't' from  record_attribution  
	where recattrib_data_zdb_id == dblink_zdb_id
	  and recattrib_source_zdb_id != "ZDB-PUB-020723-3" 
);

delete from zdb_active_data where exists (
	select 't' from tmp_rs where dblink_zdb_id == zactvd_zdb_id
);
-- To check if there may be an error in what is deleated
! echo "Spot check deleted RefSeq"
select first 50 dblink_linked_recid[1,25], dblink_acc_num[1,25] from tmp_rs;
drop table tmp_rs;

! echo "What RefSeq links are oddly attributed"
select dblink_linked_recid[1,25],dblink_acc_num[1,25], 
	   recattrib_source_zdb_id[1,25], dblink_info
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "RefSeq"
  and recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
;

--######################################################################
! echo "*** Delete dropped GenBank links attributed to NCBI Gene load ***"
select dblink_zdb_id, dblink_linked_recid, dblink_acc_num 
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "GenBank"
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
   and not exists (
	select 't' from entrez_zdbid 
	 join entrez_rs on ez_eid == er_eid
	 where dblink_linked_recid == ez_zdbid 
	   and (dblink_acc_num == er_rs OR dblink_acc_num == er_rp)
 ) into temp tmp_gb with no log
;

! echo "Unmaintainable links, see your DBA"
select dblink_linked_recid[1,25], dblink_acc_num[1,25] 
 from tmp_gb 
 join expression_experiment on xpatex_dblink_zdb_id == dblink_zdb_id
;
-- the stupid ... it burns.
delete from tmp_gb where exists (
	select 't' from  expression_experiment 
	where xpatex_dblink_zdb_id == dblink_zdb_id
);

! echo "Remove unsupported attributions"
delete from record_attribution where exists (
	select 't' from tmp_gb 
	 where dblink_zdb_id == recattrib_data_zdb_id 
	   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"
);

! echo "Avoid dropping links with other attribution" 
delete from tmp_gb where exists (
	select 't' from  record_attribution  
	where recattrib_data_zdb_id == dblink_zdb_id
	  and recattrib_source_zdb_id != "ZDB-PUB-020723-3" 
);

delete from zdb_active_data where exists (
	select 't' from tmp_gb where dblink_zdb_id == zactvd_zdb_id
);
-- To check if there may be an error in what is deleated
! echo "Spot check deleted GenBank"
select first 50 dblink_linked_recid[1,25], dblink_acc_num[1,25] from tmp_gb;

drop table tmp_gb;
{
 --Genbank accessions are used so widely this check is pointless
! echo "what GenBank links are oddly attributed"
select dblink_linked_recid[1,25],dblink_acc_num[1,20], 
	   recattrib_source_zdb_id[1,25], dblink_info
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name == "GenBank"
  and recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
;
}

--######################################################################

! echo "---------------------------------------------------------------"
! echo "Delete incomming UniGene unmappable to zfin genes"
delete from entrez_ug where not exists (
	select 't' from entrez_zdbid where ez_eid == eu_eid
);
select count(*) ug_remaining from entrez_ug;

! echo "Delete incomming RefSeq unmappable to zfin genes"
delete from entrez_rs where not exists (
	select 't' from entrez_zdbid where ez_eid == er_eid
);
select count(*) rs_remaining from entrez_rs;

! echo "Delete incomming GenBank unmappable to zfin genes"
delete from entrez_gb where not exists (
	select 't' from entrez_zdbid where ez_eid == eg_eid
);
select count(*) gb_remaining from entrez_gb;

! echo "---------------------------------------------------------------"

! echo "Delete incomming UniGene already mapped to zfin genes via Entrez"
delete from entrez_ug where exists (
	select 't' from entrez_zdbid 
	 join db_link on dblink_linked_recid == ez_zdbid
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where ez_eid == eu_eid
	   and dblink_acc_num == eu_ug
	   and fdb_db_name == "UniGene"
);


! echo "Delete incomming RefSeq already mapped to zfin genes via Entrez"
delete from entrez_rs where exists (
	select 't' from entrez_zdbid 
	 join db_link on dblink_linked_recid == ez_zdbid
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where  ez_eid == er_eid
	   and (dblink_acc_num == er_rs OR 
	        dblink_acc_num == er_rp)
	   and fdb_db_name == "RefSeq"
);


! echo "Delete incomming GenBank already mapped to zfin genes via Entrez"
delete from entrez_gb where exists (
	select 't' from entrez_zdbid 
	 join db_link on dblink_linked_recid == ez_zdbid
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where ez_eid == eg_eid
	   and (dblink_acc_num == eg_rna OR 
	        dblink_acc_num == eg_aa  OR 
	        dblink_acc_num == eg_dna)
	   and fdb_db_name in ("GenBank","GenPept")
);
select count(*) gb_remaining from entrez_gb;

! echo "---------------------------------------------------------------"

-- mappings via associated marker

! echo "Are there any UniGene associated with Non-genes in ZFIN?"
	select count(*) be_zero from db_link 
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where dblink_linked_recid[1,8] != "ZDB-GENE"
	   and fdb_db_name == "UniGene"
;
	   
! echo "Are there any RefSeq associated with Non-genes in ZFIN?"	   
	select count(*) be_zero from db_link 
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where dblink_linked_recid[1,8] != "ZDB-GENE"
	   and fdb_db_name == "RefSeq"
;

! echo "Delete incomming GenBank already mapped to zfin genes via markers"
-- unless they are transcript evidence
delete from entrez_gb where exists (
	select 't' from entrez_zdbid 
	 join marker_relationship  on mrel_mrkr_1_zdb_id  == ez_zdbid 
	 join db_link on dblink_linked_recid == mrel_mrkr_2_zdb_id
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where ez_eid == eg_eid
	   and (dblink_acc_num == eg_rna OR 
	        dblink_acc_num == eg_aa  OR 
	        dblink_acc_num == eg_dna)
	   and fdb_db_name in ("GenBank","GenPept")
	   and dblink_linked_recid[1,8] not in("ZDB-GENE","ZDB-TRAN")
);
! echo "###############################################################"
! echo "delete existing Entrez attributed links that shadow other links"

select dblink_zdb_id, dblink_linked_recid mrkr, dblink_acc_num acc, dblink_fdbcont_zdb_id fdb
 from db_link entrez 
 join record_attribution on entrez.dblink_zdb_id == recattrib_data_zdb_id
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 where fdb_db_name not in ("EntrezGene","UniGene")
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3" 
  into temp tmp_entrez with no log
;

! echo "drop redundant attributions"
select distinct recattrib_data_zdb_id 
 from record_attribution 
 where recattrib_source_zdb_id != "ZDB-PUB-020723-3"
   and exists (
	select 't' from tmp_entrez 
	 where recattrib_data_zdb_id == mrkr
) into temp dup_attrib with no log
;

delete from record_attribution 
 where recattrib_source_zdb_id == "ZDB-PUB-020723-3"
   and exists (
	select 't' from dup_attrib 
	 where dup_attrib.recattrib_data_zdb_id == record_attribution.recattrib_data_zdb_id
);
drop table dup_attrib;

! echo "spare any up for deletion that have alternative attribution"
delete from tmp_entrez where exists ( 
	select 't' from  record_attribution 
	 where recattrib_source_zdb_id != "ZDB-PUB-020723-3"
	   and recattrib_data_zdb_id == dblink_zdb_id
);

! echo "---------------------------------------------------------------"
! echo "drop redundant gene-based shadow links "

select distinct dblink_acc_num dl_mrkr ,dblink_acc_num dl_acc, dblink_fdbcont_zdb_id dl_fdb
 from db_link
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id
 join tmp_entrez on mrkr == dblink_linked_recid
 where recattrib_source_zdb_id != "ZDB-PUB-020723-3"
   and dblink_acc_num == acc
   and dblink_fdbcont_zdb_id == fdb
 into temp dup_link with no log
;

delete from zdb_active_data where exists (
	select 't' 
	 from tmp_entrez join dup_link on dl_acc == acc
	 where dl_mrkr == mrkr
	   and dl_fdb == fdb
	   and dblink_zdb_id == zactvd_zdb_id
);

drop table dup_link;

! echo "---------------------------------------------------------------"
! echo "drop redundant marker-based shadow links"

select distinct dblink_acc_num dl_mrkr ,dblink_acc_num dl_acc, dblink_fdbcont_zdb_id dl_fdb
 from db_link
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id
 join marker_relationship on mrel_mrkr_2_zdb_id ==  dblink_linked_recid
 join tmp_entrez on mrkr ==  mrel_mrkr_1_zdb_id 
 where recattrib_source_zdb_id != "ZDB-PUB-020723-3"
   and dblink_linked_recid[1,8] != "ZDB-GENE"
   and dblink_acc_num == acc
   and dblink_fdbcont_zdb_id == fdb
 into temp dup_link with no log
;

delete from zdb_active_data where exists (
	select 't' 
	 from tmp_entrez join dup_link on dl_acc == acc
	 where dl_mrkr == mrkr
	   and dl_fdb == fdb
	   and dblink_zdb_id == zactvd_zdb_id
);

drop table dup_link;
drop table tmp_entrez;

! echo "###############################################################"
! echo "what is left may be added as new links on genes "
! echo "attribibuted to the NCBI Gene Load"

select count(*) ug_remaining from entrez_ug;
select count(*) rs_remaining from entrez_rs where er_rs != "-";
select count(*) rp_remaining from entrez_rs where er_rp != "-";
select count(*) gb_rna_remaining from entrez_gb where eg_rna != "-";
select count(*) gb_aa_remaining from entrez_gb where eg_aa != "-";
select count(*) gb_dna_remaining from entrez_gb where eg_dna != "-";








 
! echo "###############################################################"
drop table  entrez_zdbid; 
drop table  entrez_gb; 
drop table  entrez_rs; 
drop table  entrez_ug; 


rollback work;

