-- pre-process the .csv files from Bernard
-- to create informix loadable .unl files with 
-- the data somewhat closer to it's final form.
! echo "parsing Bernard's data" 
--! /private/bin/rebol -sq parse-bt.r
--! echo "Bernard's data parsed"

--       Constants that may be useful

-- ZDB-LAB-980204-15     Thisse Lab
-- ZDB-LAB-991005-53     ZIRC
-- ZDB-PERS-960805-556   Thisse, Bernard
-- ZDB-FISH-010924-10    AB/TU fish 


-- load bernards probe table into temps
begin work;

-- PROBES --
------------
create temp table probes_tmp (
clone varchar (120) not null,
isgene varchar(15) default null,
genename varchar(120)default null,
top_blast varchar(200),
blast_result clob, 
gb5p varchar (50),
gb3p varchar (50),
or_lg varchar(2),
lg_loc decimal(8,2),
metric varchar(5),
library varchar(80),
vector varchar(80),
insert_kb float,
cloning_site varchar(20),
digest varchar(20),
polymerase varchar(80),
medline_id varchar(80),
text_citation lvarchar, 
comments lvarchar,
expression lvarchar, 
modified DATETIME YEAR TO DAY
)with no log;

load from 'probes.unl' insert into probes_tmp;

update probes_tmp set clone = lower(clone);
update probes_tmp set isgene = 'off' where isgene is null;


! echo "Duplicate clones?"
select clone, count(*) dups from  probes_tmp group by clone having count(*) > 1; 

select count(unique clone)clones from probes_tmp;
! echo "Called genes they are" 
select clone[1,10],genename from probes_tmp where isgene = 'on'; 

unload to 'is_gene.txt' select clone[1,10],genename from probes_tmp where isgene = 'on';

! echo "Called PUTATIVE they are" 
select clone[1,10],genename
from probes_tmp
where genename is not null
and isgene <> 'on'
; 

! echo "Called IN ZFIN PUTATIVE they are" 
select clone[1,10],genename
from probes_tmp,marker 
where genename is not null
and isgene <> 'on'
and mrkr_name = clone 
; 





-- EXPRESSION_TXT --
--------------------
create temp table expression_tmp(
clone varchar (80) not null,
sstart varchar (50),
sstop varchar (50),
description lvarchar,
modified DATETIME YEAR TO DAY
)with no log;

load from 'expression.unl' insert into expression_tmp;
update expression_tmp set clone = lower(clone);
!echo "expression not associated with clone in this load"  
select clone from  expression_tmp where clone not in (select p.clone from probes_tmp p);
delete from expression_tmp where clone not in (select p.clone from probes_tmp p);

update expression_tmp set sstart = 
    (select stg_zdb_id from stage where stg_name = sstart);
update expression_tmp set sstop = 
    (select stg_zdb_id from stage where stg_name = sstop);

select count(*) valid_exp  from probes_tmp p,expression_tmp e where p.clone = e.clone; 


-- KEYWORDS_TXT --
------------------
create temp table keywords_tmp (
clone varchar(80) not null,
sstart varchar (50),
sstop varchar (50),
keyword varchar(50),
modified DATETIME YEAR TO DAY  
)with no log;

load from 'keywords.unl' insert into keywords_tmp;

!echo "blank keywords in keyword table"    
select * from keywords_tmp where keyword is NULL;
delete   from keywords_tmp where keyword is NULL; 

--select keyword kw from keywords_tmp;
update keywords_tmp set clone = lower(clone);
!echo "keywords not associated with clone in this load"  

select clone,keyword from  keywords_tmp k where k.clone not in ( select p.clone from probes_tmp p);
delete from  keywords_tmp where clone not in ( select p.clone from probes_tmp p);


select count(*)valid_kw  from probes_tmp p, keywords_tmp k where p.clone = k.clone;


-- check for keywords that have been modified/deleted

-- These keywords have been deleted from the indicated stages only.
-- In some cases they still exist at other stages.
 
-- B        'Blastula:Sphere'
-- G        'Gastrula:50%-epiboly'             
-- ES       'Segmentation:1-somite'   
-- MS       'Segmentation:10-somite'
-- 24 h     'Segmentation:20-somite'   
-- 36 h     'Pharyngula:Prim-15'      
-- 48 h     'Pharyngula:High-pec' 


! echo "check for keywords that bave been deleted"

select * from keywords_tmp where keyword = 'ectoderm' 
    and sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
delete from keywords_tmp where keyword = 'ectoderm' 
    and sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)

