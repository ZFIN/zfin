-- get the set of zfin genes which may be associated with a location on Ensembl
-- via association with Vega transcripts mapped to Ensembl transcripts OR
-- via 1 to 1 mappings of ensdarGs to ZDB-GENE by Ensembl

-- zfin_ensembl_gene table is created by the 'create_gff3.sql' script
-- zero it out just in case
truncate table zfin_ensembl_gene;

begin work;

--! echo 'Isolate ZFIN genes on Ensembl assembly'

create temp table tmp_vega_zeg(
	gff_seqname varchar(25),
	source varchar(45),
	feature varchar(45),
	gstart integer,
	gend integer,
	score varchar(5),
	strand char(1),
	frame char(1),
	id_name text,
	alias varchar(55)
);

insert into tmp_vega_zeg
select distinct
	et.gff_seqname,
	'ZFIN' source,
	case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end feature,
	et.gff_start gstart,
	et.gff_end   gend,
	'1' score ,
	et.gff_strand strand,
	'.' frame,
	'gene_id=' || gene.mrkr_zdb_id
	   ||';Name=' || gene.mrkr_abbrev
	   || ';so_term_name=' || szm_term_name
	   || ';curie=' || 'ZFIN:' || mrkr_zdb_id as id_name,
	gene.mrkr_zdb_id alias
 from  marker gene, marker_type_group_member, marker_relationship, gff3 vt, gff3 et, db_link vTdbl, db_link eTdbl, so_zfin_mapping
 where mtgrpmem_mrkr_type = mrkr_type
   and mtgrpmem_mrkr_type_group = 'GENEDOM'
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and gene.mrkr_type = szm_object_type
   and vTdbl.dblink_linked_recid = mrel_mrkr_2_zdb_id
   and vTdbl.dblink_acc_num = vt.gff_id
   and vt.gff_source  = 'vega'
   and vt.gff_feature = 'transcript'
   and eTdbl.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
   and eTdbl.dblink_linked_recid = vTdbl.dblink_linked_recid
   and eTdbl.dblink_acc_num = et.gff_id
   and substring(et.gff_source from 1 for 8) = 'Ensembl_'
   and et.gff_feature in ('mRNA','transcript')
  group by 1,3,4,5,7,9,10
;

-- index creation on temp tables within a transaction ids broken on waldo 2011/Oct/6
-- aparently may work if temp table is created first...

create index tmp_vega_zeg_alias_idx on  tmp_vega_zeg(alias);
create index tmp_vega_zeg_gstart_idx on tmp_vega_zeg(gstart);
create index tmp_vega_zeg_gend_idx on   tmp_vega_zeg(gend);

--update statistics high for table tmp_vega_zeg(alias,gstart,gend);

--! echo 'any ott based genes on multiple LG?'   -- 6, 5 w/2 tscripts 1 w/4
Create temp table tmp_dup_vega_zeg as
select a.alias dup_lg ,count(*) howmany
 from tmp_vega_zeg a, tmp_vega_zeg b
 where a.alias = b.alias
   and  a.gff_seqname != b.gff_seqname
  group by 1;

delete from tmp_vega_zeg where exists (
	select 't' from tmp_dup_vega_zeg where dup_lg = alias
);
select distinct * from  tmp_dup_vega_zeg;
drop table  tmp_dup_vega_zeg;

------------------------------------------------------------------------




--------------------------------------------------------------
--! echo 'remove strandedness when vega dissagrees with vega (regardless of extents)'
create temp table tmp_strand as
select a.Alias
from  tmp_vega_zeg a, tmp_vega_zeg b
	 where b.gff_seqname =  a.gff_seqname
	   --and b.gstart    =  a.gstart
	   --and b.gend      =  a.gend
	   and b.strand      !=  a.strand
	   and b.Alias       =  a.Alias
;

--! echo 'Potentially on different strands by ottdarT<->ensdarT'
select distinct * from  tmp_strand order by 1;


update tmp_vega_zeg set strand = '.' where exists (
	select 't' from tmp_strand
	 where  tmp_strand.alias =  tmp_vega_zeg.alias
);

drop table tmp_strand;

--{
--  We want to avoid including poorly localized genes since 3rd parties
--  will be using these extents of a gene to attach location based
--  attributes and a gene that spans half a chromosome will be a liability
--}

--! echo 'if ott based extents for a gene overlap, them merge them'
create temp table tmp_vz as
select distinct * from tmp_vega_zeg;

--! echo 'extend to max when ott based extents overlap'
update tmp_vega_zeg set gend = (
	select max(tmp_vz.gend) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gend        >   tmp_vega_zeg.gend
	   and tmp_vega_zeg.gend between tmp_vz.gstart and tmp_vz.gend

)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gend        >   tmp_vega_zeg.gend
	   and tmp_vega_zeg.gend between tmp_vz.gstart and tmp_vz.gend
);

