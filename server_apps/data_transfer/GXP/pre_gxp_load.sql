---------------------------------------------------------------------------
-- The file does quality ensurance and quality checks of the incoming
-- gene expression data. It creates temporary tables to hold the data,
-- makes literal level changes according to ZFIN db constraints/concensus/defs,
-- and detects major offenses to table constraints, such as duplication on 
-- primary key field.
--
-- INPUT:
--    probes.unl
--    expression.unl
--    images.unl
--    authors.unl
--
--  Optional:
--    acc_imClone.unl: GB acc and image clone name
--    
-- OUTPUT:
--    STDOUT: sql execution output
--    ???.err: its name root indicates the error/problem in the file
--
-- EFFECT:
--    a group of *_tmp tables are created, and loaded with *.unl files.
--    These tables would be dropped in case of any output in .err file(s).
--    Otherwise, they will be used by the gxp_load_func.sql to load into
--    ZFIN db and dropped afterwards. The whole process has to be run
--    manually, at the end, the person has the option of keeping the 
--    tables around for debugging and drop it later using post_gxp_load.sql
--    when finish. 
--    No changes to ZFIN permanent tables is happening here.
----------------------------------------------------------------------      

begin work;

----------------------------------------------------------------
--   PROBES_TMP   --
--
----------------------------------------------------------------

create table probes_tmp (
   prb_keyValue	 	varchar(50) not null primary key,
   prb_clone_name  	varchar(50) not null,
   prb_gene_zdb_id   	varchar(120) default null, 
   prb_gb5p 		varchar (50),
   prb_gb3p 		varchar (50),
   prb_library 		varchar(80),
   prb_digest 		varchar(20),
   prb_vector 		varchar(80),
   prb_pcr_amp 		lvarchar,
   prb_insert_size 	integer,
   prb_cloning_site 	varchar(20),
   prb_polymerase 	varchar(80),
   prb_comments 	lvarchar,               --> mrkr_comments
   prb_rating           integer,
   prb_modified 	varchar(20)           -- not used  
) in tbldbs2
extent size 256 next size 256;

create unique index probes_tmp_clone_name_index on probes_tmp(prb_clone_name);
 
load from './probes.unl' insert into probes_tmp;

-- Data adjustment
--------------------- 
update probes_tmp set prb_gb5p = upper(prb_gb5p);
update probes_tmp set prb_gb3p = upper(prb_gb3p);
update probes_tmp set prb_library = "Zebrafish adult retina cDNA" 
		where prb_library = "Zebrafish adult retina";
update probes_tmp set prb_library = "Sugano Kawakami zebrafish DRA" 
		where prb_library = "Sugano-Kawakami DRA";


-- Update clone name
-----------------------
    -------------------------------------------
    --  ACC_CLONE_TMP
    --
    -- we decided to not use thisse FR# name as  
    -- ZFIN name, in case of ZGC data, the clone
    -- is in ZFIN; in case of GIS image clone,
    -- additional step is taken to find out clone
    -- name based on GenBank accession, and stored
    -- in acc_imClone.unl file. Use that information
    -- to update probes_tmp table.
    -----------------------------------------------
create temp table acc_clone_tmp (
	accl_accession 		varchar(50) not null,
	accl_image_clone	varchar(50)
) with no log;

!echo "ok to have file not found if acc_imClone.unl not exists"
load from './acc_imClone.unl' insert into acc_clone_tmp;

unload to 'dupacc.unl' select * from acc_clone_tmp,probes_tmp where accl_accession=prb_gb5p;
update probes_tmp set prb_clone_name = (select distinct accl_image_clone
					  from acc_clone_tmp
					 where accl_accession = prb_gb5p
                                            or accl_accession = prb_gb3p
					)
		where prb_gb5p in (select accl_accession from acc_clone_tmp)
		   or prb_gb3p in (select accl_accession from acc_clone_tmp); 

