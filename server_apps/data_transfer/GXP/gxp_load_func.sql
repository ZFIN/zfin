-------------------------------------------------------------------------
-- The function only executes on the basis of a pre_gxp_load.sql run which  
-- builds up tables that are used in this function call. It distributes
-- the content in the temporarily created tables into ZFIN permanent tables.
--
-- INPUT VARS:
--	sbm_lab_name
--	sbm_person_id :  as owner of new marker and image record	
--	sbm_pub_id   	
--	sbm_supplier_id : lab that supplier clone data	
--	sbm_fish_line_id
--      sbm_featexp_zdb_id	
--	sbm_fgene_prefix: prefix for place holder genes	
--	sbm_release_type	
--
-- INPUT:
--      probes_tmp 
--      expression_tmp
--      images_tmp
--      authors_tmp
--     
-- RETURN:
--      0:  success
--      1:  check /tmp/gxp_load_exception for error 
--     	  
-- EFFECT:
--      Gene expression data get loaded into ZFIN.
--      Affected tables: marker, expression_experiment, expression_result,
--      fish_images, figure expression_pattern_figure,record_attribution,
--      zdb_active_data. 
-- 
--      A group of tables are created for processing purpose. To ease 
--      debugging, 'permanent' tables are used. In case of success, the 
--      calling script, load_gxp.sh, will accept user's input on whether 
--      to drop these tables or keep around. Person that run the load is 
--      responsible to drop these tables after debugging using 
--      post_gxp_load.sql.In case of failure, the transaction rolls back, 
--      the tables would be cleaned up by that.
--------------------------------------------------------------------------
-- this function is currently scheduled to be dropped after each execution
-- in load_gxp.sh 

create function gxp_load_func (
		sbm_lab_name	  varchar(80),
		sbm_release_type  varchar(5),
		sbm_person_id	  like person.zdb_id,	
		sbm_pub_id	  like publication.zdb_id,
		sbm_supplier_id	  varchar(50),
		sbm_fish_line_id  like fish.zdb_id,
		sbm_featexp_zdb_id  like feature_experiment.featexp_zdb_id,
		sbm_fgene_prefix  varchar(5),
		sbm_assay_name    varchar(50)
 	)
	returning integer;  
		
 begin  -- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);	

    define fdbcontZdbId	like foreign_db_contains.fdbcont_zdb_id;	
    define sbm_lab_id	like lab.zdb_id;
    define lastProbe    like marker.mrkr_zdb_id;
    define labelCounter integer;
    define fishImageForm like fish_image.fimg_form;
    define curProbe	like marker.mrkr_zdb_id;
    define curFigZdbId  like figure.fig_zdb_id;
    define curCaption   like figure.fig_caption;
    define curStgHour   like stage.stg_hours_start;
    define originatorNote	lvarchar;	

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
  
        return -1;

    end exception;

    begin work;

let fishImageForm = "still";

let errorHint = "get sumbitter's lab id";

-- in Thisses' case, lab name is "Thisses' Lab"
select zdb_id
  into sbm_lab_id
  from lab
 where name like sbm_lab_name || "%";
  
let errorHint = "get foreign db contains zdb id";
   
select fdbcont_zdb_id 
  into fdbcontZdbId
  from foreign_db_contains
 where fdbcont_fdb_db_name = "GenBank"
   and fdbcont_fdbdt_data_type = "cDNA";

-------------------------------------------------------------
--   MARKER    RECORD_ATTRIBUTION    ZDB_ACTIVE_DATA      --
-------------------------------------------------------------
let errorHint = "marker";

create table tmp_gxp_marker(
    t_mrkr_zdb_id  	varchar(50) not null primary key,
    t_mrkr_name 	varchar(255) not null unique,
    t_mrkr_abbrev 	varchar(40),
    t_mrkr_type 	varchar(10),
    t_mrkr_owner 	varchar(50),
    t_mrkr_comments 	lvarchar
  );  

------------------------
-- New ESTs
------------------------
let errorHint = "new ESTs";
-- Put brand new ESTs from probes_tmp into tmp_gxp_marker. We do get expression 
-- data for cDNA, but so far and in the foreseeable future, those data
-- is expected to be already in ZFIN at the time of GXP load.

