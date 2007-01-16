!echo    ''
!echo    'Statistics on Vega related genes in ZFIN'
!echo    '---------------------------------------------------------------------'
!echo    '# of sequence accession numbers Sanger has submitted to us'

select count(distinct dblink_acc_num)OTTDARGS
 from db_link
 where dblink_acc_num[1,6] = 'OTTDAR'
;

select count(distinct dblink_linked_recid)GENES
 from db_link
 where dblink_acc_num[1,6] = 'OTTDAR'
;

!echo    '---------------------------------------------------------------------'
!echo    '# of si genes renamed to informative gene nomenclature'
select count(distinct mrkr_zdb_id) proper_genes
 from marker g, data_alias
 where mrkr_type[1,4] = 'GENE'
 and mrkr_abbrev not like '%:%'
 and dalias_data_zdb_id = mrkr_zdb_id
 and dalias_alias[1,3] = 'si:'
;

select count(distinct dalias_alias) alias
 from marker g, data_alias
 where mrkr_type[1,4] = 'GENE'
 and mrkr_abbrev not like '%:%'
 and dalias_data_zdb_id = mrkr_zdb_id
 and dalias_alias[1,3] = 'si:'
;

! echo   'proper Genes with OTTDARG(s) which never had a si: name. (these are named prior to loading'

select count(distinct mrkr_zdb_id) proper_genes
 from marker g, db_link
 where mrkr_type[1,4] = 'GENE'
 and mrkr_abbrev[1,3] <> 'si:'
 and mrkr_abbrev[1,4] <> 'zgc:'
 and dblink_acc_num[1,6] = 'OTTDAR'
 and mrkr_zdb_id = dblink_linked_recid
 and not exists (
 	select 1 from data_alias
 	 where mrkr_zdb_id = dalias_data_zdb_id
 	 and dalias_alias[1,3] <> 'si:'
 )
;

! echo   'ZGC Genes with OTTDARG(s) which never had a si: name.'
! echo   '*** (previously intersected above tally) ***'

select count(distinct mrkr_zdb_id) zgc_genes
 from marker g, db_link
 where mrkr_type[1,4] = 'GENE'
 and mrkr_abbrev[1,4] = 'zgc:'
 and dblink_acc_num[1,6] = 'OTTDAR'
 and mrkr_zdb_id = dblink_linked_recid
 and not exists (
 	select 1 from data_alias
 	 where mrkr_zdb_id = dalias_data_zdb_id
 	 and dalias_alias[1,3] <> 'si:'
 )
;


!echo    ''
!echo    '---------------------------------------------------------------------'
!echo    ''
!echo    "These weren't in your categories, but may be viewed as important and easy to understand:"
!echo    '# of genes Sanger has submitted to us'
!echo    ''
!echo    '     (this is nominaly the number of OTTDARGs in the first statistic)'
!echo    ''
!echo   "(note: Sanger has withdrawn or never sent > 100 OTTDARGs)"
{
!echo    '---------------------------------------------------------------------'
!echo    'si genes that do not have an ottdarg'

select mrkr_zdb_id si_genes
 from marker
 where mrkr_abbrev[1,3] = 'si:'
 and not exists (
	select 1 from db_link
	 where dblink_acc_num[1,6] = 'OTTDAR'
	 and dblink_linked_recid = mrkr_zdb_id
)
union
select dalias_data_zdb_id
 from data_alias
 where dalias_alias[1,3] = 'si:'
 and not exists (
	select 1 from db_link
	 where dblink_acc_num[1,6] = 'OTTDAR'
	 and dblink_linked_recid = dalias_data_zdb_id
 )
;

}
!echo    '---------------------------------------------------------------------'
!echo    '# of clones Sanger has submitted to us'
select count(mrkr_zdb_id)all_bac
 from marker
 where mrkr_type in ('BAC','PAC')
;

select count(distinct mrel_mrkr_1_zdb_id)bac_w_gene
 from marker_relationship
 where mrel_type = 'clone contains gene'
;

!echo    ''
!echo    '---------------------------------------------------------------------'
!echo    '# of Sanger genes assigned to existing named genes in ZFIN with informative nomenclature'
!echo    ''
!echo   '*** see first sql ***'
!echo    '---------------------------------------------------------------------'
!echo    '# of EST genes renamed and merged into si genes'

