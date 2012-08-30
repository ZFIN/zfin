-- load_entrez.sql
{
The point of the Entrez Gene load is to maintain "extra" links from Zfin 
gene pages to various NCBI databases that may have their own load process.

so these links
	Are _only_ on ZFIN genes with Entrez Gene links. 
	Strive to not shadow existing links, directly or pulled from clones.
	Attributions are to good to be stolen by other scripts/curation.

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
 
create table rs_len( 
	rsl_rs varchar(16) PRIMARY KEY not NULL, 
	rsl_len integer not NULL
) 
	in tbldbs3;

load from 'refseq_len.unl' insert into rs_len;

update statistics high for table rs_len; 
 

--  
create table gprp_len( 
	gprpl_gp varchar(16) PRIMARY KEY not NULL, 
	gprpl_len integer not NULL
) 
	in tbldbs3;

load from 'genpeptRP_len.unl' insert into gprp_len;

update statistics high for table gprp_len; 

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

! echo "Update the zdbids Entrez sends us through zdb_replaced_data"
update entrez_zdbid set ez_zdbid = (
	select zrepld_new_zdb_id from zdb_replaced_data 
	 where ez_zdbid == zrepld_old_zdb_id
)
where exists (
	select 't' from zdb_replaced_data 
	 where ez_zdbid == zrepld_old_zdb_id
);

! echo "Are any zdbids Entrez sends to us unknown to us?"
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

! echo "Adopt any unattributed Enrez gene links first"
-- were 11 first time
	
select  dblink_zdb_id zad
 from db_link entrez
 where entrez.dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-1"
  and not exists (
  	select 't' from record_attribution
  	 where entrez.dblink_zdb_id == recattrib_data_zdb_id
) into temp tmp_dblink with no log
;
! echo "attribute to ncbi EntrezGene load"
insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
    --recattrib_source_significance,
    --recattrib_source_type 
)
select zad, "ZDB-PUB-020723-3" from tmp_dblink
;
drop table tmp_dblink;

! echo "What Entrez Gene ID links are not attributed to this load?"	
! echo "curator responsible might want to revisit them"
select dblink_linked_recid[1,25] gene, recattrib_source_zdb_id[1,25] pub, dblink_info blurb, dblink_zdb_id[1,30] zad
 from db_link entrez
 join record_attribution on entrez.dblink_zdb_id == recattrib_data_zdb_id 
 where entrez.dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-1"
   and recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
;	

! echo "Abandon any Entrez attribution on links that have an alternative attribution"

select dblink_zdb_id zad
 from db_link 
 join record_attribution etzrecattr on dblink_zdb_id == etzrecattr.recattrib_data_zdb_id
 join record_attribution othrecattr on dblink_zdb_id == othrecattr.recattrib_data_zdb_id 
 where etzrecattr.recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
   and othrecattr.recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
 into temp tmp_dup_attrib with no log
;

delete from record_attribution 
 where recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
  and exists ( 
  	select 't' from tmp_dup_attrib where recattrib_data_zdb_id == zad
);

drop table tmp_dup_attrib;


! echo "What existing EntrezGene links are not in this load?"
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

! echo "Delete dropped Entrez Gene links "
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
-- or use as later check, which is safer...  

! echo "What existing Entrez Gene links are ... oddly attributed?"
select dblink_linked_recid[1,25],dblink_acc_num[1,20], 
		recattrib_source_zdb_id[1,25], dblink_info
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 where dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-1"
  and recattrib_source_zdb_id != "ZDB-PUB-020723-3"    -- NCBI Gene
;
-- TODO: should this script adopt the odities?


! echo "What new EntrezGene links can we add?"
select ez_zdbid mrkr ,ez_eid acc, get_id("DBLINK") zad
 from entrez_zdbid where not exists (
	select 't' from db_link 
	 where dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-1"
	   and ez_zdbid == dblink_linked_recid
	   and ez_eid ==  dblink_acc_num
) into temp tmp_dbl with no log;

insert into zdb_active_data select zad from tmp_dbl;

insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    --dblink_acc_num_display,
    --dblink_length,
    dblink_fdbcont_zdb_id 
) select mrkr, acc, "uncurated: EntrezGene load " || TODAY, zad, "ZDB-FDBCONT-040412-1"
from tmp_dbl
;

! echo "Attribute to NCBI EntrezGene load"
insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
    --recattrib_source_significance,
    --recattrib_source_type 
)
select zad, "ZDB-PUB-020723-3" from tmp_dbl
;

drop table tmp_dbl;

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

-- TODO check if there may be an error in what is to be deleated
--  spot checks have all been good so far.


--######################################################################
! echo "*** Find dropped RefSeq links attributed to NCBI Gene load ***"
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

! echo "Potentially unmaintainable RefSeq links"
-- there is no point in checking if they need to be maintained. 
select distinct dblink_linked_recid[1,25], dblink_acc_num[1,25] 
 from tmp_rs 
 join expression_experiment on xpatex_dblink_zdb_id == dblink_zdb_id
;

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


! echo "Spot check deleted RefSeq"
select first 50 dblink_linked_recid[1,25], dblink_acc_num[1,25] from tmp_rs;
drop table tmp_rs;
--  spot checks have all been good so far.

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

! echo "Potentially unmaintainable GenBank links"
-- there is no point in checking if they need to be maintained. 
select distinct dblink_linked_recid[1,25], dblink_acc_num[1,25] 
 from tmp_gb 
 join expression_experiment on xpatex_dblink_zdb_id == dblink_zdb_id
;

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
 --Genbank accessions are used widely outside of GenBank so this check is pointless
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

! echo "Delete incomming UniGene correctly mapped to zfin genes via Entrez"
delete from entrez_ug where exists (
	select 't' from entrez_zdbid 
	 join db_link on dblink_linked_recid == ez_zdbid
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where ez_eid == eu_eid
	   and dblink_acc_num == eu_ug
	   and fdb_db_name == "UniGene"
);

! echo "Delete incomming RefSeq correctly mapped to zfin genes via Entrez"
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

! echo "Delete incomming GenBank correctly mapped to zfin genes via Entrez"
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

-- TODO 
-- explore if also exists incorectly mapped for same correct mapping? 

! echo ""
! echo "------------- mappings via associated marker---------------------------"
! echo ""

! echo "Are there any EntrezGene associated with Non-genes in ZFIN?"
	select count(*) be_zero from db_link 
	 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
	 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
	 where dblink_linked_recid[1,8] != "ZDB-GENE"
	   and fdb_db_name == "EntrezGene"
;

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
	   and dblink_linked_recid[1,8] not in("ZDB-GENE","ZDB-TRAN") -- transcript evidence
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
 select 't' from record_attribution
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
 
select distinct dblink_linked_recid dl_mrkr ,dblink_acc_num dl_acc, dblink_fdbcont_zdb_id dl_fdb
 from db_link
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id
 join marker_relationship on mrel_mrkr_2_zdb_id == dblink_linked_recid
 join tmp_entrez on mrkr == mrel_mrkr_1_zdb_id
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

--{ -- just for checking
select count(*) ug_remaining from entrez_ug where eu_ug != "-";
select count(*) rs_remaining from entrez_rs where er_rs != "-";
select count(*) rp_remaining from entrez_rs where er_rp != "na";
select count(*) gb_rna_remaining from entrez_gb where eg_rna != "-";
select count(*) gb_aa_remaining  from entrez_gb where eg_aa  != "-";
select count(*) gb_dna_remaining from entrez_gb where eg_dna != "-";
--}

! echo "remove redundant accessions from GenBank that exist as RefSeq"
! echo "in the incomming load"

update entrez_gb set eg_aa = "-" 
 where exists (
	select 't' from entrez_rs 
	where er_eid == eg_eid
	  and er_rp == eg_aa
);

update entrez_gb set eg_rna = "-" 
 where exists (
	select 't' from entrez_rs 
	where er_eid == eg_eid
	  and er_rs == eg_rna
);

-- hmmm none

! echo ""

-- TODO: do we have information on UniGene being RNA or SeqClusters??

