drop function gxp_load_func;

create function gxp_load_func (
			sbm_lab_name	varchar(50),
			sbm_person_id	varchar(50),
			sbm_pub_id	varchar(50),
			sbm_supplier_id	varchar(50),
			sbm_fish_line_id	varchar(50),
			sbm_fgene_prefix	varchar(5),
			sbm_release_type	varchar(5)
			)
	returning integer;  
		
 begin  -- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
   	

    define fdbcontZdbId	varchar(50);	
    define sbm_lab_id	varchar(50);
    define assayName	varchar(50);

	
    on exception
	set sqlError, isamError, errorText

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
                   ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/gxp_load_exception';
	system exceptionMessage;	

	system '/bin/chmod 666 /tmp/gxp_load_exception';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;
  
        return 1;

    end exception;

    begin work;

let errorHint = "get sumbit lab id";

select zdb_id
  into sbm_lab_id
  from lab
 where name like sbm_lab_name || "%";
  
let errorHint = "foreign_db_contains";
   
    select fdbcont_zdb_id
      into fdbcontZdbId
      from foreign_db_contains
     where fdbcont_fdb_db_name = "Genbank"
       and fdbcont_fdbdt_data_type = "cDNA";

let assayName = "RNA in situ whole mount";

-- MARKER --
------------------------

let errorHint = "marker";

create table tmp_marker(
    t_mrkr_zdb_id  	varchar(50) not null primary key,
    t_mrkr_name 	varchar(80) not null unique,
    t_mrkr_abbrev 	varchar(20),
    t_mrkr_type 	varchar(10),
    t_mrkr_owner 	varchar(50),
    t_mrkr_name_order 	varchar(140),
    t_mrkr_abbrev_order varchar(60),
    t_mrkr_comments 	lvarchar

  );  -- with no log;

-- get 'new est' markers into tmp_marker

insert into tmp_marker
    	 select get_id('EST'), 
	   	prb_clone_name, 
		prb_clone_name, 
		'EST', 
           	sbm_person_id, 
		zero_pad(prb_clone_name), 
		zero_pad(prb_clone_name), 
		prb_comments
    	   from probes_tmp 
   	  where prb_clone_name not in (
        		select mrkr_name 
          	 	  from marker
        		 where mrkr_type in ('EST','CDNA')
         		);

-- get exist markers (without xpat data) into tmp_updated_marker
-- with marker comments concatenated with new comments. 

select mrkr_zdb_id  as tu_mrkr_zdb_id, 
       mrkr_name    as tu_mrkr_name,
       mrkr_comments || " " || prb_comments as tu_mrkr_comments	
  from marker, probes_tmp
 where prb_clone_name = mrkr_name
   and mrkr_type in ('EST','CDNA')
 into temp tmp_updated_marker with no log;

update marker set mrkr_comments =(select tu_mrkr_comments 
				    from tmp_updated_marker 
				   where tu_mrkr_zdb_id = marker.mrkr_zdb_id ) 
	    where mrkr_zdb_id in (select tu_mrkr_zdb_id 
			  	    from tmp_updated_marker);


-- get exist markers with existing clone data into tmp_clone_data_exist_marker 
select mrkr_zdb_id as tcl_mrkr_zdb_id, 
       mrkr_name   as tcl_mrkr_name
  from probes_tmp, marker
 where prb_clone_name = mrkr_name
   and mrkr_zdb_id in (select clone_mrkr_zdb_id from clone)
into temp tmp_clone_data_exist_marker with no log; 


-- add the new active data zdbids
insert into zdb_active_data (zactvd_zdb_id) select t_mrkr_zdb_id from tmp_marker;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, 
		    mrkr_owner, mrkr_name_order, mrkr_abbrev_order, mrkr_comments) 
	select * from tmp_marker;