select * from keywords_tmp where keyword = 'endoderm' 
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); --(36h, 48h)
delete from keywords_tmp where keyword = 'endoderm' 
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); --(36h, 48h)

select * from keywords_tmp where keyword = 'nasal pit' 
    and sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
delete from keywords_tmp where keyword = 'nasal pit' 
    and sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)

select *  from keywords_tmp where keyword = 'neural crest' 
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); -- (36h, 48h)
delete from keywords_tmp where keyword = 'neural crest' 
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); -- (36h, 48h)

select * from keywords_tmp where keyword = 'otic placode' 
    and sstart in ('Segmentation:1-somite'); -- (ES) 
delete from keywords_tmp where keyword = 'otic placode' 
    and sstart in ('Segmentation:1-somite'); -- (ES)

select * from keywords_tmp where keyword = 'primary motorneurons' 
    and sstart in ('Segmentation:14-somite'); -- (MS) 
delete from keywords_tmp where keyword = 'primary motorneurons' 
    and sstart in ('Segmentation:14-somite'); -- (MS)

select * from keywords_tmp where keyword = 'Rohon-Beard neurons' 
    and sstart in ('Segmentation:14-somite'); -- (MS)
delete from keywords_tmp where keyword = 'Rohon-Beard neurons' 
    and sstart in ('Segmentation:14-somite'); -- (MS)

select * from keywords_tmp where keyword = 'YSL' 
    and sstart in ('Pharyngula:High-pec'); -- (48h)
delete from keywords_tmp where keyword = 'YSL' 
    and sstart in ('Pharyngula:High-pec'); -- (48h)

--Keywords modified
! echo "check for keywords that have been changed at particular stages"

update keywords_tmp set keyword = 'branchial arches' 
    where keyword = 'pharyngeal arches' 
    and sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
update keywords_tmp set keyword = 'presumptive central nervous system' 
    where keyword = 'neurectoderm' 
    and sstart in ('Segmentation:1-somite'); --  (ES);
update keywords_tmp set keyword = 'central nervous system' 
    where keyword = 'neurectoderm' 
    and sstart in ('Segmentation:14-somite','Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); --  ( MS, 24h, 36h, 48h)

-- These keywords have been changed at the indicated stages only. 
-- In some cases they have been left unchanged at other stages.
update keywords_tmp set keyword = 'proctodeum' 
    where keyword = 'anus' 
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); --  (36h, 48h)
update keywords_tmp set keyword = 'presumptive cephalic mesoderm'  
    where keyword = 'cephalic mesoderm'
    and sstart in ('Gastrula:50%-epiboly'); --  (G)
update keywords_tmp set keyword = 'presumptive cranial ganglia'
    where keyword = 'cranial ganglia' 
    and sstart in ('Segmentation:1-somite'); -- (ES)
update keywords_tmp set keyword = 'inner ear' 
    where keyword = 'ear'
    and sstart in ('Segmentation:14-somite','Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (MS, 24h, 36h, 48h)
update keywords_tmp set keyword = 'presumptive hindbrain' 
    where keyword = 'hindbrain'
   and sstart in ('Segmentation:1-somite'); -- (ES)
-- In the anatomical dictionary, primordia is shown as a sub-part of lateral line. 
-- The keyword primordia is available for 24h, 36h, and 48h
update keywords_tmp set keyword = 'primordia' 
    where keyword = 'lateral line primordium'
    and sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); -- (36h, 48h)
update keywords_tmp set keyword = 'muscle pioneers' 
    where keyword = 'muscle pioneer cells'
    and sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set keyword = 'presumptive central nervous system'
    where keyword = 'neurectoderm' 
    and sstart in ('Segmentation:1-somite'); -- (ES)
update keywords_tmp set keyword = 'neuromasts'  
    where keyword = 'neuromast';
update keywords_tmp set keyword = 'presumptive neurons' 
    where keyword = 'neurons'
    and sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set keyword = 'otic placode' 
    where keyword = 'otic vesicle'
    and sstart in ('Segmentation:14-somite'); -- (MS)
--In the anatomical dictionary, segmental plate is a sub-part of paraxial mesoderm at these stages.
update keywords_tmp set keyword = 'segmental plate' 
    where keyword = 'paraxial segmental plate'
    and sstart in ('Segmentation:1-somite','Segmentation:14-somite','Segmentation:14-somite'); -- (ES, MS, 24h)    
update keywords_tmp set keyword = 'primary motoneurons' 
    where keyword = 'primary motorneurons'; 
update keywords_tmp set keyword = 'gut' 
    where keyword = 'primitive gut'
    and sstart in ('Segmentation:20-somite'); -- (24h)