create temp table tmp_dblink (
	mrkr varchar(50), acc varchar(50), zad varchar(50), fdb varchar(50)
);

select ez_zdbid mrkr, eu_ug acc, "ZDB-FDBCONT-040412-43" fdb
 from entrez_zdbid join entrez_ug on ez_eid == eu_eid
union
select ez_zdbid, er_rs, "ZDB-FDBCONT-040412-38"
 from entrez_zdbid join entrez_rs on ez_eid == er_eid
union
select ez_zdbid, er_rp, "ZDB-FDBCONT-040412-39"
 from entrez_zdbid join entrez_rs on ez_eid == er_eid
 where er_rp != "na"
union
select ez_zdbid, eg_rna, "ZDB-FDBCONT-040412-37"
 from entrez_zdbid join entrez_gb on ez_eid == eg_eid
 where eg_rna != "-"
union
select ez_zdbid, eg_aa, "ZDB-FDBCONT-040412-42"
 from entrez_zdbid join entrez_gb on ez_eid == eg_eid
 where eg_aa != "-"
union
select ez_zdbid, eg_dna,"ZDB-FDBCONT-040412-36"
 from entrez_zdbid join entrez_gb on ez_eid == eg_eid
 where eg_dna != "-"
 into temp tmp_dbl with no log
;

insert into tmp_dblink(mrkr,acc,fdb) select * from tmp_dbl;
drop table tmp_dbl;
update tmp_dblink set zad = get_id("DBLINK");

insert into zdb_active_data select zad from tmp_dblink;

insert into db_link (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
    --dblink_acc_num_display,
    --dblink_length,
    dblink_fdbcont_zdb_id 
) select mrkr,acc,"uncurated: EntrezGene load " || TODAY, zad, fdb 
from tmp_dblink
;

! echo "attribute to ncbi EntrezGene load"
insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
    --recattrib_source_significance,
    --recattrib_source_type 
)
select zad, "ZDB-PUB-020723-3" from tmp_dblink
;

! echo "Update NULL dblink lengths with AccessionBank where possible"
update db_link set dblink_length = (
	select  accbk_length 
	 from accession_bank 
	  where accbk_fdbcont_zdb_id == dblink_fdbcont_zdb_id
	    and accbk_acc_num == dblink_acc_num
	    and accbk_length IS NOT NULL
) where dblink_length is NULL 
  and exists (
	select 't' from accession_bank 
	  where accbk_fdbcont_zdb_id == dblink_fdbcont_zdb_id
	    and accbk_acc_num == dblink_acc_num
	    and accbk_length IS NOT NULL
);

! echo "Update changed dblink lengths with AccessionBank where possible"
update db_link set dblink_length = (
	select accbk_length 
	 from accession_bank 
	  where accbk_fdbcont_zdb_id == dblink_fdbcont_zdb_id
	    and accbk_acc_num == dblink_acc_num
	    and accbk_length IS NOT NULL
	    and accbk_length != dblink_length
) where dblink_length is not NULL 
  and exists (
	select 't' from accession_bank 
	  where accbk_fdbcont_zdb_id == dblink_fdbcont_zdb_id
	    and accbk_acc_num == dblink_acc_num
	    and accbk_length IS NOT NULL
	    and accbk_length != dblink_length
);


! echo "What links just added have NULL lengths?"
select --dblink_linked_recid[1,25] mrkr,dblink_acc_num[1,20] acc, fdb_db_name fdb
	fdb_db_name fdb, count(*)
 from db_link 
 join record_attribution on dblink_zdb_id == recattrib_data_zdb_id 
 join foreign_db_contains on dblink_fdbcont_zdb_id == fdbcont_zdb_id
 join foreign_db on fdbcont_fdb_db_id == fdb_db_pk_id
 join  tmp_dblink on zad == dblink_zdb_id
 where fdb_db_name in ("GenBank","GenPept","RefSeq")
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"
   and dblink_length is NULL 
 group by 1
;

drop table tmp_dblink;

! echo "###############################################################"
! echo ""
! echo "These should not exist ..."
! echo "Genes with links attributed the the NCBI gene load" 
! echo "that do not have an Entrez Gene ID (... attributed to this load)"
! echo "to associated with them to map anything to"