insert into record_attribution
   (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select t_mrkr_zdb_id, sbm_pub_id 
	from tmp_marker;

--per curator request,add attribution to exist ESTs
insert into record_attribution
   (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select tu_mrkr_zdb_id, sbm_pub_id 
	from tmp_updated_marker;


if (sbm_supplier_id <> "") then

  -- current rule: new EST/cDNA is attributed to the supplier, 
  -- exist ones only attributed if no supplier yet.

  if (sbm_lab_name == "Talbot") then

    -- with clone name, order at RZPD could be directed to the specific item
    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num)
  	select t_mrkr_zdb_id, sbm_supplier_id, t_mrkr_name
          from tmp_marker;

    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num)
  	select tu_mrkr_zdb_id, sbm_supplier_id, tu_mrkr_name
          from tmp_updated_marker
  	 where tu_mrkr_zdb_id not in (select idsup_data_zdb_id from int_data_supplier);

  else
    -- Thisse uses ZGC clone which should already exist in ZFIN with supplier defined, 
    -- here, we only assign I.M.A.G.E. consortium as supplier for the rest image clone.
  
    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id)
  	select t_mrkr_zdb_id, sbm_supplier_id
          from tmp_marker;

    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id)
  	select tu_mrkr_zdb_id, sbm_supplier_id
          from tmp_updated_marker
  	 where tu_mrkr_zdb_id not in (select idsup_data_zdb_id from int_data_supplier);
  end if

end if 


-- Fake Gene --
------------------
let errorHint = "fake gene";

-- new est that are not in is_gene.unl file gets a fake gene
create table tmp_fake_gene (
	t_mrkr_zdb_id 	varchar(50),
	t_mrkr_name 	varchar(80) not null,
    	t_mrkr_abbrev 	varchar(20) not null,
    	t_mrkr_type 	varchar(10),
    	t_mrkr_owner 	varchar(50),
    	t_mrkr_name_order 	varchar(140),
    	t_mrkr_abbrev_order varchar(60) not null,
    	t_mrkr_comments 	lvarchar
  );  -- with no log;

insert into tmp_fake_gene
  	 select get_id('GENE'), 
		sbm_fgene_prefix||t_mrkr_name,
		sbm_fgene_prefix||t_mrkr_abbrev, 
		'GENE', 
		sbm_person_id,
		zero_pad(sbm_fgene_prefix||t_mrkr_name),
        	zero_pad(sbm_fgene_prefix||t_mrkr_abbrev),
		"The prefix "|| sbm_fgene_prefix ||" indicates this gene is represented by an EST generated at the "|| sbm_lab_name ||"'s Lab"

   	   from tmp_marker
   	  where t_mrkr_name in (select prb_clone_name
				  from probes_tmp
				 where prb_gene_zdb_id is null);

-- there seems a trim function behind the "||" which has a restriction of length within 255, so split one step into two.
update tmp_fake_gene set t_mrkr_comments = 
		"This gene is characterized solely by an EST or collection of ESTs. When more is known about the gene, the current gene nomenclature based on the EST name will be replaced with more traditional zebrafish gene nomenclature. " || t_mrkr_comments;


insert into zdb_active_data (zactvd_zdb_id)
	select t_mrkr_zdb_id from tmp_fake_gene;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, 
		    mrkr_owner, mrkr_name_order, mrkr_abbrev_order, mrkr_comments) 
	select * from tmp_fake_gene;



-- Gene Relationships --
---------------------------
let errorHint = "marker relationship";

create table tmp_marker_relationship (
	t_mrel_zdb_id		varchar(50),
	t_mrel_type		varchar(50),
	t_mrel_mrkr1_zdb_id 	varchar(50) not null,
	t_mrel_mrkr2_zdb_id	varchar(50) not null,
	t_mrel_comments		lvarchar
); -- with no log;

-- known gene and clone assignments
insert into tmp_marker_relationship 
	 select get_id('MREL'), 'gene encodes small segment',
	        prb_gene_zdb_id, t_mrkr_zdb_id, sbm_lab_name||' load '||TODAY
	   from tmp_marker, probes_tmp
	  where t_mrkr_name = prb_clone_name
	    and prb_gene_zdb_id is not null ;

-- fake gene and clone relationship
insert into tmp_marker_relationship 
	 select get_id('MREL'), 'gene encodes small segment',
	        fgene.t_mrkr_zdb_id, est.t_mrkr_zdb_id, sbm_lab_name||' load '||TODAY
	   from tmp_marker est, tmp_fake_gene fgene
	  where fgene.t_mrkr_name = sbm_fgene_prefix || est.t_mrkr_name ;

insert into zdb_active_data (zactvd_zdb_id) 
		select t_mrel_zdb_id from tmp_marker_relationship;

-- add mrel records
insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, 
				 mrel_mrkr_2_zdb_id, mrel_comments)
		select * from tmp_marker_relationship;

-- attribute mrel records
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select t_mrel_zdb_id, sbm_pub_id from tmp_marker_relationship;


-- Data Alias --
-------------------
-- deleted as curator proves