insert into tmp_gxp_marker
    	 select get_id('EST'), 
	   	prb_clone_name, 
		prb_clone_name, 
		'EST', 
           	sbm_person_id, 
		prb_comments
    	   from probes_tmp 
   	  where prb_clone_name not in (
        		select mrkr_name 
          	 	  from marker
        		 where mrkr_type in ('EST','CDNA')
         		);

------------------------
-- Exist ESTs/cDNAs
------------------------
let errorHint = "exist ESTs/cDNAs";
-- Put existing ESTs/cDNAs from probes_tmp into tmp_gxp_updated_marker.
-- The content would be moved to tmp_gxp_marker later in this script.
-- Update those marker's comments to have new comments appended.

select mrkr_zdb_id  as tu_mrkr_zdb_id, 
       mrkr_name    as tu_mrkr_name,
       mrkr_comments || " " || prb_comments as tu_mrkr_comments	
  from marker, probes_tmp
 where prb_clone_name = mrkr_name
   and mrkr_type in ('EST','CDNA')
 into temp tmp_gxp_updated_marker with no log;

update marker set mrkr_comments =(select tu_mrkr_comments 
				    from tmp_gxp_updated_marker 
				   where tu_mrkr_zdb_id = marker.mrkr_zdb_id ) 
	    where mrkr_zdb_id in (select tu_mrkr_zdb_id 
			  	    from tmp_gxp_updated_marker);

------------------------
-- Into DB
------------------------
-- add the brand new ESTs into zdb active data, marker and record attribution
insert into zdb_active_data (zactvd_zdb_id) 
	select t_mrkr_zdb_id from tmp_gxp_marker;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, 
		    mrkr_owner, mrkr_comments) 
	select * from tmp_gxp_marker;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select t_mrkr_zdb_id, sbm_pub_id 
	  from tmp_gxp_marker;

-- add attribution to existing ESTs
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select tu_mrkr_zdb_id, sbm_pub_id 
	  from tmp_gxp_updated_marker;


-------------------------------------------------------------
--             INT_DATA_SUPPLIER                        --
-------------------------------------------------------------
let errorHint = "int_data_supplier";

-- It is possible that we get clone data that do not have a supplier.
if (sbm_supplier_id <> "") then

  -- current rule: new EST/cDNA is attributed to the supplier, 
  -- exist ones only attributed if no supplier yet.

  if (sbm_lab_name == "Talbot") then

    -- with clone name, ordering at RZPD could be directed to the specific item
    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num)
  	select t_mrkr_zdb_id, sbm_supplier_id, t_mrkr_name
          from tmp_gxp_marker;

    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id, idsup_acc_num)
  	select tu_mrkr_zdb_id, sbm_supplier_id, tu_mrkr_name
          from tmp_gxp_updated_marker
  	 where tu_mrkr_zdb_id not in (select idsup_data_zdb_id from int_data_supplier);

  else  
    -- Thisses' ZGC clone would have already exist in ZFIN with supplier defined. 
    -- Here, we would assign I.M.A.G.E. consortium for the GIS image clone.
  
    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id)
  	select t_mrkr_zdb_id, sbm_supplier_id
          from tmp_gxp_marker;

    insert into int_data_supplier (idsup_data_zdb_id, idsup_supplier_zdb_id)
  	select tu_mrkr_zdb_id, sbm_supplier_id
          from tmp_gxp_updated_marker
  	 where tu_mrkr_zdb_id not in 
			(select idsup_data_zdb_id 
			   from int_data_supplier);
  end if

end if 

----------------------
-- Fake Gene --
----------------------
let errorHint = "fake gene";

if (sbm_fgene_prefix == "sb:" and sbm_release_type == "eu") then
   let originatorNote = "a genomic sequence submitted by the Thisse Lab";

elif (sbm_fgene_prefix == "sb:") then
   let originatorNote = "an EST generated at the Thisse Lab";

elif (sbm_fgene_prefix == "wu:") then 
   let originatorNote = "an EST generated at Washington University School of Medicine";

elif (sbm_fgene_prefix == "im:") then
   let originatorNote = "an EST generated by the I.M.A.G.E. Consortium"; 

end if 



-- new ESTs that don't match an existing ZFIN gene get to create a fake gene

create table tmp_gxp_fake_gene (
	t_fgene_zdb_id 	varchar(50),
	t_fgene_probe_zdb_id	varchar(50),
	t_fgene_name 	varchar(255) not null,
    	t_fgene_abbrev 	varchar(40) not null,
    	t_fgene_type 	varchar(10),
    	t_fgene_owner 	varchar(50),
    	t_fgene_comments 	lvarchar
  );  