update keywords_tmp set keyword = 'primordia' 
    where keyword = 'primordium'
    and sstart in ('Segmentation:20-somite'); -- (24h)
update keywords_tmp set keyword = 'rhombomeres r2-r6' 
    where keyword = 'rhombomeres r2-r8'
    and sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set keyword = 'presumptive spinal cord' 
    where keyword = 'spinal cord'
    and sstart in ('Segmentation:1-somite','Segmentation:14-somite'); -- (ES, MS)
update keywords_tmp set keyword = 'trigeminal ganglions' 
    where keyword = 'trigeminal ganglion';

-------------------------------------------------------------
! echo "change start/stop stage names to zdbids"
update keywords_tmp set sstart = 
    (select stg_zdb_id from stage where stg_name = sstart);
update keywords_tmp set sstop = 
    (select stg_zdb_id from stage where stg_name = sstop);

! echo "change anatomy item names to zdbids"
update keywords_tmp set keyword = 
    (select anatitem_zdb_id from anatomy_item where anatitem_name = keyword);



!echo "-- IMAGES_TXT --"
----------------
create temp table images_tmp (
imagename varchar (80) not null unique,
clone varchar (80) not null,
sstart varchar (50),
sstop varchar (50),
o_view varchar(20),
o_direction varchar(30),
orientation varchar(60),
preparation varchar(15),
comments lvarchar,
medline_ID varchar (50),
text_citation varchar (50) ,
modified DATETIME YEAR TO DAY
)with no log;

load from 'images.unl' insert into images_tmp;


!echo "images not associated with clone in this load"  
update images_tmp set clone = lower(clone);
select imagename,clone from  images_tmp where clone not in (select p.clone from probes_tmp p);
delete from  images_tmp where clone not in (select p.clone from probes_tmp p);

update images_tmp set sstart = 
    (select stg_zdb_id from stage where stg_name = sstart);
update images_tmp set sstop = 
    (select stg_zdb_id from stage where stg_name = sstop);

update images_tmp set comments = "none given" where comments is NULL;  

--select imagename from  probes_tmp p,images_tmp i where p.clone = i.clone;

! echo "image's expression stage not found in expression_pattern_stage"
insert into expression_tmp
    select distinct i.clone, 
           i.sstart, 
           i.sstop,
           "none given",
           TODAY
    from images_tmp i
    where not exists 
      (  
         select *
         from expression_tmp e
         where i.clone = e.clone
           and i.sstart = e.sstart
      );



! echo "--  BERNARD's data loaded into temp tables"
! echo ""

-------------------------------------------------------
-------------------------------------------------------
-- ZFIN

! echo "-- MARKER --"
------------

create Temp table tmp_mrkr(
    mrkr_zdb_id  varchar(50) not null,
    mrkr_name varchar(80)not null,
    mrkr_comments lvarchar,
    mrkr_abbrev varchar(20)not null,
    mrkr_type varchar(10),
    mrkr_owner varchar(50),
    mrkr_name_order varchar(140),
    mrkr_abbrev_order varchar(60) not null
  ) with no log;

-- get 'new est' markers into zfin
-- may never happen if they continue to ship .xl files 
-- to the stock center prior to sending us the expression data. 
! echo "the next insert may never happen"
insert into tmp_mrkr 
    select 
        get_id('EST'),
        pt.clone,
        comments,
        pt.clone,
        'EST',
        'ZDB-PERS-960805-556',
        zero_pad(pt.clone),
        zero_pad(pt.clone)
    from probes_tmp pt 
    where pt.clone not in (
        select mrkr_abbrev from marker
        where mrkr_abbrev[1,2] = 'cb'
        and mrkr_type in ('EST','GENE')
    )
;


-- add the new active data zdbids
insert into zdb_active_data select mrkr_zdb_id from tmp_mrkr;
insert into marker select * from tmp_mrkr;


!echo " load pubs for marker"
insert into record_attribution 
select mrkr_zdb_id, 'ZDB-PUB-010810-1','primary' from tmp_mrkr where mrkr_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_data_zdb_id = mrkr_zdb_id and recattrib_source_zdb_id = "ZDB-PUB-010810-1");


select mrkr_name new_ests  from  tmp_mrkr;


-- 

-- these markers already exist in zfin so it is posible they are being updated
create Temp table tmp_mrkr_update(
    mrkr_zdb_id  varchar(50) not null,
    mrkr_name varchar(80)not null,
    mrkr_comments lvarchar,
    mrkr_abbrev varchar(20)not null,
    mrkr_type varchar(10),
    mrkr_owner varchar(50),
    mrkr_name_order varchar(140),
    mrkr_abbrev_order varchar(60) not null
  ) with no log;