!echo    '----- direct'
select count(distinct g.mrkr_zdb_id) si_genes
 from marker g, marker ss, marker_relationship
 where g.mrkr_abbrev[1,3] = 'si:'
 and mrel_type = 'gene encodes small segment'
 and mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
 and mrel_mrkr_2_zdb_id = ss.mrkr_zdb_id
 and ss.mrkr_type in ('EST','CDNA')
;

select count(distinct ss.mrkr_zdb_id) ests
 from marker g, marker ss, marker_relationship
 where g.mrkr_abbrev[1,3] = 'si:'
 and mrel_type = 'gene encodes small segment'
 and mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
 and mrel_mrkr_2_zdb_id = ss.mrkr_zdb_id
 and ss.mrkr_type in ('EST','CDNA')
;

!echo    '----- via marker_relationships'
select count(distinct dblink_linked_recid) si_genes
 from db_link, marker_relationship, marker ss
 where dblink_acc_num[1,6] = 'OTTDAR'
 and mrel_type = 'gene encodes small segment'
 and mrel_mrkr_1_zdb_id = dblink_linked_recid
 and mrel_mrkr_2_zdb_id = ss.mrkr_zdb_id
 and ss.mrkr_type in ('EST','CDNA')
;

select count(distinct ss.mrkr_zdb_id) ests
 from db_link, marker_relationship, marker ss
 where dblink_acc_num[1,6] = 'OTTDAR'
 and mrel_type = 'gene encodes small segment'
 and mrel_mrkr_1_zdb_id = dblink_linked_recid
 and mrel_mrkr_2_zdb_id = ss.mrkr_zdb_id
 and ss.mrkr_type in ('EST','CDNA')
;

!echo    '---------------------------------------------------------------------'
!echo    '# of si genes merged into another si gene'

!echo    '----- via alias and history'
select count (distinct mrkr_zdb_id) si_genes
 from marker, data_alias,marker_history
 where mrkr_abbrev[1,3] = 'si:'
 and mrkr_zdb_id = dalias_data_zdb_id
 and dalias_alias[1,3] = 'si:'
 and dalias_zdb_id = mhist_dalias_zdb_id
 and mhist_event = 'merged'
;

select count (distinct dalias_alias) alias
 from marker, data_alias,marker_history
 where mrkr_abbrev[1,3] = 'si:'
 and mrkr_zdb_id = dalias_data_zdb_id
 and dalias_alias[1,3] = 'si:'
 and dalias_zdb_id = mhist_dalias_zdb_id
 and mhist_event = 'merged'
;

!echo    '----- via ottargs and history'
select count (distinct dblink_acc_num) ottdargs
 from db_link, data_alias, marker_history
 where dblink_acc_num[1,6] = 'OTTDAR'
 and dblink_linked_recid = dalias_data_zdb_id
 and dalias_alias[1,3] = 'si:'
 and dalias_zdb_id = mhist_dalias_zdb_id
 and mhist_event = 'merged'
;

select count (distinct dalias_alias) alias
 from db_link, data_alias, marker_history
 where dblink_acc_num[1,6] = 'OTTDAR'
 and dblink_linked_recid = dalias_data_zdb_id
 and dalias_alias[1,3] = 'si:'
 and dalias_zdb_id = mhist_dalias_zdb_id
 and mhist_event = 'merged'
;

!echo    '---------------------------------------------------------------------'
!echo '# of novel si genes'
select count(*) si_genes
 from marker
 where mrkr_abbrev[1,3] = 'si:'
;


!echo    '---------------------------------------------------------------------'
!echo 'Genes with a Vega link and no Thisse expression'

create temp table tmp_vega_thisse_report
  (
    veth_significance  integer,
    veth_mrkr_abbrev   varchar(50),
    veth_acc_num       varchar(40),
    veth_length        integer
  )
with no log;

load from 'vega_thisse_report.unl'
insert into tmp_vega_thisse_report;

!echo    '---------------------------------------------------------------------'
!echo '# of named genes'
select count(*) named_genes from tmp_vega_thisse_report where veth_mrkr_abbrev not like "%:%";

!echo    '---------------------------------------------------------------------'
!echo '# of zgc genes'
select count(*) zgc_genes
  from tmp_vega_thisse_report
 where exists
     (
       select *
         from marker_relationship, marker gene, marker clone
        where gene.mrkr_abbrev = veth_mrkr_abbrev
          and gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
          and mrel_mrkr_2_zdb_id = clone.mrkr_zdb_id
          and clone.mrkr_abbrev[1,3] = "MGC"
     );

!echo    '---------------------------------------------------------------------'
!echo '# of si genes'
select count(*) si_genes from tmp_vega_thisse_report;