insert into tmp_gxp_fake_gene
  	 select get_id('GENE'), t_mrkr_zdb_id,
		case when sbm_fgene_prefix = "im:"  --est: IMAGE:####
                     then sbm_fgene_prefix||t_mrkr_name[7,13]
                else sbm_fgene_prefix||t_mrkr_name
                end,
                case when sbm_fgene_prefix = "im:"  --est: IMAGE:####
                      then sbm_fgene_prefix||t_mrkr_name[7,13]
                else sbm_fgene_prefix||t_mrkr_name
                end, 
		'GENE', 
		sbm_person_id,
		"The prefix "|| sbm_fgene_prefix ||" indicates this gene is represented by "|| originatorNote 

   	   from tmp_gxp_marker
   	  where t_mrkr_name in (select prb_clone_name
				  from probes_tmp
				 where prb_gene_zdb_id is null);

-- There seems a trim function behind the "||" which has a length restriction
-- of 255, Thus we have to split one step into two.

if (sbm_fgene_prefix == "sb:" and sbm_release_type == "eu") then
  update tmp_gxp_fake_gene set t_fgene_comments = 
		"This gene is characterized solely by a PCR fragment. When more is known about the gene, the current gene nomenclature based on the placeholder name will be replaced with more traditional zebrafish gene nomenclature. " || t_fgene_comments
	                  where t_fgene_name like "sb:eu%";

else
   update tmp_gxp_fake_gene set t_fgene_comments = 
		"This gene is characterized solely by an EST or collection of ESTs. When more is known about the gene, the current gene nomenclature based on the EST name will be replaced with more traditional zebrafish gene nomenclature. " || t_fgene_comments;

end if 

insert into zdb_active_data (zactvd_zdb_id)
	select t_fgene_zdb_id from tmp_gxp_fake_gene;

insert into marker (mrkr_zdb_id, mrkr_name, mrkr_abbrev, mrkr_type, 
		    mrkr_owner, mrkr_comments) 
	select t_fgene_zdb_id, t_fgene_name, t_fgene_abbrev, t_fgene_type,
		t_fgene_owner, t_fgene_comments 
 	  from tmp_gxp_fake_gene;


------------------------------------------------------------------------
--  MARKER_RELATIONSHIP   ZDB_ACTIVE_DATA   RECORD_ATTRIBUTION      -- 
------------------------------------------------------------------------
let errorHint = "marker relationship";
-- New EST get an "encodes" entry in marker_relationship either with known
-- gene in ZFIN or fake gene just created, and have the relationship
-- attributed to the submitter's expression data publication.

create table tmp_gxp_marker_relationship (
	t_mrel_zdb_id		varchar(50),
	t_mrel_type		varchar(50),
	t_mrel_mrkr1_zdb_id 	varchar(50) not null,
	t_mrel_mrkr2_zdb_id	varchar(50) not null,
	t_mrel_comments		varchar(255)
);

-- known gene and clone assignments
insert into tmp_gxp_marker_relationship 
	 select get_id('MREL'), 'gene encodes small segment',
	        prb_gene_zdb_id, t_mrkr_zdb_id, sbm_lab_name||' load '||TODAY
	   from tmp_gxp_marker, probes_tmp
	  where t_mrkr_name = prb_clone_name
	    and prb_gene_zdb_id is not null ;

-- fake gene and clone relationship
insert into tmp_gxp_marker_relationship 
	 select get_id('MREL'), 'gene encodes small segment',
	        t_fgene_zdb_id, est.t_mrkr_zdb_id, sbm_lab_name||' load '||TODAY
	   from tmp_gxp_marker est, tmp_gxp_fake_gene
	  where t_fgene_probe_zdb_id = est.t_mrkr_zdb_id ;

insert into zdb_active_data (zactvd_zdb_id) 
		select t_mrel_zdb_id from tmp_gxp_marker_relationship;

-- add mrel records
insert into marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, 
				 mrel_mrkr_2_zdb_id, mrel_comments)
		select * from tmp_gxp_marker_relationship;

-- attribute mrel records
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
	select t_mrel_zdb_id, sbm_pub_id from tmp_gxp_marker_relationship;