! echo "isolate the existing markers that are in this gene expression upload " 
insert into tmp_mrkr_update 
    select marker.* from marker, probes_tmp where mrkr_abbrev = clone;


-- move any existing marker for which there is currently no expression data 
-- in with the new markers
insert into tmp_mrkr 
    select distinct * 
    from  tmp_mrkr_update tmu 
    where tmu.mrkr_zdb_id not in (
        select xpat_probe_zdb_id
        from expression_pattern
    )
    and tmu.mrkr_zdb_id is not null
    and tmu.mrkr_zdb_id not in (
        select m.mrkr_zdb_id
        from tmp_mrkr m
    );



delete from tmp_mrkr_update where mrkr_zdb_id in (select mrkr_zdb_id from tmp_mrkr);
--rows left in tmp_mrkr_update have had expression data in the past and need to
--be updated differently -- that is they should not be here.

! echo "these are updates, not new_loads, deal with them elsewhere" 
select * from  tmp_mrkr_update;





-- set up genbank & BLAST records for markers with accession numbers

create temp table tmp_db_link
  (
    linked_recid	varchar(50),
    db_name		varchar(50),
    acc_num		varchar(50),
    into 		varchar(80),
    dblink_zdb_id	varchar(50)
  )with no log;

select linked_recid
from db_link 
where db_name = 'Genbank' 
into temp linked_markers;

!echo "making active data records for new genbank links"
select distinct mrkr_zdb_id, mrkr_abbrev
from tmp_mrkr,probes_tmp
where clone = mrkr_name
and mrkr_zdb_id not in (select * from linked_markers)
into temp link_db;


insert into tmp_db_link
select mrkr_zdb_id,'Genbank',gb5p, 'uncurated ' || TODAY, 'x'
from link_db,probes_tmp
where clone = mrkr_abbrev
and gb5p is not NULL
--and gb3p is NULL
;

insert into tmp_db_link
select mrkr_zdb_id,'Genbank',gb3p, 'uncurated ' || TODAY, 'x'
from link_db,probes_tmp
where clone = mrkr_abbrev
--and gb5p is NULL
and gb3p is not NULL
;

update tmp_db_link set dblink_zdb_id = get_id('DBLINK');

insert into zdb_active_data select dblink_zdb_id from tmp_db_link;
insert into db_link select * from tmp_db_link;


delete from tmp_db_link;
drop table  linked_markers;
drop table  link_db;


!echo "Add BLAST Links"
select linked_recid 
from db_link 
where db_name = 'BLAST' 
into temp linked_markers
;

!echo "making active data records for new blast links"
select mrkr_zdb_id, mrkr_abbrev
from marker,probes_tmp
where clone = mrkr_abbrev
and mrkr_zdb_id not in (select * from linked_markers)
into temp link_db;


insert into tmp_db_link
select mrkr_zdb_id,'BLAST',gb5p, 'uncurated ' || TODAY, 'x'
from link_db,probes_tmp
where clone = mrkr_abbrev
and gb5p is not NULL
--and gb3p is NULL
;
insert into tmp_db_link
select mrkr_zdb_id,'BLAST', gb3p, 'uncurated ' || TODAY, 'x'
from link_db,probes_tmp
where clone = mrkr_abbrev
and gb3p is not NULL
--and gb5p is NULL
;

update tmp_db_link set dblink_zdb_id = get_id('DBLINK');

insert into zdb_active_data select dblink_zdb_id from tmp_db_link;
insert into db_link select * from tmp_db_link;

delete from tmp_db_link;
drop table  linked_markers;
drop table  link_db;





! echo "-- PROBE_LIBRARY --"
----------------------------
create temp table tmp_probe_library
  (
    pl_zdb_id varchar(50),
    pl_name varchar(80),
    pl_url varchar(100)
  )
with no log;

insert into tmp_probe_library
  select distinct  
    'x',
    library,
    ''
  from
    probes_tmp
  where
    library not in (select probelib_name from probe_library)
;

update tmp_probe_library
set pl_zdb_id = get_id('PROBELIB');

! echo 'insert probe libraries'
insert into zdb_active_data select pl_zdb_id from tmp_probe_library;
insert into probe_library select * from tmp_probe_library;


! echo "-- CLONE --"
-----------------------------
create temp table tmp_clone (
    cln_zdb_id varchar(50)not null,
    cln_comments varchar(80),
    cln_name varchar(80)not null,
    cln_polymerase_name varchar(80),
    cln_insert_size integer,
    cln_cloning_site varchar(20),
    cln_digest varchar(20),
    cln_probelib_zdb_id varchar(50),
    cln_sequence_type varchar(20),
    cln_type_name varchar(20)
) with no log;