drop table tmp_vz;
--! echo 'make ott based overlapping extents unique'
create temp table tmp_distinct_vega_zeg as
select distinct * from tmp_vega_zeg ;

delete from tmp_vega_zeg;
insert into tmp_vega_zeg select * from tmp_distinct_vega_zeg;
drop table tmp_distinct_vega_zeg;

create temp table tmp_vz as
select * from tmp_vega_zeg;
--! echo 'extend to min when ott based extents overlap'

update tmp_vega_zeg set gstart = (
	select min(tmp_vz.gstart) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
	   and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
       and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
);

--! echo 'make ott based overlapping extents unique'
create temp table tmp_distinct_vega_zeg as
select distinct * from tmp_vega_zeg;

delete from tmp_vega_zeg;
insert into tmp_vega_zeg select * from tmp_distinct_vega_zeg;
drop table tmp_distinct_vega_zeg;

--update statistics high for table tmp_vega_zeg(alias,gstart,gend);

--! echo 'if the gap between clusters of a gene's transcripts is not longer'
--! echo 'than the longer cluster, then merge those transcript clusters'

--! echo 'merge adjecent ott based extents when gap is short'


update tmp_vega_zeg set gend = (
	select max(tmp_vz.gend) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart	   >   tmp_vega_zeg.gend
	   and (
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vega_zeg.gend - tmp_vega_zeg.gstart)
	   		OR
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vz.gend - tmp_vz.gstart)
	   )

)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart	   >   tmp_vega_zeg.gend
	   and (
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vega_zeg.gend - tmp_vega_zeg.gstart)
	   		OR
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vz.gend - tmp_vz.gstart)
	   )
);

--! echo '_Again_, extend to min when ott based extents overlap'

drop table tmp_vz;

create temp table tmp_vz as
select distinct * from tmp_vega_zeg;

update tmp_vega_zeg set gstart = (
	select min(tmp_vz.gstart) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
	   and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
	   and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
);

--! echo 'Second round of merge adjecent ott based extents when gap is short'

update tmp_vega_zeg set gend = (
	select max(tmp_vz.gend) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart	   >   tmp_vega_zeg.gend
	   and (
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vega_zeg.gend - tmp_vega_zeg.gstart)
	   		OR
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vz.gend - tmp_vz.gstart)
	   )
)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart	   >   tmp_vega_zeg.gend
	   and (
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vega_zeg.gend - tmp_vega_zeg.gstart)
	   		OR
	   		(tmp_vz.gstart - tmp_vega_zeg.gend) <= (tmp_vz.gend - tmp_vz.gstart)
	   )
);

--! echo '_Again_, extend to min when ott based extents overlap'

drop table tmp_vz;

create temp table tmp_vz as
select  distinct * from tmp_vega_zeg;

update tmp_vega_zeg set gstart = (
	select min(tmp_vz.gstart) from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
	   and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
)where exists (
select 't' from tmp_vz
	 where tmp_vz.gff_seqname =  tmp_vega_zeg.gff_seqname
	   and tmp_vz.Alias      =  tmp_vega_zeg.Alias
	   and tmp_vz.gstart      <   tmp_vega_zeg.gstart
	   and tmp_vega_zeg.gstart between tmp_vz.gstart and tmp_vz.gend
);

drop table tmp_vz;

--! echo 'make ott based overlapping extents unique'
create temp table tmp_distinct_vega_zeg as
select distinct * from tmp_vega_zeg ;

delete from tmp_vega_zeg;

insert into tmp_vega_zeg select * from tmp_distinct_vega_zeg;
drop table tmp_distinct_vega_zeg;

--! echo 'what are the distances between the gene extents on the same chr?'
select
	substring(a.Alias from 1 for 25) zdb,
	b.gstart - a.gend gap,
	(case when a.gend - a.gstart  >  b.gend - b.gstart
	 then a.gend - a.gstart
	 else b.gend - b.gstart end) len,
	 case
		when a.gend - a.gstart  >=  (b.gstart - a.gend) then 'true'
	 	when b.gend - b.gstart  >=  (b.gstart - a.gend) then 'true'
	 	else 'false'
	 end  collapse,
	 count(*) howmany
 from tmp_vega_zeg a,tmp_vega_zeg b
 where a.gff_seqname =  b.gff_seqname
   and a.Alias      =  b.Alias
   and a.gend  < b.gstart
 group by 1,2,3,4
 order by 1,4,2
;
------------------------------------------------------------------------
--{ have made an effort to grow clusters of ott based transcript clusters
----into single gene extents, genes that still have disjoint fragments are
--dropped and we hopr they have an 2nsdarG1:1 relationship and will use
--ensembls transcript set to determine gene extent if any
--}

create temp table tmp_disjoint_vega_zeg as
select distinct alias disjoint from tmp_vega_zeg group by 1 having count(*) > 1
;

delete from tmp_vega_zeg where exists (
	select 't' from tmp_disjoint_vega_zeg where alias = disjoint
);

select * from tmp_disjoint_vega_zeg;
drop table tmp_disjoint_vega_zeg;