--------------------------------------------------------------
--    DB_LINK     ZDB_ACTIVE_DATA                       --
--                                                      --
-- we do not attribute GenBank acc db_link record to    --
-- the lab, or pub since neither is the originator.     --
--------------------------------------------------------------
if (sbm_release_type == "eu_nm" ) then
   -- for RefSeq NM_# clones, we don't attach NM_# to clones.  
else

let errorHint = "db_link";

-- Associate the not-null GenBank accession number with the 
-- new EST, or the exist EST/cDNA which doesn't have that 
-- GenBank accession yet.
create table tmp_gxp_db_link (
	t_dblink_zdb_id 	varchar(50),
	t_dblink_linked_recid	varchar(50) not null,
	t_dblink_acc_num	varchar(50),
	t_dblink_fdbcont_zdb_id	varchar(50),
	t_dblink_info		varchar(80)
) ; 
	
insert into tmp_gxp_db_link 
	 select get_id('DBLINK'), 
		t_mrkr_zdb_id, 
		prb_gb5p, 
		fdbcontZdbId,
	        sbm_lab_name || " " || sbm_release_type || " load"
	  from tmp_gxp_marker, probes_tmp
  	 where t_mrkr_name = prb_clone_name
           and prb_gb5p is not null;
 
insert into tmp_gxp_db_link
	 select get_id('DBLINK'), 
		t_mrkr_zdb_id, 
		prb_gb3p, 
		fdbcontZdbId,
	        sbm_lab_name || " " || sbm_release_type || " load"
	  from tmp_gxp_marker, probes_tmp
  	 where t_mrkr_name = prb_clone_name
           and prb_gb3p is not null;


insert into tmp_gxp_db_link
	 select get_id('DBLINK'), 
		tu_mrkr_zdb_id, 
		prb_gb5p, 
		fdbcontZdbId,
	        sbm_lab_name || " " || sbm_release_type || " load" 
	  from tmp_gxp_updated_marker, probes_tmp
  	 where tu_mrkr_name = prb_clone_name
           and prb_gb5p is not null
           and not exists (
			  select dblink_zdb_id 
			    from db_link
			   where dblink_linked_recid = tu_mrkr_zdb_id
			     and dblink_acc_num      = prb_gb5p
			 );
	
insert into tmp_gxp_db_link 
	 select get_id('DBLINK'), 
		tu_mrkr_zdb_id, 
		prb_gb3p, 
		fdbcontZdbId,
	        sbm_lab_name || " " || sbm_release_type || " load"
	  from tmp_gxp_updated_marker, probes_tmp
  	 where tu_mrkr_name = prb_clone_name
           and prb_gb3p is not null
           and not exists (
			select dblink_zdb_id 
			  from db_link
			 where dblink_linked_recid = tu_mrkr_zdb_id
			   and dblink_acc_num      = prb_gb3p
			 );

-- If a sent gb# is currently associated with the gene in zfin, drop that
-- since it should be associated with an EST/cDNA record
delete from db_link where exists (
		select prb_clone_name 
		  from probes_tmp
		 where ( prb_gb5p = db_link.dblink_acc_num 
		      or prb_gb3p = db_link.dblink_acc_num )
	           and db_link.dblink_linked_recid = prb_gene_zdb_id
                  );

insert into zdb_active_data (zactvd_zdb_id)
	select t_dblink_zdb_id from tmp_gxp_db_link;

insert into db_link (dblink_zdb_id,dblink_linked_recid,dblink_acc_num,
		     dblink_fdbcont_zdb_id, dblink_info)
	select * from tmp_gxp_db_link;

end if -- eu_nm type don't attach NM_# to clones

-------------------------------------------------------------------
-- Combine the new and existing ESTs/cDNAs into tmp_gxp_marker 
-- for clone data and expression data processing
-------------------------------------------------------------------
insert into tmp_gxp_marker (t_mrkr_zdb_id, t_mrkr_name)
	select tu_mrkr_zdb_id, tu_mrkr_name from tmp_gxp_updated_marker;


-------------------------------------------------------------------
--        VECTOR                                        --
-------------------------------------------------------------------
let errorHint = "vector";

-- curators give 'go-ahead' for creating new vector record, and 
-- believe that 'Plasmid' is gold.
insert into vector (vector_name, vector_type_name)
	select distinct prb_vector, 'Plasmid' 
	  from probes_tmp 
	 where prb_vector not in (select vector_name from vector);