-- DB_LINK --
---------------
let errorHint = "db_link";

create table tmp_db_link (
	t_dblink_zdb_id 	varchar(50),
	t_dblink_linked_recid	varchar(50) not null,
	t_dblink_acc_num	varchar(50),
	t_dblink_fdbcont_zdb_id	varchar(50),
	t_dblink_info		varchar(80)
) ; -- with no log;
	
insert into tmp_db_link 
	 select get_id('DBLINK'), 
		t_mrkr_zdb_id, 
		prb_gb5p, 
		fdbcontZdbId,
	       'gxp load'||TODAY
	  from tmp_marker, probes_tmp
  	 where t_mrkr_name = prb_clone_name
           and prb_gb5p is not null;
 
insert into tmp_db_link
	 select get_id('DBLINK'), 
		t_mrkr_zdb_id, 
		prb_gb3p, 
		fdbcontZdbId,
	        'gxp load'||TODAY 
	  from tmp_marker, probes_tmp
  	 where t_mrkr_name = prb_clone_name
           and prb_gb3p is not null;


insert into tmp_db_link
	 select get_id('DBLINK'), 
		tu_mrkr_zdb_id, 
		prb_gb5p, 
		fdbcontZdbId,
	        'gxp load'||TODAY 
	  from tmp_updated_marker, probes_tmp
  	 where tu_mrkr_name = prb_clone_name
           and prb_gb5p is not null
           and not exists (
			  select dblink_zdb_id 
			    from db_link
			   where dblink_linked_recid = tu_mrkr_zdb_id
			     and dblink_acc_num      = prb_gb5p
			 );
	
insert into tmp_db_link 
	 select get_id('DBLINK'), 
		tu_mrkr_zdb_id, 
		prb_gb3p, 
		fdbcontZdbId,
	        'gxp load'||TODAY
	  from tmp_updated_marker, probes_tmp
  	 where tu_mrkr_name = prb_clone_name
           and prb_gb3p is not null
           and not exists (
			select dblink_zdb_id 
			  from db_link
			 where dblink_linked_recid = tu_mrkr_zdb_id
			   and dblink_acc_num      = prb_gb3p
			 );

-- if a sent gb# is currently associated with the gene in zfin, drop that
-- since it should be associated with an EST/cDNA record
delete from db_link where exists (
		select prb_clone_name 
		  from probes_tmp
		 where ( prb_gb5p = db_link.dblink_acc_num 
		      or prb_gb3p = db_link.dblink_acc_num )
	           and db_link.dblink_linked_recid = prb_gene_zdb_id
                  );


insert into zdb_active_data (zactvd_zdb_id)
	select t_dblink_zdb_id from tmp_db_link;

insert into db_link (dblink_zdb_id,dblink_linked_recid,dblink_acc_num,
		     dblink_fdbcont_zdb_id, dblink_info)
	select * from tmp_db_link;

insert into tmp_marker (t_mrkr_zdb_id, t_mrkr_name)
	select tu_mrkr_zdb_id, tu_mrkr_name from tmp_updated_marker;


-- VECTOR, CLONE --
-----------------------

let errorHint = "vector, clone";
   
if (sbm_lab_name = "Thisse" AND sbm_release_type = "fr") then 
	update probes_tmp set prb_pcr_amp = "Reaction denatured 4 min. followed by PCR cycling 95°C 30s, 55°C 30s, 72°C 3 min. (at least 1 min. per kb) followed by elongation at 72°C 7 min.<br>" || prb_pcr_amp;

end if

{commented out
  create table tmp_clone (
    t_cln_zdb_id 	  varchar(50) not null,
    t_cln_comments 	  varchar(80),
    t_cln_name		  varchar(80) not null,
    t_cln_polymerase_name varchar(80),
    t_cln_insert_size 	  integer,
    t_cln_cloning_site    varchar(20),
    t_cln_digest 	  varchar(20),
    t_cln_probelib_zdb_id varchar(50),
    t_cln_sequence_type   varchar(20),
    t_cln_pcr_amp	  varchar(255),
    t_cln_is_chimeric 	  boolean
) ; -- with no log;
}

insert into vector (vector_name, vector_type_name)
	select distinct prb_vector, 'Plasmid' 
	  from probes_tmp 
	 where prb_vector not in (select vector_name from vector);

--if clone data already partly exist, Talbot data would provide polymerase and digest
-- Thisse data would also provide cloning site, insert size and PCR amplification