--! echo 'Gene / ENSDARG Combinations that need to be excluded because they're not 1-1'

create temp table tmp_ensembl_not_one_to_one as
select mrkr_zdb_id, dbl1.dblink_acc_num
from marker
  join db_link dbl1 on mrkr_zdb_id = dbl1.dblink_linked_recid
  join (select count(*) as be_zero, dblink_acc_num
                            from db_link
                            where dblink_acc_num like 'ENSDARG%'
                            group by dblink_acc_num
                            having count(*) > 1) dbl2 on dbl1.dblink_acc_num = dbl2.dblink_acc_num
union
select mrkr_zdb_id, dblink_acc_num
from marker
  join (select count(*) as be_zero, dblink_linked_recid as troublemaker_zdb_id
            from db_link dbl1
            where dblink_acc_num like 'ENSDARG%'
            group by dblink_linked_recid
            having count(*) > 1 ) as alias on mrkr_zdb_id = troublemaker_zdb_id
  join db_link on dblink_linked_recid = mrkr_zdb_id
where dblink_acc_num like 'ENSDARG%';

select * from tmp_ensembl_not_one_to_one;


----------------- Extra ensdarG 1:1 ----------------------------
create temp table tmp_ensembl_zeg as
select
	gff_seqname,
	'ZFIN'::text source,
	case gene.mrkr_type when 'GENEP' then 'pseudogene' else  'gene' end feature,
	min(gff_start) gstart,
	max(gff_end)  gend,
	'2'::text score,
	gff_strand,
	'.'::text frame,
		'gene_id=' || gene.mrkr_zdb_id
	   ||';Name=' || gene.mrkr_abbrev
	   || ';so_term_name=' || szm_term_name
	   || ';curie=' || 'ZFIN:' || mrkr_zdb_id as id_name,
	gene.mrkr_zdb_id alias
 from  marker gene, db_link eGdbl, gff3, so_zfin_mapping
 where substring(gene.mrkr_type from 1 for 4) = 'GENE'
   and gene.mrkr_type = szm_object_type
   and gene.mrkr_zdb_id = eGdbl.dblink_linked_recid
   and eGdbl.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
   and eGdbl.dblink_acc_num = gff_parent
   and substring(gff_source from  1 for 8) = 'Ensembl_'
   and gff_feature in ('mRNA','transcript')
   and not exists (
   	select 't' from tmp_vega_zeg where gene.mrkr_zdb_id = tmp_vega_zeg.alias
  )
   and not exists (
    select 't' from tmp_ensembl_not_one_to_one tenoto where tenoto.mrkr_zdb_id = gene.mrkr_zdb_id
   )
 group by 1,3,7,9,10
;



--! echo 'any ens based genes on multiple LG? (should be zero)'   -- 0
select substring(a.alias from 1 for 25) zdb ,count(*) bezero
 from tmp_ensembl_zeg a, tmp_ensembl_zeg b
 where a.alias = b.alias
   and  a.gff_seqname != b.gff_seqname
  group by 1
;

------------------------------------------------------------------------

--! echo 'howmany from Vega transcript mapping?'
select count(*) vega_mapping from tmp_vega_zeg;

--! echo 'howmany from Ensembl 1:1?'
select count(*) Ens_1to1 from tmp_ensembl_zeg;

--! echo 'Save both zfin gene placement collections'
insert into zfin_ensembl_gene (
    zeg_seqname,
    zeg_source,
    zeg_feature,
    zeg_start,
    zeg_end,
    zeg_score,
    zeg_strand,
    zeg_frame,
    zeg_ID_Name,
    zeg_Alias
)
select
    gff_seqname,
    source,
    trim(feature),
    min(gstart) gstart,
    max(gend) gend,
    score,
    strand,
    frame,
    ID_Name,
    Alias
 from tmp_vega_zeg
 group by
    gff_seqname,
    source,
    feature,
    score,
    strand,
    frame,
    ID_Name,
    Alias
;

insert into zfin_ensembl_gene (
    zeg_seqname,
    zeg_source,
    zeg_feature,
    zeg_start,
    zeg_end,
    zeg_score,
    zeg_strand,
    zeg_frame,
    zeg_ID_Name,
    zeg_Alias
)
	SELECT
		 gff_seqname,
		 source,
		 trim(feature),
		 min(gstart) gstart,
		 max(gend)   gend,
		 score,
		 gff_strand,
		 frame,
		 ID_Name,
		 Alias
	 FROM tmp_ensembl_zeg
	 GROUP BY
		 gff_seqname,
		 source,
		 feature,
		 score,
		 gff_strand,
		 frame,
		 ID_Name,
		 Alias
;

select * from zfin_ensembl_gene;
-------------------------------------------------------------------------

drop table tmp_vega_zeg;
drop table tmp_ensembl_zeg;

--update statistics high for table zfin_ensembl_gene;

-- unload genes,alias,antibody,pheno,xpat,...