-------------------------------------------------------------------
--         CLONE                                       --
-------------------------------------------------------------------
let errorHint = "clone";

-- add disclaimer as requested by thisse   
if (sbm_lab_name = "Thisse" AND sbm_release_type = "fr") then 
	update probes_tmp set prb_pcr_amp = "Reaction denatured 4 min. followed by PCR cycling 95Å∞C 30s, 55Å∞C 30s, 72Å∞C 3 min. (at least 1 min. per kb) followed by elongation at 72Å∞C 7 min.<br>" || prb_pcr_amp;

end if

-- Thisse put "genomix DNA" to probe library box, we move it to a disclaimer  
if (sbm_lab_name = "Thisse" AND sbm_release_type LIKE "eu%") then 
	update probes_tmp set prb_pcr_amp = "Probes for in situ hybridization amplified by PCR from genomic DNA.<br>" || prb_pcr_amp;

end if

-- In the case that the clone record is already exist, update five fields
-- with the incoming data and leave the rest as it is 

-- put ESTs/cDNAs who already have clone data into tmp_gxp_clone_data_exist_marker 
select mrkr_zdb_id as tcl_mrkr_zdb_id, 
       mrkr_name   as tcl_mrkr_name
  from probes_tmp, marker
 where prb_clone_name = mrkr_name
   and mrkr_zdb_id in (select clone_mrkr_zdb_id from clone)
into temp tmp_gxp_clone_data_exist_marker with no log; 


update clone set (clone_polymerase_name, clone_digest,
		  clone_insert_size,  clone_cloning_site,
		  clone_pcr_amplification, clone_rating) 

        =((select prb_polymerase, prb_digest, 
		  prb_insert_size, prb_cloning_site,
		  prb_pcr_amp, prb_rating
	     from probes_tmp, tmp_gxp_clone_data_exist_marker
	    where prb_clone_name = tcl_mrkr_name
	      and tcl_mrkr_zdb_id = clone.clone_mrkr_zdb_id
	 ))
   where clone_mrkr_zdb_id in (select tcl_mrkr_zdb_id
  			         from tmp_gxp_clone_data_exist_marker); 

-- new clone record
insert into clone 
  (
    clone_mrkr_zdb_id,  clone_is_chimeric,
    clone_vector_name,  clone_polymerase_name,
    clone_insert_size,  clone_cloning_site,
    clone_digest,       clone_probelib_zdb_id,
    clone_sequence_type,clone_pcr_amplification,
    clone_rating
  )
  select t_mrkr_zdb_id,  'f',
	prb_vector,  prb_polymerase,
	prb_insert_size,  prb_cloning_site,
	prb_digest,  prb_library, 'cDNA', 
	prb_pcr_amp, prb_rating
    from probes_tmp, tmp_gxp_marker
   where prb_clone_name = t_mrkr_name
     and prb_clone_name not in (select tcl_mrkr_name from tmp_gxp_clone_data_exist_marker);

---------------------------------------------------------------------	
-- EXPRESSION_EXPERIMENT   ZDB_ACTIVE_DATA   INT_DATA_SOURCE     --
--                                                               --  
-- record attribution is taken care by database trigger          --
---------------------------------------------------------------------
let errorHint = "expression_experiment";

create table tmp_gxp_expression_experiment (
    t_xpatex_zdb_id 		varchar(50) not null primary key,
    t_xpatex_featexp_zdb_id 	varchar(50) not null,
    t_xpatex_assay_name 	varchar(40),
    t_xpatex_probe_zdb_id 	varchar(50),
    t_xpatex_gene_zdb_id 	varchar(50) not null,
    t_xpatex_source_zdb_id	varchar(50) not null
) ;

insert into tmp_gxp_expression_experiment 
    	 select get_id('XPAT'), sbm_featexp_zdb_id, 
		sbm_assay_name, t_mrkr_zdb_id, 
		mrkr_zdb_id, sbm_pub_id
    	   from tmp_gxp_marker, marker, marker_relationship
    	  where t_mrkr_zdb_id = mrel_mrkr_2_zdb_id
      	    and mrkr_zdb_id   = mrel_mrkr_1_zdb_id
     	    and mrel_type = "gene encodes small segment";


insert into zdb_active_data select t_xpatex_zdb_id from tmp_gxp_expression_experiment;