update clone set (clone_polymerase_name, 
		  clone_digest,
		  clone_insert_size,
	  	  clone_cloning_site,
		  clone_pcr_amplification
		  ) 
        =((select prb_polymerase, 
		  prb_digest, 
		  prb_insert_kb*1000,
		  prb_cloning_site,
		  prb_pcr_amp
	     from probes_tmp, tmp_clone_data_exist_marker
	    where prb_clone_name = tcl_mrkr_name
	      and tcl_mrkr_zdb_id = clone.clone_mrkr_zdb_id
				   ))
   where clone_mrkr_zdb_id in (select tcl_mrkr_zdb_id
  			         from tmp_clone_data_exist_marker); 

{commentted out
  update clone set (clone_insert_size, clone_cloning_site, clone_pcr_amplification) = 
				  ((select prb_insert_kb*1000, prb_cloning_site, prb_pcr_amp
					    from probes_tmp, tmp_clone_data_exist_marker
					   where prb_clone_name = mrkr_abbrev
					     and mrkr_zdb_id = clone.clone_mrkr_zdb_id
				   ))
	    where clone_mrkr_zdb_id in (select mrkr_zdb_id
					  from tmp_clone_data_exist_marker); 
}

insert into clone 
  (
    clone_mrkr_zdb_id,
    clone_comments,
    clone_vector_name,
    clone_polymerase_name,
    clone_insert_size,
    clone_cloning_site,
    clone_digest,
    clone_probelib_zdb_id,
    clone_sequence_type,
    clone_pcr_amplification,
    clone_is_chimeric
  )
  select distinct t_mrkr_zdb_id,
	 '',
	prb_vector,
	prb_polymerase,
	prb_insert_kb * 1000,
	prb_cloning_site,
	prb_digest,
	prb_library,
	'cDNA',
	prb_pcr_amp,
	'f'
    from probes_tmp, tmp_marker
   where prb_clone_name = t_mrkr_name
     and prb_clone_name not in (select tcl_mrkr_name from tmp_clone_data_exist_marker);

	
-- EXPRESSION PATTERN --
--------------------------------
let errorHint = "expression_pattern";

create table tmp_expression_pattern (
    t_xpat_zdb_id 		varchar(50) not null primary key,
    t_xpat_fish_zdb_id 		varchar(50) not null,
    t_xpat_assay_name 		varchar(40),
    t_xpat_direct_sbm_date  	DATETIME YEAR TO DAY,
    t_xpat_probe_zdb_id 	varchar(50),
    t_xpat_gene_zdb_id 		varchar(50) not null,
    t_xpat_source_zdb_id	varchar(50) not null,
    t_xpat_comments		lvarchar
	
) ; -- with no log;

insert into tmp_expression_pattern 
    	 select get_id('XPAT'), 
		sbm_fish_line_id, 
		assayName, 
		TODAY, 
		t_mrkr_zdb_id, 
		mrkr_zdb_id, 
		sbm_pub_id,
		""
    	   from tmp_marker, marker, marker_relationship
    	  where t_mrkr_name in 
		 (select exp_clone_name from expression_tmp)
      	    and t_mrkr_zdb_id = mrel_mrkr_2_zdb_id
      	    and mrkr_zdb_id   = mrel_mrkr_1_zdb_id
     	    and mrel_type in ("gene encodes small segment","gene hybridized by small segment");

if (sbm_lab_name = "Thisse" AND sbm_release_type = "fr") then 

	update tmp_expression_pattern set t_xpat_comments = "The cDNA sequences and in situ hybridizations for Fast Release clones (high throughput analysis) have not been double checked. Mistakes may occur. Please contact <A HREF='mailto:thisse@titus.u-strasbg.fr'>C and B Thisse</A> if you detect anything wrong. PCR protocol available on the probe details page.";
	
end if

insert into zdb_active_data select t_xpat_zdb_id from tmp_expression_pattern;

insert into expression_pattern (xpat_zdb_id, xpat_stock_zdb_id, xpat_assay_name,
                           xpat_direct_submission_date, xpat_probe_zdb_id, xpat_gene_zdb_id,
			   xpat_source_zdb_id, xpat_comments)
	select * from tmp_expression_pattern;

-- record_attribution, this would be a database trigger --
--insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) 
--	select t_xpat_zdb_id, sbm_pub_id
--	  from tmp_expression_pattern;