select --first 100 
		other.dblink_linked_recid[1,25] mrkr, 
		other.dblink_acc_num[1,20] acc,  
		other.dblink_fdbcont_zdb_id[1,25] fdb,
		other.dblink_zdb_id[1,25] zad
 from db_link other
 join record_attribution on other.dblink_zdb_id == recattrib_data_zdb_id 
 where other.dblink_fdbcont_zdb_id != "ZDB-FDBCONT-040412-1"
   and other.dblink_linked_recid[1,8] == "ZDB-GENE" 
   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
   and not exists (
	select 't'
	 from db_link entrez
	 join record_attribution on entrez.dblink_zdb_id == recattrib_data_zdb_id 
	 where entrez.dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-1"
	   and recattrib_source_zdb_id == "ZDB-PUB-020723-3"    -- NCBI Gene
	   and entrez.dblink_linked_recid == other.dblink_linked_recid
);


-- TODO 
-- a stronger test would isolate others that do not match 
-- the _correct_ EntrezGene ID instead of just any EntrezGene ID.
 
-- could truncate the tables reload and do a comprehensive sanity check

------------------------------------------------------------------------
-- update (all?) RefSeq Lengths

! echo "RefSeq had null length"
update db_link set dblink_length = (
	select  rsl_len  from rs_len where rsl_rs == dblink_acc_num
)
where dblink_length is NULL
 and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39")
  and exists (
	select 't' from rs_len
	 where rsl_rs == dblink_acc_num
);

! echo "RefSeq changed length"
update db_link set dblink_length = (
	select  rsl_len  from rs_len where rsl_rs == dblink_acc_num
)
where dblink_length is not NULL
 and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39")
  and exists (
	select 't' from rs_len
	 where rsl_rs == dblink_acc_num
	   and dblink_length != rsl_len
);

! echo "RefSeq still have null length"
select  count(*) toomany  
 from db_link,rs_len 
 where rsl_rs == dblink_acc_num 
   and dblink_length is NULL
   and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39")
;
------------------------------------------------------------------------
! echo "can update length of RefPept based GenPept"
! echo " although maybe they should not be GenPept links"

! echo "RefSeq had null length"
update db_link set dblink_length = (
	select  gprpl_len  from gprp_len where gprpl_gp == dblink_acc_num
)
where dblink_length is NULL
 and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-42"
  and exists (
	select 't' from gprp_len
	 where gprpl_gp == dblink_acc_num
);

! echo "RefPept based GenPept changed length"
update db_link set dblink_length = (
	select gprpl_len from gprp_len where gprpl_gp == dblink_acc_num
)
where dblink_length is not NULL
 and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-42"
  and exists (
	select 't' from gprp_len
	 where gprpl_gp == dblink_acc_num
	   and dblink_length != gprpl_len
);

! echo "RefPept based GenPept still have null length"
select  count(*) toomany  
 from db_link, gprp_len 
 where gprpl_gp == dblink_acc_num 
   and dblink_length is NULL
   and dblink_fdbcont_zdb_id == "ZDB-FDBCONT-040412-42"
;


! echo "Get a list of possible non RefSeq based GenPept accessions"
delete from entrez_gb;
load from 'entrezid_GBnt_GBaa_GBdna.tab' delimiter "	"
 insert into entrez_gb;

update statistics high for table entrez_gb;

! echo "potential GenPepts to update" 
unload to 'EntrezGenPept_acc.unl' 
select distinct dblink_acc_num 
 from entrez_gb join db_link on eg_aa == dblink_acc_num
  and dblink_fdbcont_zdb_id != "ZDB-FDBCONT-040412-42" --  GenPept
  --and dblink_length is NULL  -- much cheaper but lengths *can* change
  order by 1
; 

--fdb                     howmany 
--ZDB-FDBCONT-040412-42   2750  GenPept




------------------------------------------------------------------------
drop table gprp_len;
drop table rs_len;
drop table entrez_zdbid; 
drop table entrez_gb; 
drop table entrez_rs; 
drop table entrez_ug; 


-- transaction terminated externally