--- get _new_ clones
insert into tmp_clone 
	select distinct
        mrkr_zdb_id,
        '',
        vector,
        polymerase,
        insert_kb *1000,
        cloning_site,
        digest,
        probelib_zdb_id,
        'cDNA',
        'Plasmid'
	from probes_tmp, probe_library, tmp_mrkr
	where library = probelib_name
          and clone = mrkr_name
;


! echo "load clones"
insert into clone select * from tmp_clone tl;


! echo "-- EXPRESSION PATTERN --"
--------------------------------
create temp table tmp_exp_pat (
    xpat_zdb_id varchar(50)not null,
    xpat_fish_zdb_id varchar(50)not null,
    xpat_comments lvarchar,
    xpat_assay_name varchar(40),
    xpat_direct_submission_date DATETIME YEAR TO DAY,
    xpat_probe_zdb_id varchar(50)not null
) with no log;

insert into tmp_exp_pat 
    select distinct 'x', 'ZDB-FISH-010924-10', '', 'RNA in situ', TODAY, mrkr_zdb_id
    from tmp_mrkr
    where mrkr_name in (select distinct clone from expression_tmp)
;

--debugging
!echo "Clones with no expression pattern."
select mrkr_name 
from tmp_mrkr 
where mrkr_zdb_id not in (select xpat_probe_zdb_id from tmp_exp_pat);

UPDATE tmp_exp_pat SET xpat_zdb_id = get_id('XPAT');

insert into zdb_active_data select xpat_zdb_id from tmp_exp_pat;

insert into expression_pattern select * from tmp_exp_pat;

! echo "load pub & source for xpats"
-- int_data_source --
insert into int_data_source select xpat_zdb_id,'ZDB-LAB-980204-15' from tmp_exp_pat;

-- record_attribution --
insert into record_attribution select xpat_zdb_id, 'ZDB-PUB-010810-1','primary' from tmp_exp_pat;

--drop table tmp_exp_pat;


! echo "-- GENBANK --" 
-- make sure the genbank numbers exist --
select gb5p from probes_tmp where gb5p is not null into temp gb5p_tmp with no log;
delete from gb5p_tmp where gb5p in (select genbank_acc_num from genbank);
insert into genbank select gb5p,"5'" from gb5p_tmp;
drop table gb5p_tmp;

select gb3p from probes_tmp where gb3p is not null into temp gb3p_tmp with no log;
delete from gb3p_tmp where gb3p in (select genbank_acc_num from genbank);
insert into genbank select gb3p,"3'" from gb3p_tmp;
drop table gb3p_tmp;



! echo "-- BLAST --"  
--------------------------
-- 
-- top_blast is not being used now???

select * from tmp_mrkr tm where tm.mrkr_zdb_id not in ( select m.mrkr_zdb_id from marker m); 

insert into blast 
    select distinct mrkr_zdb_id, gb5p, TODAY, 'none given', blast_result
    from tmp_mrkr, probes_tmp p 
    where p.clone = mrkr_abbrev
    and  gb5p   is not null
    and p.blast_result is not null
;
insert into blast 
    select distinct mrkr_zdb_id, gb3p, TODAY, 'none given',blast_result
    from tmp_mrkr, probes_tmp p 
    where p.clone = mrkr_abbrev
    and gb3p is not null
    and p.blast_result is not null
    and mrkr_zdb_id not in (select blast_mrkr_zdb_id from blast)
;



! echo "-- PUTATIVE_GENE --" 
--------------------------
insert into putative_non_zfin_gene
    select distinct mrkr_zdb_id, p.genename,'none','ZDB-PUB-010810-1'
    from tmp_mrkr, probes_tmp p 
    where p.clone = mrkr_abbrev
    and p.isgene <> 'on'  
    and p.genename is not NULL
;


! echo "-- EXPRESSION_PATTERN_STAGE --" 
---------------------------------------
create temp table tmp_exp_pat_stg(
    xpatstg_xpat_zdb_id varchar(50)not null,
    xpatstg_start_stg_zdb_id varchar(50)not null,
    xpatstg_end_stg_zdb_id varchar(50)not null,
    xpatstg_comments lvarchar
  ) with no log;

-- stick it in a temp so this batch stays isolated from what is in zfin (for the rest of the load)
insert into tmp_exp_pat_stg 
    select distinct
            xpat_zdb_id, 
            ex.sstart, 
            ex.sstop, 
            ex.description

    from tmp_mrkr,tmp_exp_pat,expression_tmp ex
    where mrkr_name = ex.clone
    and   mrkr_zdb_id = xpat_probe_zdb_id
    and   mrkr_name is not null
    and   ex.clone is not null
    and   xpat_zdb_id is not null