-- int_data_source --

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
	select t_xpat_zdb_id, sbm_lab_id 
	  from tmp_expression_pattern;




-- EXPRESSION_PATTERN_STAGE --
---------------------------------
let errorHint = "expression_pattern_stage";

-- temp table is used to minimize the table lock time,
-- also this tabling is changing to have a zdb_id

create table tmp_expression_pattern_stage(
    t_xpatstg_xpat_zdb_id 	varchar(50) not null,
    t_xpatstg_start_stg_zdb_id 	varchar(50) not null,
    t_xpatstg_end_stg_zdb_id 	varchar(50) not null,
    t_xpatstg_comments 		lvarchar,	
    t_xpatstg_expression_found	boolean not null
  ) ; -- with no log;

insert into tmp_expression_pattern_stage
   select distinct t_xpat_zdb_id, exp_sstart, exp_sstop, exp_description, exp_found
     from tmp_expression_pattern, expression_tmp, tmp_marker
    where t_xpat_probe_zdb_id = t_mrkr_zdb_id
      and t_mrkr_name = exp_clone_name;


insert into expression_pattern_stage (xpatstg_xpat_zdb_id, xpatstg_start_stg_zdb_id, 
			xpatstg_end_stg_zdb_id, xpatstg_comments, xpatstg_expression_found)
   select t_xpatstg_xpat_zdb_id, t_xpatstg_start_stg_zdb_id, t_xpatstg_end_stg_zdb_id,
    	  t_xpatstg_comments, t_xpatstg_expression_found
     from tmp_expression_pattern_stage;



-- EXPRESSION_PATTERN_ANATOMY --
---------------------------------
let errorHint = "expression_pattern_anatomy";

create table tmp_expression_pattern_anatomy(
    t_xpatanat_xpat_zdb_id 		varchar(50)not null,
    t_xpatanat_start_stg_zdb_id 	varchar(50)not null,
    t_xpatanat_end_stg_zdb_id 	varchar(50)not null,
    t_xpatanat_anat_item_zdb_id varchar(50)
  ); -- with no log;

insert into tmp_expression_pattern_anatomy
	select distinct t_xpat_zdb_id, kwd_sstart, kwd_sstop, kwd_keyword
  	  from tmp_marker, tmp_expression_pattern, keywords_tmp
 	 where t_mrkr_name = kwd_clone_name
   	   and t_mrkr_zdb_id = t_xpat_probe_zdb_id;

insert into expression_pattern_anatomy (
		xpatanat_xpat_zdb_id, xpatanat_xpat_start_stg_zdb_id, 
		xpatanat_xpat_end_stg_zdb_id, xpatanat_anat_item_zdb_id ) 
     	select * from tmp_expression_pattern_anatomy;



-- FISH_IMAGE --
----------------
let errorHint = "fish_image";

create table tmp_fish_image(
    t_fimg_zdb_id 		varchar(50)not null,
    t_fimg_image 		blob,
    t_fimg_annotation 		lvarchar,
    t_fimg_image_w_annot	blob,
    t_fimg_thumbnail 		blob ,
    t_fimg_width   		integer default 1 not null,
    t_fimg_height  		integer default 1 not null,
    t_fimg_fish_zdb_id 		varchar(50)not null,
    t_fimg_comments 		lvarchar default '',
    t_fimg_view 		varchar(20) ,
    t_fimg_direction 		varchar(30) ,
    t_fimg_form 		varchar(10) default NULL,
    t_fimg_preparation 		varchar(15) ,
    t_fimg_owner_zdb_id 	varchar(50)not null,
    t_fimg_external_name 	varchar(50)not null
    
)  PUT t_fimg_image_w_annot in (smartbs1, smartbs2, smartbs3, smartbs4)(log), 
	   t_fimg_image in (smartbs1, smartbs2, smartbs3, smartbs4)(log),
	   t_fimg_thumbnail in (smartbs1, smartbs2, smartbs3, smartbs4)(log);

if (sbm_lab_name = "Thisse" AND sbm_release_type = "cb") then 

    insert into tmp_fish_image
	select get_ID('IMAGE'),
    		FILETOBLOB(img_image_name || '.jpg','client','tmp_fish_image','t_fimg_image'),
    		'',
    		FILETOBLOB(img_image_name || '--C.jpg','client'),
    		FILETOBLOB(img_image_name || '--t.jpg','client','tmp_fish_image','t_fimg_thumbnail'),
    		imgdim_width,
    		imgdim_height,
    		sbm_fish_line_id,
    		img_comments, 
    		img_view,
    		img_orient,
    		'still',
    		img_preparation,
    		sbm_person_id,
    		img_image_name
    	   from images_tmp, tmp_marker, image_dim
          where t_mrkr_name   = img_clone_name
	    and imgdim_name   = img_image_name
	;

	