insert into expression_experiment (xpatex_zdb_id, xpatex_featexp_zdb_id, 
				   xpatex_assay_name, xpatex_probe_feature_zdb_id,
                                   xpatex_gene_zdb_id, xpatex_source_zdb_id)
	select * from tmp_gxp_expression_experiment;

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
	select t_xpatex_zdb_id, sbm_lab_id 
	  from tmp_gxp_expression_experiment;


------------------------------------------------------------------
--   EXPRESSION_RESULT      ZDB_ACTIVE_DATA                  --
------------------------------------------------------------------
let errorHint = "expression_result";

create table tmp_gxp_expression_result(
    t_xpatres_zdb_id		varchar(50)not null,
    t_xpatres_xpatex_zdb_id 	varchar(50)not null,
    t_xpatres_start_stg_zdb_id 	varchar(50)not null,
    t_xpatres_end_stg_zdb_id 	varchar(50)not null,
    t_xpatres_anat_item_zdb_id  varchar(50),
    t_xpatres_expression_found	boolean not null
    --t_xpatres_comments		lvarchar
  ); 

insert into tmp_gxp_expression_result 
	select get_id('XPATRES'), t_xpatex_zdb_id, 
	       exp_sstart, exp_sstop,
	       exp_keyword, exp_found
  	  from tmp_gxp_marker, tmp_gxp_expression_experiment, expression_tmp
 	 where t_mrkr_name = exp_clone_name
   	   and t_mrkr_zdb_id = t_xpatex_probe_zdb_id;


insert into zdb_active_data (zactvd_zdb_id)
	 	select t_xpatres_zdb_id 
	          from tmp_gxp_expression_result;

insert into expression_result (
		xpatres_zdb_id, xpatres_xpatex_zdb_id, 
		xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id,
		xpatres_anat_item_zdb_id, xpatres_expression_found) 
     	select * from tmp_gxp_expression_result;


----------------------------------------------------------------
-- FIGURE
--
-- The images of the same probe in the same stage window are 
-- grouped into one figure record. The xpatres_comments are 
-- carried over as figure caption. The figures for the same
-- probe are labeled by numbers in the order of the stage 
-- window start hour.
-- record_attribution is populated by database tigger
----------------------------------------------------------------
let errorHint = "figure";
-- the prb_zdb_id, start_stg and end_stg will be used to link
-- figure record to expression_result record.
create table tmp_gxp_figure(
	t_fig_zdb_id		varchar(50),
	t_fig_caption		lvarchar,
	t_fig_prb_zdb_id	varchar(50),
	t_fig_start_stg_zdb_id	varchar(50),
	t_fig_end_stg_zdb_id	varchar(50), 
	t_fig_start_hours	decimal(7,2)	
  );

insert into tmp_gxp_figure (t_fig_caption, t_fig_prb_zdb_id, 
			t_fig_start_stg_zdb_id, t_fig_end_stg_zdb_id,
			t_fig_start_hours)
	select exp_description, t_xpatex_probe_zdb_id, 
	       exp_sstart, exp_sstop, stg_hours_start
	  from expression_tmp, tmp_gxp_marker, 
	       tmp_gxp_expression_experiment, stage
         where exp_clone_name = t_mrkr_name
	   and t_mrkr_zdb_id = t_xpatex_probe_zdb_id 
	   and exp_sstart = stg_zdb_id
      group by t_xpatex_probe_zdb_id, exp_sstart, exp_sstop,
	       exp_description, stg_hours_start;

update tmp_gxp_figure set t_fig_zdb_id = get_id ("FIG");

insert into zdb_active_data (zactvd_zdb_id) 
	select t_fig_zdb_id
	  from tmp_gxp_figure;

let lastProbe = '';
let labelCounter = 1;

-- label the figures from the same probe.
foreach
	select t_fig_prb_zdb_id, t_fig_zdb_id, t_fig_caption, t_fig_start_hours
	  into curProbe, curFigZdbId, curCaption, curStgHour
	  from tmp_gxp_figure
      order by t_fig_prb_zdb_id, t_fig_start_hours

	if (curProbe = lastProbe) then
	    let labelCounter = labelCounter + 1;
	else
	    let labelCounter = 1;
	end if

	insert into figure (fig_zdb_id, fig_source_zdb_id,
		    	    fig_caption, fig_label)
		values (curFigZdbId, sbm_pub_id, curCaption, "Fig. " || labelCounter);

	let lastProbe = curProbe;

