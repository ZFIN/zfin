begin work;

-- PROBES_TMP --
--------------------

create table probes_tmp (
   prb_keyValue	 	varchar(50) not null primary key,
   prb_clone_name  	varchar(50) not null unique,
   prb_gene_zdb_id   	varchar(120) default null, --11/17 abbrev->ZDB-id, filled if available
   prb_gb5p 		varchar (50),
   prb_gb3p 		varchar (50),
   prb_library 		varchar(80),
   prb_digest 		varchar(20),
   prb_vector 		varchar(80),
   prb_pcr_amp 		lvarchar,
   prb_insert_kb 	float,
   prb_cloning_site 	varchar(20),
   prb_polymerase 	varchar(80),
   prb_comments 	lvarchar,               --> mrkr_comments
   prb_modified 	varchar(20)           -- not used  
);

load from './probes.unl' insert into probes_tmp;
 
update probes_tmp set prb_gb5p = upper(prb_gb5p);
update probes_tmp set prb_gb3p = upper(prb_gb3p);

create temp table acc_clone_tmp (
	accl_accession 		varchar(50) not null,
	accl_image_clone	varchar(50)
) with no log;

!echo "ok to have file not found if acc_imClone.unl not exists"
load from './acc_imClone.unl' insert into acc_clone_tmp;

update probes_tmp set prb_clone_name = (select accl_image_clone
					  from acc_clone_tmp
					 where accl_accession = prb_gb5p
                                            or accl_accession = prb_gb3p
					)
		where prb_gb5p in (select accl_accession from acc_clone_tmp)
		   or prb_gb3p in (select accl_accession from acc_clone_tmp); 

--when there exists an xpat using the same probe and is from direct submission, 
--it is highly possible to be from the same submission, so flag it as a problem 
--to be checked
UNLOAD TO 'exist_same_xpat_experiment_from_directsub.err'
	select prb_clone_name, xpat_zdb_id, xpat_stock_zdb_id, xpat_assay_name, xpat_source_zdb_id
	  from probes_tmp, expression_pattern, marker
	 where mrkr_zdb_id = xpat_probe_zdb_id
	   and mrkr_name = prb_clone_name
	   and xpat_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1", "ZDB-PUB-031103-24");

-- use alias name in ZFIN 
update probes_tmp set prb_library = "Zebrafish adult retina cDNA" 
		where prb_library = "Zebrafish adult retina";
update probes_tmp set prb_library = "Sugano Kawakami zebrafish DRA" 
		where prb_library = "Sugano-Kawakami DRA";

UNLOAD TO 'unknown_probelib.err' 
	select distinct prb_library 
	  from probes_tmp 
	 where prb_library not in (select probelib_name from probe_library);

update probes_tmp set prb_library = 
	(select probelib_zdb_id from probe_library where probelib_name = prb_library);


{ thisse load might report a bunch of inconsistency with ZGC data,
  we comment out this check since so far we trust exist data.

  UNLOAD TO 'clone_data_conflict.err' 
	select prb_clone_name, mrkr_zdb_id
	  from probes_tmp, marker, clone
	 where prb_clone_name = mrkr_abbrev
           and mrkr_zdb_id = clone_mrkr_zdb_id
	   and (lower(clone_vector_name) <> lower(prb_vector)
             or clone_probelib_zdb_id <> prb_library
	     or (clone_polymerase_name is not null 
                 and lower(clone_polymerase_name) <> lower(prb_polymerase))
	     or (clone_digest is not null 
                 and lower(clone_digest) <> lower(prb_digest))
	      ) ;
}

-- IS_GENE_TMP --
----------------------------

create temp table is_gene_tmp (
	isgn_accession	varchar(50) not null,
	isgn_gene_zdb_id	varchar(50) not null
)with no log;

!echo 'ok to have file not found if is_gene.unl not exists'
load from './is_gene.unl' insert into is_gene_tmp;