;


--debugg violation of informix.expression_pattern_stage_primary_key
select 
  mrkr_name,
  s1.stg_name,
  s2.stg_name
from tmp_exp_pat_stg, stage s1, stage s2, marker, expression_pattern
where mrkr_zdb_id = xpat_probe_zdb_id
  and xpat_zdb_id = xpatstg_xpat_zdb_id
  and xpatstg_start_stg_zdb_id = s1.stg_zdb_id
  and xpatstg_end_stg_zdb_id = s2.stg_zdb_id
group by mrkr_name, s1.stg_name, s2.stg_name
having count(*) > 1
order by mrkr_name, s1.stg_name;


insert into expression_pattern_stage 
    select * from tmp_exp_pat_stg;



! echo "-- EXPRESSION_PATTERN_ANATOMY --"
--------------------------------
! echo ""

--select * from  tmp_exp_pat_stg;
--select * from  tmp_exp_pat;
--select * from  tmp_mrkr;


select distinct xpatstg_xpat_zdb_id xpatstg_xpat_zdb
    from    tmp_exp_pat_stg,
            tmp_exp_pat,
            tmp_mrkr,
            keywords_tmp

    where mrkr_name = clone
    and xpat_probe_zdb_id = mrkr_zdb_id
    and xpatstg_xpat_zdb_id = xpat_zdb_id
    and clone is not null
    and mrkr_name is not null
    and mrkr_zdb_id is not null
    and xpat_probe_zdb_id is not null
    and xpat_zdb_id  is not null
    and xpatstg_xpat_zdb_id is not null
;


delete from keywords_tmp where clone not in (select mrkr_abbrev from tmp_mrkr);
select count(*) from keywords_tmp;

select distinct clone, s1.stg_name, s2.stg_name 
from keywords_tmp, stage s1, stage s2
where clone not in (
    select distinct mrkr_name 
    from    tmp_exp_pat_stg,
            tmp_exp_pat,
            tmp_mrkr
    where mrkr_name = clone
    and xpat_probe_zdb_id = mrkr_zdb_id
    and xpatstg_xpat_zdb_id = xpat_zdb_id

    and clone is not null
    and mrkr_name is not null

    and mrkr_zdb_id is not null
    and xpat_probe_zdb_id is not null

    and xpat_zdb_id  is not null
    and xpatstg_xpat_zdb_id is not null)
and sstart = s1.stg_zdb_id
and sstop = s2.stg_zdb_id
--where clone in (select mrkr_name from tmp_mrkr where mrkr_name is not null)
--and clone is not null
;


update keywords_tmp set clone = 
    (select distinct xpatstg_xpat_zdb_id 
    from    tmp_exp_pat_stg,
            tmp_exp_pat,
            tmp_mrkr
    where mrkr_name = clone
    and xpat_probe_zdb_id = mrkr_zdb_id
    and xpatstg_xpat_zdb_id = xpat_zdb_id

    and clone is not null
    and mrkr_name is not null

    and mrkr_zdb_id is not null
    and xpat_probe_zdb_id is not null

    and xpat_zdb_id  is not null
    and xpatstg_xpat_zdb_id is not null)
where clone in (select mrkr_name from tmp_mrkr where mrkr_name is not null)
and clone is not null
;


! echo "testing input for anatitem_overlaps_stg_window"

select count(*) all_anat_items from keywords_tmp; 

--select distinct clone,sstart,sstop from keywords_tmp;
! echo "find keywords asigned to a expression stage that has not been defined"
! echo "Erik has added constraints to the template so this can not happen anymore" 

select distinct clone nam,a.stg_name strt,b.stg_name stp 
from keywords_tmp,stage a, stage b 
where not exists (
    select 'foo' 
    from expression_pattern_stage -- tmp_exp_pat_stg
    where xpatstg_xpat_zdb_id  = clone
    and xpatstg_start_stg_zdb_id = sstart
    and xpatstg_end_stg_zdb_id = sstop
    )
and sstart = a.stg_zdb_id
and sstop = b.stg_zdb_id 
into temp addto_expresion with no log;

update addto_expresion set nam = 
    (select clone_polymerase_name from clone where nam = clone_mrkr_zdb_id)
where nam in (select clone_mrkr_zdb_id from clone)
; 

select * from addto_expresion;

unload to 'addto_expresion.unl' select * from addto_expresion;
drop table addto_expresion;