end foreach

----------------------------------------------------
-- EXPRESSION_PATTERN_FIGURE   --
----------------------------------------------------
let errorHint = "expression_pattern_figure";

insert into expression_pattern_figure (xpatfig_fig_zdb_id, 
					  xpatfig_xpatres_zdb_id)
     select t_fig_zdb_id, t_xpatres_zdb_id
       from tmp_gxp_figure, tmp_gxp_expression_result, tmp_gxp_expression_experiment
      where t_fig_prb_zdb_id = t_xpatex_probe_zdb_id
        and t_xpatex_zdb_id = t_xpatres_xpatex_zdb_id
        and t_xpatres_start_stg_zdb_id = t_fig_start_stg_zdb_id
        and t_xpatres_end_stg_zdb_id = t_fig_end_stg_zdb_id;

-----------------------------------------------------
-- FISH_IMAGE 
-- 
-- no additional record attribution as figure is attributed
------------------------------------------------------
let errorHint = "fish_image";

insert into zdb_active_data (zactvd_zdb_id)
	select img_zdb_id 
	  from images_tmp;
if (sbm_lab_name = "Talbot") then
    update images_tmp set img_comments = "CEG load";
end if 

-- images with annotation need a slightly different SQL
-- if modify, check both SQLs.
if (sbm_lab_name = "Thisse" AND sbm_release_type = "cb") then 
  insert into fish_image (
   	fimg_zdb_id, fimg_fig_zdb_id,
	fimg_image, fimg_thumbnail,
	fimg_image_with_annotation, fimg_annotation,
    	fimg_width, fimg_height,
    	fimg_fish_zdb_id, fimg_comments,
    	fimg_view, fimg_direction,
    	fimg_form, fimg_preparation,
    	fimg_owner_zdb_id, fimg_external_name)
    select img_zdb_id, t_fig_zdb_id, 
	   img_zdb_id || '.jpg', img_zdb_id || '_thumb.jpg',
	   img_zdb_id || '_annot.jpg', '' -- will come back to annot text later
	   imgdim_width, imgdim_height,
	   sbm_fish_line_id, img_comments,
	   img_view, img_orient, 
	   fishImageForm, img_preparation,
	   sbm_person_id, img_image_name
      from images_tmp, tmp_gxp_figure, tmp_gxp_marker, image_dim
     where img_clone_name = t_mrkr_name
       and t_mrkr_zdb_id = t_fig_prb_zdb_id
       and img_sstart = t_fig_start_stg_zdb_id
       and img_sstop = t_fig_end_stg_zdb_id
       and imgdim_name   = img_image_name;
else 
  insert into fish_image (
   	fimg_zdb_id, fimg_fig_zdb_id,
	fimg_image, fimg_thumbnail,
    	fimg_width, fimg_height,
    	fimg_fish_zdb_id, fimg_comments,
    	fimg_view, fimg_direction,
    	fimg_form, fimg_preparation,
    	fimg_owner_zdb_id, fimg_external_name)
    select img_zdb_id, t_fig_zdb_id, 
	   img_zdb_id || '.jpg', img_zdb_id || '_thumb.jpg',
	   imgdim_width, imgdim_height,
	   sbm_fish_line_id, img_comments,
	   img_view, img_orient, 
	   fishImageForm, img_preparation,
	   sbm_person_id, img_image_name
      from images_tmp, tmp_gxp_figure, tmp_gxp_marker, image_dim
     where img_clone_name = t_mrkr_name
       and t_mrkr_zdb_id = t_fig_prb_zdb_id
       and img_sstart = t_fig_start_stg_zdb_id
       and img_sstop = t_fig_end_stg_zdb_id
       and imgdim_name   = img_image_name;
end if



----------------------------------------------------
-- INT_DATA_SOURCE   --
----------------------------------------------------
let errorHint = "int_data_source";

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
	select distinct t_xpatex_zdb_id, person.zdb_id
	  from person, authors_tmp, 
	       tmp_gxp_expression_experiment, tmp_gxp_marker
         where full_name = aut_author_name
           and t_xpatex_probe_zdb_id = t_mrkr_zdb_id
	   and t_mrkr_name = aut_clone_name;



commit work;

  end
  return 0;

end function;

update statistics for function gxp_load_func;