update probes_tmp set prb_gene_zdb_id = (select isgn_gene_zdb_id 
					   from is_gene_tmp
					  where isgn_accession = prb_gb5p 
					     or isgn_accession = prb_gb3p)
		where prb_gene_zdb_id is null
		  and (exists (select 'x'
				from is_gene_tmp
			       where prb_gb5p = isgn_accession)
		    or exists (select 'x'
				from is_gene_tmp
			       where prb_gb3p = isgn_accession)
		      )	;



-- EXPRESSION_TMP --
--------------------

create table expression_tmp(
  exp_clone_name	varchar (50) not null,
  exp_sstart 		varchar (50) not null,
  exp_sstop 		varchar (50) not null,
  exp_description 	lvarchar,
  exp_found		boolean  default 't',
  exp_keywords          lvarchar,
  exp_modified 		varchar (20)
);
ALTER TABLE expression_tmp add constraint (foreign key (exp_clone_name) references probes_tmp constraint exp_clone_name_foreign_key);

load from './expression.unl' insert into expression_tmp;

UNLOAD TO 'prb_without_xpat.err' 
	select prb_clone_name
	  from probes_tmp
	 where prb_keyValue not in 
		(select exp_clone_name 
		   from expression_tmp);

UNLOAD TO 'dup_xpat.err'
	select exp_clone_name, exp_sstart, exp_sstop 
	  from expression_tmp 
	group by 1, 2, 3 
	having count(*) > 1 ;

UNLOAD TO 'xpat_stg_unknown.err'
	select exp_clone_name, exp_sstart, exp_sstop
	  from expression_tmp
	 where exp_sstart not in (select stg_name from stage)
	   or exp_sstop not in (select stg_name from stage);


ALTER TABLE expression_tmp drop constraint exp_clone_name_foreign_key;
update expression_tmp set exp_clone_name = 
    (select prb_clone_name from probes_tmp where exp_clone_name = prb_keyValue);

update expression_tmp set exp_sstart = 
    (select stg_zdb_id from stage where stg_name = exp_sstart);
update expression_tmp set exp_sstop = 
    (select stg_zdb_id from stage where stg_name = exp_sstop);

 
-- temporary solution for combine keywords and comments from talbot lab data.
ALTER TABLE expression_tmp add exp_comment	lvarchar;
update expression_tmp set exp_comment = exp_description;


-- KEYWORDS_TMP --
------------------

create table keywords_tmp (
  kwd_clone_name 	varchar(50) not null,
  kwd_sstart 		varchar (50) not null,
  kwd_sstop 		varchar (50) not null,
  kwd_keyword 		varchar(50),        -- updated to anotitem_zdb_id
  kwd_modified 		varchar(20)  
);

ALTER TABLE keywords_tmp add constraint (foreign key (kwd_clone_name) references probes_tmp constraint kwd_clone_name_foreign_key);

load from './keywords.unl' insert into keywords_tmp;

delete from keywords_tmp where kwd_keyword is NULL; 

ALTER TABLE keywords_tmp drop constraint kwd_clone_name_foreign_key;
update keywords_tmp set kwd_clone_name = 
    (select prb_clone_name from probes_tmp where kwd_clone_name = prb_keyValue);

-- Some keywords have been deleted/modified from the some stages.
 
-- B        'Blastula:Sphere'
-- G        'Gastrula:50%-epiboly'             
-- ES       'Segmentation:1-somite'   
-- MS       'Segmentation:10-somite'
-- 24 h     'Segmentation:20-somite'   
-- 36 h     'Pharyngula:Prim-15'      
-- 48 h     'Pharyngula:High-pec' 
-- all stages

!echo "massive keywords updates:"

-- check for keywords that have been deleted
delete from keywords_tmp where kwd_keyword = 'ectoderm' 
    and kwd_sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
delete from keywords_tmp where kwd_keyword = 'endoderm' 
    and kwd_sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); --(36h, 48h)
delete from keywords_tmp where kwd_keyword = 'nasal pit' 
    and kwd_sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
delete from keywords_tmp where kwd_keyword = 'neural crest' 
    and kwd_sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); -- (36h, 48h)
delete from keywords_tmp where kwd_keyword = 'otic placode' 
    and kwd_sstart in ('Segmentation:1-somite'); -- (ES)