select count(*) from keywords_tmp;

select count(*) from keywords_tmp where keyword is null;

insert into expression_pattern_anatomy 
    select distinct clone,sstart,sstop,keyword 
    from keywords_tmp, expression_pattern_stage
    where keyword is not null 
    and sstart = xpatstg_start_stg_zdb_id
    and sstop = xpatstg_end_stg_zdb_id
    and clone = xpatstg_xpat_zdb_id
--in (select xpatstg_xpat_zdb_id from expression_pattern_stage)
    and anatitem_overlaps_stg_window(keyword,sstart,sstop)
;

--! echo "good anat items"
select count(*) good_anat_items 
from keywords_tmp 
where keyword is not null
    and sstart is not null
    and sstop is not null
    and clone in (select xpatstg_xpat_zdb_id from expression_pattern_stage)
    and anatitem_overlaps_stg_window(keyword,sstart,sstop);

! echo "BAD anat items"

select distinct clone,sstart,sstop,keyword 
from keywords_tmp
where  not exists (
    select * from expression_pattern_anatomy 
        where clone =  xpatanat_xpat_zdb_id   
        and keyword =  xpatanat_anat_item_zdb_id
        and sstart =   xpatanat_xpat_start_stg_zdb_id
        and sstop =    xpatanat_xpat_end_stg_zdb_id
    )
;

! echo "BAD anat items again"

unload to 'bad_exp_pat_anat.unl'
select clone,sstart,sstop,keyword 
from keywords_tmp
where  not exists (
    select * from expression_pattern_anatomy 
        where clone =  xpatanat_xpat_zdb_id   
        and keyword =  xpatanat_anat_item_zdb_id
        and sstart =   xpatanat_xpat_start_stg_zdb_id
        and sstop =    xpatanat_xpat_end_stg_zdb_id
    )
;


! echo "-- FISH_IMAGE --"
----------------

create temp table tmp_fish_image(
    fimg_zdb_id varchar(50)not null,
    fimg_image blob,
    fimg_annotation lvarchar,
    fimg_image_with_annotation blob,
    fimg_thumbnail blob ,
    fimg_width   integer default 1,
    fimg_height  integer default 1,
    fimg_fish_zdb_id varchar(50)not null,
    fimg_comments lvarchar default '',
    fimg_view varchar(20) ,
    fimg_direction varchar(30) ,
    fimg_form varchar(10) default NULL,
    fimg_preparation varchar(15) ,
    fimg_owner_zdb_id varchar(50)not null,
    fimg_external_name varchar(50)not null
    
) with no log in tempdbs1;

insert into tmp_fish_image
    select get_ID('IMAGE'),
    FILETOBLOB(im.imagename || '.jpg','client'),
    im.imagename ||'.txt',
    FILETOBLOB(im.imagename || '--C.jpg','client'),
    FILETOBLOB(im.imagename || '--t.jpg','client'),
    0, 
    0,
    'ZDB-FISH-010924-10',
    im.comments, -- null not allowed
    im.o_view,
    im.o_direction,
    'still',
    im.preparation,
    'ZDB-PERS-960805-556',
    im.imagename
    from images_tmp im, tmp_mrkr
    where mrkr_name = im.clone
;

! echo "add height and width to images"  
create temp table imagedim (name varchar(50), width integer, height integer) with no log;
load from 'imagedim.unl' insert into  imagedim;

update tmp_fish_image set fimg_width  = (
    select unique width  from imagedim where name = fimg_external_name) 
    where fimg_external_name in (select name from imagedim);

update tmp_fish_image set fimg_height = (
    select height from imagedim where name = fimg_external_name)
    where fimg_external_name in (select name from imagedim);

select count(*)zero_width from  tmp_fish_image where fimg_width <= 0;
select count(*)null_width from  tmp_fish_image where fimg_width is null;

delete from imagedim where name in (select fimg_external_name from tmp_fish_image);

select count(*)be_zero from  imagedim;
select name orphan_jpg from  imagedim;
drop table imagedim;

-- update tmp_fish_image set fimg_comments = (select ...)

insert into zdb_active_data select fimg_zdb_id from tmp_fish_image;
unload to 'foo_image_dump.unl' select fimg_view,fimg_direction,fimg_preparation,fimg_external_name from tmp_fish_image;

insert into fish_image select * from tmp_fish_image;

-- record_attribution --
insert into record_attribution select fimg_zdb_id, 'ZDB-PUB-010810-1','primary' from tmp_fish_image;


! echo "-- FISH_IMAGE_STAGE --"
------------------------------
-- getting an  error in this next sql
-- fimgstg_fimg_zdb_id_foregin_key