-- Fill in gene field
----------------------
    --------------------------------------------
    -- IS_GENE_TMP 
    -- 
    -- When the probe/clone is not in ZFIN yet,
    -- like Thisse GIS data, we run blast to find 
    -- out gene match as much as possible, and 
    -- save the GenBank acc and ZFIN gene zdb id
    -- in is_gene.unl file. While for thisse ZGC
    -- data, this step is not necessary and so that 
    -- no is_gene.unl is available. 
    -----------------------------------------------

create temp table is_gene_tmp (
	isgn_accession	varchar(50) not null,
	isgn_gene_zdb_id	varchar(50) not null
)with no log;

!echo 'ok to have file not found if is_gene.unl not exists'
load from './is_gene.unl' insert into is_gene_tmp;

update probes_tmp set prb_gene_zdb_id = (select distinct isgn_gene_zdb_id 
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


update probes_tmp set prb_library = 
	(select probelib_zdb_id 
           from probe_library 
          where probelib_name = prb_library);

-- use "unknown". During the loading, if the clone already exist,
-- the existing probe library would be used instead.
update probes_tmp set prb_library = "ZDB-PROBELIB-040512-1"
	        where prb_library is null;

	
-- Check data error 
--------------------
-- The probes and its encoding genes would both have the expression
-- data attached. For existing probes, if no existing encode relationship,
-- curators need to look at it and make correction.
UNLOAD TO 'probe_without_encoding_gene.err' 
	select prb_clone_name
    	  from probes_tmp
         where exists 
		(select 't' 
		   from marker
		  where mrkr_name = prb_clone_name)
           and not exists 
		(select 't' 
		   from marker, marker_relationship
		  where mrkr_zdb_id = mrel_mrkr_2_zdb_id
		    and mrel_type = "gene encodes small segment"
		    and mrkr_name = prb_clone_name);

--When there exists an xpat using the same probe and is from direct submission,
--it is highly possible that the data is from the same submission, which is 
-- problematic. Flag it.
UNLOAD TO 'exist_same_xpat_experiment_from_directsub.err'
       select prb_clone_name, xpatex_zdb_id, xpatex_genox_zdb_id, xpatex_assay_name, xpatex_source_zdb_id
         from probes_tmp, expression_experiment, marker
        where mrkr_zdb_id = xpatex_probe_feature_zdb_id
          and mrkr_name = prb_clone_name
          and xpatex_source_zdb_id in ("ZDB-PUB-040907-1", "ZDB-PUB-010810-1", "ZDB-PUB-031103-24", "ZDB-PUB-051025-1", "ZDB-PUB-080227-22");


-- use the "unknown" if probe library is not provided.
-- we hardcode the zdb id since there is equal chance of the name to 
-- be changed as the id. 
update probes_tmp set prb_library = "ZDB-PROBELIB-040512-1"
                where prb_library is null;

UNLOAD TO 'unknown_gene_id.err'
	select prb_clone_name,prb_gene_zdb_id
	  from probes_tmp
         where prb_gene_zdb_id is not null
	   and not exists (select 't' 
			     from marker
	                    where mrkr_zdb_id = prb_gene_zdb_id);

-------------------------------------------------------------------
--   EXPRESSION_TMP                                              --
-- 
-- Each keyword has an entry. The description field has to be
-- identical among entries with the same clone and stage range,
-- because it will be grouped by as figure caption for a particular
-- clone at the particular stage range
-------------------------------------------------------------------

create table expression_tmp(
  exp_clone_name	varchar (50) not null,
  exp_sstart 		varchar (50) not null,
  exp_sstop 		varchar (50) not null,
  exp_description 	lvarchar,    
  exp_found		boolean  default 't',
  exp_keyword           varchar (80),
  exp_modified 		varchar (20)
)
in tbldbs1
extent size 256 next size 256;

-- exp_clone_name column, in Thisse case which is what all we have now,
-- is loaded with keyValue first and updated into clone name in this file
-- and so be prepared for loading into ZFIN. We don't expect the foreign
-- key constraint to be violated, but we do want to catch that if it indeed 
-- happened oddly. 
ALTER TABLE expression_tmp add constraint (foreign key (exp_clone_name) references probes_tmp constraint exp_clone_name_foreign_key);

load from './expression.unl' insert into expression_tmp;

-- Check data error 
-----------------------
UNLOAD TO 'prb_without_xpat.err' 
	select prb_clone_name
	  from probes_tmp
	 where prb_keyValue not in 
		(select exp_clone_name 
		   from expression_tmp);

UNLOAD TO 'dup_xpat.err'
	select exp_clone_name, exp_sstart, exp_sstop, exp_keyword 
	  from expression_tmp 
	group by 1, 2, 3, 4 
	having count(*) > 1 ;

-- not a problem with Thisse data, but since this is a generic script
-- and we are wishing to have more GXP providers, we include this check.
UNLOAD TO 'xpat_stg_unknown.err'
	select exp_clone_name, exp_sstart, exp_sstop
	  from expression_tmp
	 where exp_sstart not in (select stg_name from stage)
	    or exp_sstop not in (select stg_name from stage);


-- Data adjustment
-----------------------

ALTER TABLE expression_tmp drop constraint exp_clone_name_foreign_key;
update expression_tmp set exp_clone_name = 
    (select prb_clone_name from probes_tmp where exp_clone_name = prb_keyValue);

update expression_tmp set exp_sstart = 
    (select stg_zdb_id from stage where stg_name = exp_sstart);
update expression_tmp set exp_sstop = 
    (select stg_zdb_id from stage where stg_name = exp_sstop);


-- Post adjustment error check
--------------------------------

update expression_tmp 
   set exp_keyword = 
           (select anatitem_zdb_id from anatomy_item where anatitem_name = exp_keyword)
 where exp_keyword in  (select anatitem_name from anatomy_item);

UNLOAD TO "keyword_is_alias_to_multiple_ao.err"
   select exp_clone_name, exp_sstart, dalias_alias
     from data_alias, expression_tmp 
    where dalias_alias = exp_keyword
      and dalias_data_zdb_id like "ZDB-ANAT-%"
  group by exp_clone_name, exp_sstart, dalias_alias
  having count(*) > 1;

-- if got 284 error, check the keyword_is_alias_to_multiple_ao.err
-- and fix the keyword in the expression.unl file.
update expression_tmp 
   set exp_keyword = 
        (select dalias_data_zdb_id from data_alias 
          where dalias_alias = exp_keyword
            and dalias_data_zdb_id like "ZDB-ANAT-%"
         )
  where exp_keyword in
        (select dalias_alias from data_alias where dalias_data_zdb_id like "ZDB-ANAT-%") ;

UNLOAD TO 'keywords_undef.err' 
   select distinct  exp_keyword, exp_sstart, exp_sstop
     from expression_tmp
    where exp_keyword not like "ZDB-ANAT-%";

UNLOAD TO 'keywords_stgerr.err' 
	select exp_clone_name, 
	       (select stg_name from stage where stg_zdb_id = exp_sstart),
	       (select stg_name from stage where stg_zdb_id = exp_sstop), 
	       exp_keyword 
     	  from expression_tmp
         where not anatitem_overlaps_stg_window(exp_keyword,exp_sstart,exp_sstop); 


----------------------------------------------------------------
-- IMAGES_TMP --
----------------------------------------------------------------

create table images_tmp (
  imgt_clone_name 	varchar (50) not null,
  imgt_image_name 	varchar (80) not null unique, --no extention
  imgt_sstart 		varchar (50),
  imgt_sstop		varchar (50),
  imgt_view 		varchar(20),
  imgt_orient 		varchar(60),
  imgt_preparation 	varchar(15),
  imgt_comments 		lvarchar,
  imgt_modified 		varchar (20)
  -- imgt_zdb_id  varchar(50) is added as below
)in tbldbs3
extent size 256 next size 256;
-- key_clone_name column, in Thisse case which is what all we have now,
-- is loaded with keyValue first and updated into clone name in this file
-- and so be prepared for loading into ZFIN. We don't expect the foreign
-- key constraint to be violated, but we do want to catch that if it indeed 
-- happened oddly. 
ALTER TABLE images_tmp add constraint (foreign key (imgt_clone_name) references probes_tmp constraint imgt_clone_name_foreign_key);

load from './images.unl' insert into images_tmp;

-- Data Adjustment
----------------------

ALTER TABLE images_tmp drop constraint imgt_clone_name_foreign_key;
update images_tmp set imgt_clone_name = 
    (select prb_clone_name from probes_tmp where imgt_clone_name = prb_keyValue);

update images_tmp set imgt_sstart = 
    (select stg_zdb_id from stage where stg_name = imgt_sstart);
update images_tmp set imgt_sstop = 
    (select stg_zdb_id from stage where stg_name = imgt_sstop);

-- adjust known naming difference 
update images_tmp
	  set imgt_preparation = "whole-mount"
	where imgt_preparation = "whole mount";

-- the defaul img_preparation is "whole-mount"
update images_tmp
	  set imgt_preparation = "whole-mount"
	where imgt_preparation is null;

-- prepare for image renaming
ALTER TABLE images_tmp add imgt_zdb_id varchar(50);
update images_tmp set imgt_zdb_id = get_id ("IMAGE");
UNLOAD TO 'img_oldname_2_newname.txt'
	select imgt_image_name, imgt_zdb_id 
 	  from images_tmp;

-- Error check
---------------------

UNLOAD TO 'img_preparation_unknown.err'
	select imgt_clone_name, imgt_preparation from images_tmp where imgt_preparation not in (select imgprep_name from image_preparation);

UNLOAD TO 'img_view_unknown.err'
	select imgt_clone_name, imgt_view from images_tmp where imgt_view not in (select imgview_name from image_view);

UNLOAD TO 'img_xpat_inconsist.err'
	select imgt_clone_name, imgt_sstart, imgt_sstop
	  from images_tmp
	 where not exists (select * from expression_tmp 
			   where exp_clone_name = imgt_clone_name
			     and exp_sstart  = imgt_sstart
			     and exp_sstop   = imgt_sstop  );	

-- delete xpat data resulted from default value in the template.
-- They are identified by default expression found, no keywords,
-- no comments, no images. The same type of data also is generally
-- not useful. Thus, we save a copy in file and then drop them. 
UNLOAD TO "xpatstg_withno_kwd_img.del"
select exp_clone_name, exp_sstart, exp_sstop
  from expression_tmp
 where exp_found = "t"
   and exp_description is null
   and exp_keyword = "ZDB-ANAT-041102-1" --unspecified
   and not exists (select *
                    from images_tmp
		   where exp_clone_name = imgt_clone_name
                     and exp_sstart     = imgt_sstart
		     and exp_sstop	= imgt_sstop
		 );

delete from expression_tmp 
   where exp_found = "t"
   and exp_description is null
   and exp_keyword = "ZDB-ANAT-041102-1"    --unspecified
   and not exists (select *
                    from images_tmp
		   where exp_clone_name = imgt_clone_name
                     and exp_sstart     = imgt_sstart
		     and exp_sstop	= imgt_sstop
		 );

---------------------------------------------------
 --   IMAGES_DIM   --
---------------------------------------------------

create table image_dim (
	imgdim_name varchar(50), 
	imgdim_width integer, 
	imgdim_height integer
) in tbldbs3
extent size 256 next size 256
;

load from './images.dim' insert into  image_dim;

---------------------------------------------------
--  AUTHORS_TMP  --
---------------------------------------------------

create table authors_tmp (
	 aut_clone_name		varchar(50) not null,
	 aut_author_name	varchar(80) not null,
	 aut_modified		varchar(20) 
)in tbldbs2
extent size 256 next size 256 ;

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