delete from keywords_tmp where kwd_keyword = 'primary motorneurons' 
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
delete from keywords_tmp where kwd_keyword = 'Rohon-Beard neurons' 
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
delete from keywords_tmp where kwd_keyword = 'YSL' 
    and kwd_sstart in ('Pharyngula:High-pec'); -- (48h)

--Keywords modified
update keywords_tmp set kwd_keyword = 'presumptive mesencephalon' 
    where kwd_keyword = 'presumptive mesencephalon (midbrain)' ;

update keywords_tmp set kwd_keyword = 'presumptive rhombencephalon' 
    where kwd_keyword = 'presumptive rhombencephalon (hindbrain)' ;

update keywords_tmp set kwd_keyword = 'presumptive prosencephalon' 
    where kwd_keyword = 'presumptive prosencephalon (forebrain)' ;

update keywords_tmp set kwd_keyword = 'neurocranium (chondrocranium)' 
    where kwd_keyword = 'neurocranium (condrocranium)' ;

update keywords_tmp set kwd_keyword = 'branchial arches' 
    where kwd_keyword = 'pharyngeal arches' 
    and kwd_sstart in ('Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (24h, 36h, 48h)
update keywords_tmp set kwd_keyword = 'presumptive central nervous system' 
    where kwd_keyword = 'neurectoderm' 
    and kwd_sstart in ('Segmentation:1-somite'); --  (ES);
update keywords_tmp set kwd_keyword = 'central nervous system' 
    where kwd_keyword = 'neurectoderm' 
    and kwd_sstart in ('Segmentation:14-somite','Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); --  ( MS, 24h, 36h, 48h)
update keywords_tmp set kwd_keyword = 'proctodeum' 
    where kwd_keyword = 'anus' 
    and kwd_sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); --  (36h, 48h)
update keywords_tmp set kwd_keyword = 'presumptive cephalic mesoderm'  
    where kwd_keyword = 'cephalic mesoderm'
    and kwd_sstart in ('Gastrula:50%-epiboly'); --  (G)
update keywords_tmp set kwd_keyword = 'presumptive cranial ganglia'
    where kwd_keyword = 'cranial ganglia' 
    and kwd_sstart in ('Segmentation:1-somite'); -- (ES)
update keywords_tmp set kwd_keyword = 'inner ear' 
    where kwd_keyword = 'ear'
    and kwd_sstart in ('Segmentation:14-somite','Segmentation:20-somite','Pharyngula:Prim-15','Pharyngula:High-pec'); -- (MS, 24h, 36h, 48h)
update keywords_tmp set kwd_keyword = 'presumptive hindbrain' 
    where kwd_keyword = 'hindbrain'
   and kwd_sstart in ('Segmentation:1-somite'); -- (ES)
-- In the anatomical dictionary, primordia is shown as a sub-part of lateral line. 
-- The kwd_keyword primordia is available for 24h, 36h, and 48h
update keywords_tmp set kwd_keyword = 'primordia' 
    where kwd_keyword = 'lateral line primordium'
    and kwd_sstart in ('Pharyngula:Prim-15','Pharyngula:High-pec'); -- (36h, 48h)
update keywords_tmp set kwd_keyword = 'muscle pioneers' 
    where kwd_keyword = 'muscle pioneer cells'
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set kwd_keyword = 'presumptive central nervous system'
    where kwd_keyword = 'neurectoderm' 
    and kwd_sstart in ('Segmentation:1-somite'); -- (ES)
update keywords_tmp set kwd_keyword = 'neuromasts'  
    where kwd_keyword = 'neuromast';
update keywords_tmp set kwd_keyword = 'presumptive neurons' 
    where kwd_keyword = 'neurons'
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set kwd_keyword = 'otic placode' 
    where kwd_keyword = 'otic vesicle'
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
--In the anatomical dictionary, segmental plate is a sub-part of paraxial mesoderm at these stages.
update keywords_tmp set kwd_keyword = 'segmental plate' 
    where kwd_keyword = 'paraxial segmental plate'
    and kwd_sstart in ('Segmentation:1-somite','Segmentation:14-somite','Segmentation:14-somite'); -- (ES, MS, 24h)    
update keywords_tmp set kwd_keyword = 'primary motoneurons' 
    where kwd_keyword = 'primary motorneurons'; 
update keywords_tmp set kwd_keyword = 'gut' 
    where kwd_keyword = 'primitive gut'
    and kwd_sstart in ('Segmentation:20-somite'); -- (24h)
update keywords_tmp set kwd_keyword = 'primordia' 
    where kwd_keyword = 'primordium'
    and kwd_sstart in ('Segmentation:20-somite'); -- (24h)
update keywords_tmp set kwd_keyword = 'rhombomeres r2-r6' 
    where kwd_keyword = 'rhombomeres r2-r8'
    and kwd_sstart in ('Segmentation:14-somite'); -- (MS)
update keywords_tmp set kwd_keyword = 'presumptive spinal cord' 
    where kwd_keyword = 'spinal cord'
    and kwd_sstart in ('Segmentation:1-somite','Segmentation:14-somite'); -- (ES, MS)
update keywords_tmp set kwd_keyword = 'trigeminal ganglions' 
    where kwd_keyword = 'trigeminal ganglion';
----

update keywords_tmp set kwd_sstart = 
    (select stg_zdb_id from stage where stg_name = kwd_sstart);
update keywords_tmp set kwd_sstop = 
    (select stg_zdb_id from stage where stg_name = kwd_sstop);

------this is a temporary solution for Talbot data. db schema will change soon to solve it nicely.
----- this would not affect thisse data
update expression_tmp set exp_description = (select kwd_keyword
					       from keywords_tmp
				              where kwd_clone_name = expression_tmp.exp_clone_name
						and kwd_sstart = expression_tmp.exp_sstart
						and kwd_sstop =  expression_tmp.exp_sstop
						and kwd_keyword in ("no expression detected","ubiquitously expressed"))
		    	where exp_clone_name in (select kwd_clone_name
					       from keywords_tmp
				              where kwd_clone_name = expression_tmp.exp_clone_name
						and kwd_sstart = expression_tmp.exp_sstart
						and kwd_sstop =  expression_tmp.exp_sstop
						and kwd_keyword in ("no expression detected","ubiquitously expressed")
						);

update expression_tmp set exp_description = exp_description || "<br>" || exp_comment
	  	    where exp_description <> exp_comment;
ALTER TABLE expression_tmp drop exp_comment;

update expression_tmp set exp_found = "f" 
		where exp_description like "no expression detected%";

delete from keywords_tmp where kwd_keyword in ("no expression detected","ubiquitously expressed");

------ end temp solution

-- has to be after the temp solution for talbot data
UNLOAD TO 'keywords_undef.err' 
   select kwd_clone_name, kwd_sstart, kwd_sstop, kwd_keyword 
     from keywords_tmp
    where kwd_keyword not in (select anatitem_name from anatomy_item);

update keywords_tmp set kwd_keyword = 
    (select anatitem_zdb_id from anatomy_item where anatitem_name = kwd_keyword);

UNLOAD TO 'keywords_stgerr.err' 
	select kwd_clone_name, kwd_sstart, kwd_sstop, kwd_keyword 
     	  from keywords_tmp
         where not anatitem_overlaps_stg_window(kwd_keyword,kwd_sstart,kwd_sstop); 

UNLOAD TO 'kwd_xpat_unconsist.err'
	select kwd_clone_name, kwd_sstart, kwd_sstop, kwd_keyword 
	  from keywords_tmp
	 where not exists (select * from expression_tmp 
			   where exp_clone_name = kwd_clone_name
			     and exp_sstart  = kwd_sstart
			     and exp_sstop   = kwd_sstop  );



-- IMAGES_TMP --
----------------

create table images_tmp (
  img_clone_name 	varchar (50) not null,
  img_image_name 	varchar (80) not null unique,
  img_sstart 		varchar (50),
  img_sstop		varchar (50),
  img_view 		varchar(20),
  img_orient 		varchar(60),
  img_preparation 	varchar(15),
  img_comments 		lvarchar,
  img_modified 		varchar (20)
);

ALTER TABLE images_tmp add constraint (foreign key (img_clone_name) references probes_tmp constraint img_clone_name_foreign_key);

load from './images.unl' insert into images_tmp;

ALTER TABLE images_tmp drop constraint img_clone_name_foreign_key;
update images_tmp set img_clone_name = 
    (select prb_clone_name from probes_tmp where img_clone_name = prb_keyValue);

update images_tmp set img_sstart = 
    (select stg_zdb_id from stage where stg_name = img_sstart);
update images_tmp set img_sstop = 
    (select stg_zdb_id from stage where stg_name = img_sstop);

update images_tmp set img_comments = "none given" where img_comments is NULL;  

insert into expression_tmp
    select distinct img_clone_name, img_sstart, img_sstop,"No comments.","t", "", TODAY 
      from images_tmp
     where not exists 
       ( select *
         from expression_tmp
         where img_clone_name = exp_clone_name
           and img_sstart = exp_sstart
       );

--find any invalid prepartions
--correct know tendencies. exp. whole mount -> whole-mount
update images_tmp
	  set img_preparation = "whole-mount"
	where img_preparation = "whole mount";

UNLOAD TO 'fimg_preparation_unknown.err'
	select img_clone_name, img_preparation from images_tmp where img_preparation not in (select fimgprep_name from fish_image_preparation);

UNLOAD TO 'fimg_view_unknown.err'
	select img_clone_name, img_view from images_tmp where img_view not in (select fimgview_name from fish_image_view);

UNLOAD TO 'img_xpat_unconsist.err'
	select img_clone_name, img_sstart, img_sstop
	  from images_tmp
	 where not exists (select * from expression_tmp 
			   where exp_clone_name = img_clone_name
			     and exp_sstart  = img_sstart
			     and exp_sstop   = img_sstop  );	

-- delete xpat data results from default value in Thisse template
UNLOAD TO "xpatstg_withno_kwd_img.del"
select exp_clone_name, exp_sstart, exp_sstop
  from expression_tmp
 where exp_found = "t"
   and exp_description = "No comments."
   and not exists (select *
		    from keywords_tmp
		   where exp_clone_name = kwd_clone_name
                     and exp_sstart     = kwd_sstart
		     and exp_sstop	= kwd_sstop
		 )
   and not exists (select *
                    from images_tmp
		   where exp_clone_name = img_clone_name
                     and exp_sstart     = img_sstart
		     and exp_sstop	= img_sstop
		 );

delete from expression_tmp 
   where exp_found = "t"
   and exp_description  = "No comments."
   and not exists (select *
		    from keywords_tmp
		   where exp_clone_name = kwd_clone_name
                     and exp_sstart     = kwd_sstart
		     and exp_sstop	= kwd_sstop
		 )
   and not exists (select *
                    from images_tmp
		   where exp_clone_name = img_clone_name
                     and exp_sstart     = img_sstart
		     and exp_sstop	= img_sstop
		 );


 --IMAGES_DIM--
-----------------

create table image_dim (
	imgdim_name varchar(50), 
	imgdim_width integer, 
	imgdim_height integer
);

load from './images.dim' insert into  image_dim;


--AUTHORS_TMP--
-----------------

create table authors_tmp (
	 aut_clone_name		varchar(50) not null,
	 aut_author_name	varchar(80) not null,
	 aut_modified		varchar(20) 
);

ALTER TABLE authors_tmp add constraint (foreign key (aut_clone_name) references probes_tmp constraint aut_clone_name_foreign_key);

load from './authors.unl' insert into authors_tmp;

ALTER TABLE authors_tmp drop constraint aut_clone_name_foreign_key;
update authors_tmp set aut_clone_name = 
    (select prb_clone_name from probes_tmp where aut_clone_name = prb_keyValue);

UNLOAD TO 'author_dup.err' 
	select aut_clone_name, aut_author_name
	  from authors_tmp
	group by 1, 2
 	 having count(*) > 1;

UNLOAD TO 'non_zfin_author.err'
	select aut_clone_name, aut_author_name
	  from authors_tmp
	 where aut_author_name not in (select full_name from person);


commit work;
--rollback work;