INSERT INTO fish_image_stage 
    SELECT distinct 
           fimg_zdb_id,
           xpatstg_start_stg_zdb_id,
           xpatstg_end_stg_zdb_id
    FROM 
        tmp_fish_image,
        tmp_exp_pat_stg,
        tmp_exp_pat,
        tmp_mrkr,
        images_tmp im
    WHERE mrkr_name = im.clone
    and xpat_probe_zdb_id = mrkr_zdb_id
    and xpatstg_xpat_zdb_id = xpat_zdb_id
    and fimg_external_name = im.imagename
    and im.sstart = xpatstg_start_stg_zdb_id
    and im.sstop  = xpatstg_end_stg_zdb_id
;


! echo "-- EXPRESSION_PATTERN_IMAGE --"
------------------------------

INSERT INTO expression_pattern_image 

    SELECT distinct 
           fimg_zdb_id,
           xpatstg_xpat_zdb_id,
           xpatstg_start_stg_zdb_id,
           xpatstg_end_stg_zdb_id
    FROM 
           tmp_fish_image,
	   tmp_mrkr,
	   tmp_exp_pat,
           tmp_exp_pat_stg,
	   images_tmp im
	
    WHERE  mrkr_name = im.clone
       and xpat_probe_zdb_id = mrkr_zdb_id
       and xpatstg_xpat_zdb_id = xpat_zdb_id
       and im.sstart = xpatstg_start_stg_zdb_id
       and im.sstop = xpatstg_end_stg_zdb_id
       and im.imagename = fimg_external_name
       and fimg_overlaps_stg_window(fimg_zdb_id,xpatstg_start_stg_zdb_id,xpatstg_end_stg_zdb_id)
;

! echo "capture any bad_exp_pat_img  that failed for the sake of morbid curosity"

create temp table bad_exp_pat_img (
    xpatanat_xpat_zdb_id varchar(50) not null,
    xpatanat_xpat_start_stg_zdb_id varchar(50) not null,
    xpatanat_xpat_end_stg_zdb_id varchar(50) not null,
    xpatanat_anat_item_zdb_id varchar(50) not null
) with no log;

insert into bad_exp_pat_img 

    SELECT distinct 
           fimg_zdb_id,
           xpatstg_xpat_zdb_id,
           xpatstg_start_stg_zdb_id,
           xpatstg_end_stg_zdb_id
    FROM 
        tmp_fish_image,
	tmp_mrkr,
	tmp_exp_pat,
        tmp_exp_pat_stg,
	images_tmp im
	
    WHERE mrkr_name = im.clone
	and xpat_probe_zdb_id = mrkr_zdb_id
        and xpatstg_xpat_zdb_id = xpat_zdb_id
	and im.sstart = xpatstg_start_stg_zdb_id
	and im.sstop = xpatstg_end_stg_zdb_id
	and im.imagename = fimg_external_name
	and not fimg_overlaps_stg_window(fimg_zdb_id,xpatstg_start_stg_zdb_id,xpatstg_end_stg_zdb_id)

;

unload to 'bad_exp_pat_img.unl' select * from bad_exp_pat_img;


----------------- Gene Relationships ----------------
create temp table relationship_tmp
  (
    cb_name	varchar(80),
    gene_abbrev	varchar(80)
  )
with no log;

create temp table mrel_tmp
  (
    zdb_id 	varchar(50),
    type	varchar(40),
    mrkr_1	varchar(50),
    mrkr_2	varchar(50),
    comments	lvarchar
  )
with no log;

load from is_gene.unl insert into relationship_tmp;

---------- load marker relationship -------------
insert 
  into mrel_tmp
select 
  get_id('MREL'),
  'gene encodes small segment', 
  m1.mrkr_zdb_id, 
  m2.mrkr_zdb_id, 
  'Thisse load ' || TODAY
from
  relationship_tmp,
  marker m1,
  marker m2
where
  gene_abbrev = m1.mrkr_abbrev
  and cb_name = m2.mrkr_name;
  

---------- data validation ----------
delete from relationship_tmp where cb_name in (select mrkr_name from marker, mrel_tmp where mrkr_2 = mrkr_zdb_id);

select * from relationship_tmp;

---------- insert marker relationship -----------------
insert 
  into zdb_active_data
select
  zdb_id
from 
  mrel_tmp;

insert 
  into marker_relationship
select
  *
from
  mrel_tmp;

insert 
  into record_attribution
select
  zdb_id, 'ZDB-PUB-010810-1', 'related'
from
  mrel_tmp;


--commit work;