else 

    insert into tmp_fish_image(t_fimg_zdb_id, t_fimg_image, t_fimg_thumbnail,
			t_fimg_width, t_fimg_height, t_fimg_fish_zdb_id, t_fimg_comments, 
			t_fimg_view, t_fimg_direction, t_fimg_form, t_fimg_preparation, 
			t_fimg_owner_zdb_id, t_fimg_external_name)	
				
	select get_ID('IMAGE'),
    		FILETOBLOB(img_image_name || '.jpg','client','tmp_fish_image','t_fimg_image'),
    		FILETOBLOB(img_image_name || '--t.jpg','client','tmp_fish_image','t_fimg_thumbnail'),
    		imgdim_width,
    		imgdim_height,
    		sbm_fish_line_id,
    		img_comments || TODAY, 
    		img_view,
    		img_orient,
    		'still',
    		img_preparation,
    		sbm_person_id,
    		img_image_name
    	   from images_tmp, tmp_marker, image_dim
          where t_mrkr_name   = img_clone_name
	    and imgdim_name   = img_image_name
	;

    if (sbm_lab_name = "Talbot") then
	update tmp_fish_image set t_fimg_comments = "CEG load",
				  t_fimg_preparation = "whole-mount";
    end if	
		
end if

insert into zdb_active_data select t_fimg_zdb_id from tmp_fish_image;
insert into fish_image (
   	fimg_zdb_id,
    	fimg_image,
    	fimg_annotation,
    	fimg_image_with_annotation,
    	fimg_thumbnail,
    	fimg_width,
    	fimg_height,
    	fimg_fish_zdb_id,
    	fimg_comments,
    	fimg_view,
    	fimg_direction,
    	fimg_form,
    	fimg_preparation,
    	fimg_owner_zdb_id,
    	fimg_external_name)
    select * from tmp_fish_image;
  

-- record_attribution --
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) 
	select t_fimg_zdb_id, sbm_pub_id 
	  from tmp_fish_image;


-- FISH_IMAGE_STAGE --
-------------------------
let errorHint = "fish_image_stage";

insert into fish_image_stage (
	fimgstg_fimg_zdb_id,
	fimgstg_start_stg_zdb_id,
	fimgstg_end_stg_zdb_id )
     select t_fimg_zdb_id, img_sstart, img_sstop
       from tmp_fish_image, images_tmp, tmp_marker
      where t_mrkr_name = img_clone_name
        and t_fimg_external_name = img_image_name
     ;


-- EXPRESSION_PATTERN_IMAGE --
------------------------------
let errorHint = "expression_pattern_image";

insert into expression_pattern_image(
	xpatfimg_fimg_zdb_id,
	xpatfimg_xpat_zdb_id,
	xpatfimg_xpat_start_stg_zdb_id,
	xpatfimg_xpat_end_stg_zdb_id )
     select t_fimg_zdb_id, t_xpat_zdb_id, t_xpatstg_start_stg_zdb_id, t_xpatstg_end_stg_zdb_id
       from images_tmp, tmp_fish_image, tmp_expression_pattern_stage, tmp_marker, tmp_expression_pattern
      where img_image_name = t_fimg_external_name
        and img_clone_name = t_mrkr_name
        and t_mrkr_zdb_id = t_xpat_probe_zdb_id
        and t_xpat_zdb_id = t_xpatstg_xpat_zdb_id
	and img_sstart    = t_xpatstg_start_stg_zdb_id
        and img_sstop     = t_xpatstg_end_stg_zdb_id;



-- EXPRESSION AUTHORS --
--------------------------
let errorHint = "int_data_source";

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
	select distinct t_xpat_zdb_id, person.zdb_id
	  from person, authors_tmp, tmp_expression_pattern, tmp_marker
         where full_name = aut_author_name
           and t_xpat_probe_zdb_id = t_mrkr_zdb_id
	   and t_mrkr_name = aut_clone_name;



  commit work;

  end
  return 0;

end function;

update statistics for function gxp_load_func