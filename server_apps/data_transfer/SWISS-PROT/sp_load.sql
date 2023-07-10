-- SWISS-PROT file is parsed, after checked for consistence with ZFIN, into
-- informix loadable .unl files. From original SWISS-PROT file we got, 
-- the SWISS-PROT accession number(AC) and cross database references(DR) are
-- loaded into db_link table, Gene alias(GN) are added to data_alias table,
-- Keyword(KW) and InterPro accession numbers are translated into GO terms
-- through two available translation table and GO ontology file. Comments(CC)
-- are stored in external_note table. All the information loaded to ZFIN from
-- SWISS-PROT is attributed to an internal pub record which explains the whole
-- process of SWISS-PROT loading in record_attribution table.
  	
-- SWISS-PROT file will be reloaded every 6 months. Each time, the records 
-- attributed to SWISS-PROT loading will be cleaned for a reload. Records have 
-- other sources will be kept, and the reload process will detect them. If
-- they are in reload list, one more SWISS-PROT source will be recorded.

begin work;

--set pdqpriority 50;

--the unloaded record if already in, only add zdb_id into record_attribution with SWISS_PROT source
--	create temporary table exist_record (
--		extrecd_zdb_id	text,
--		new_zdb_id	text
--		) with no log;

------------------ loading db_link --------------------------

--!echo 'Create temporary table db_link_with_dups'
    drop table if exists tmp_uniprot_db_link_with_dups;
	create table tmp_uniprot_db_link_with_dups (
               linked_recid text,
               db_name text,
               acc_num text,
               length text
              );

--!echo 'Load from dr_dblink.unl'
        copy tmp_uniprot_db_link_with_dups from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/dr_dblink.unl' (delimiter '|');
--!echo '		from dr_dblink.unl'


update tmp_uniprot_db_link_with_dups
 set length = null
 where length = '';

alter table tmp_uniprot_db_link_with_dups
  alter column length type integer USING length::integer;

--!echo 'Update merged gene ids'
	update tmp_uniprot_db_link_with_dups
	   set linked_recid = (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = linked_recid)
         where linked_recid in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 

--!echo 'Create temporary table pre_db_link'
	drop table if exists tmp_uniprot_pre_db_link;
	create table tmp_uniprot_pre_db_link (
               linked_recid text,
               db_name text,
               acc_num text,
               info text,
	       dblink_zdb_id text,
               acc_num_disp text,
               dblink_fdbcont_zdb_id text,
               length integer
              );
              
--!echo 'Create tmp_uniprot_pre_db_link_acc_index on tmp_uniprot_pre_db_link (acc_num)'
	create index tmp_uniprot_pre_db_link_acc_index on tmp_uniprot_pre_db_link (acc_num,linked_recid,dblink_fdbcont_zdb_id);

	insert into tmp_uniprot_pre_db_link (linked_recid,db_name,acc_num,length,info,acc_num_disp)
	       		select distinct *, current_date ||' Swiss-Prot',acc_num 
			      from tmp_uniprot_db_link_with_dups;
--!echo '		into tmp_uniprot_pre_db_link'

-- per curator's request, GenBank and GenPept accession are no longer loaded from SP file
-- due to additional entries for certain database, we need to specify the db types
	update tmp_uniprot_pre_db_link set dblink_fdbcont_zdb_id =
				(select fdbcont_zdb_id 
				   from foreign_db_contains, foreign_db, foreign_db_data_type 
				  where lower(db_name)=lower(fdb_db_name)
                                    and fdbcont_fdb_db_id = fdb_db_pk_id
				    and fdbcont_fdbdt_id = fdbdt_pk_id
                                    and fdbcont_organism_common_name = 'Zebrafish'
                    and (  (fdbdt_super_type = 'protein' 
                            and fdbdt_data_type = 'domain')
                        or (fdbdt_super_type = 'sequence' 
                            and fdbdt_data_type = 'Polypeptide')
                        )
				); 

	delete from tmp_uniprot_pre_db_link where exists (
		select d.dblink_zdb_id
		  from db_link d
		 where tmp_uniprot_pre_db_link.linked_recid=d.dblink_linked_recid
           and tmp_uniprot_pre_db_link.acc_num=d.dblink_acc_num
		   and tmp_uniprot_pre_db_link.dblink_fdbcont_zdb_id = d.dblink_fdbcont_zdb_id
		);
--!echo '		from tmp_uniprot_pre_db_link'

     --   update statistics high for table tmp_uniprot_pre_db_link;

	update tmp_uniprot_pre_db_link set dblink_zdb_id = get_id('DBLINK');


--!echo 'Insert into zdb_active_data'
	insert into zdb_active_data (zactvd_zdb_id)
                  select dblink_zdb_id from tmp_uniprot_pre_db_link p;
--!echo '		into zdb_active_data'

			 
--!echo 'Insert DBLINK into db_link'
	insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,
			     dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_length)  
		select linked_recid,acc_num,info,dblink_zdb_id,
			   acc_num_disp,dblink_fdbcont_zdb_id,length 
		  from tmp_uniprot_pre_db_link p;
--!echo '		into db_link'

--!echo 'Attribute db links to the internal pub record'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select dblink_zdb_id, 'ZDB-PUB-230615-71'
		  from tmp_uniprot_pre_db_link;
--!echo '		into record_attribution'


------------------- loading ac alias ------------------------
-- from June, 2010, no longer load dblink ids into data_alias table (see FB case 5770) -----------------

--!echo 'per curator's request, no longer load gn alias -----------------

------------------------- loading marker go evidence  --------------------


-- load in information from three GO term translation tables
-- 1. Swiss-Prot keyword to GO	
-- 2. InterPro to GO
-- 3. ec to GO

	drop table if exists tmp_uniprot_spkw_goterm_with_dups;
	create table tmp_uniprot_spkw_goterm_with_dups (
		sp_kwd_id	text,
		sp_kwd_name  	text,
		goterm_name  	text,
		goterm_id  	text
	);

--!echo 'Load sp_mrkrgoterm.unl: spkwtogo translation table'
	copy tmp_uniprot_spkw_goterm_with_dups from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_mrkrgoterm.unl' (delimiter '|');
	
	create index tmp_uniprot_spkw_goterm_with_dups_keyword_index
		on tmp_uniprot_spkw_goterm_with_dups (sp_kwd_name);

--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
-- order the boolean column secondary, obsolete makes the obsolete(t) comes first,
-- secondary(t) comes second, if obsolete&secondary(?) comes last. 
	create view spkw2go_obsl_secd as
		select distinct 'UniProtKB-KW:'||sp_kwd_id, s.goterm_name, s.goterm_id,
			t.term_is_obsolete, t.term_is_secondary 
		  from tmp_uniprot_spkw_goterm_with_dups s, term t
		 where 'GO:'||s.goterm_id = t.term_ont_id
	 	   and (t.term_is_obsolete = 't'
		       or t.term_is_secondary = 't')
		  order by t.term_is_secondary, t.term_is_obsolete;	
	\copy (select * from spkw2go_obsl_secd) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/spkw2go_obsl_secd.unl' with delimiter as '|' null as '';	
	drop view spkw2go_obsl_secd;	  
		  
	delete from tmp_uniprot_spkw_goterm_with_dups
		where 'GO:'||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = 't'
		                        or  term_is_secondary = 't'
				     );		

	drop table if exists tmp_uniprot_ip_goterm_with_dups ;
	create table tmp_uniprot_ip_goterm_with_dups (
		ip_acc  text,
		goterm_name  text,
		goterm_id  text
		);

--!echo 'Load ip_mrkrgoterm.unl: iptogo translation table'
	copy tmp_uniprot_ip_goterm_with_dups from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ip_mrkrgoterm.unl' (delimiter '|');
	
--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
	create view ip2go_obsl_secd as
		select distinct 'InterPro:'||ip_acc, i.goterm_name, i.goterm_id, 
			t.term_is_obsolete, t.term_is_secondary 
		  from tmp_uniprot_ip_goterm_with_dups i,  term t
	         where 'GO:'||i.goterm_id = t.term_ont_id
	           and (t.term_is_obsolete = 't'
	             or t.term_is_secondary = 't')
		order by t.term_is_secondary, t.term_is_obsolete;
	\copy (select * from ip2go_obsl_secd) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ip2go_obsl_secd.unl' with delimiter as '|' null as '';	
	drop view ip2go_obsl_secd;		
		
	delete from tmp_uniprot_ip_goterm_with_dups
		where 'GO:'||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = 't'
		                        or  term_is_secondary = 't'
				     );		
--!echo 'Load ec_mrkrgoterm.unl: ectogo translation table'

	drop table if exists tmp_uniprot_ec_goterm_with_dups ;
	create table tmp_uniprot_ec_goterm_with_dups (
		ec_acc  text,
		goterm_name  text,
		goterm_id  text
		);

	copy tmp_uniprot_ec_goterm_with_dups from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ec_mrkrgoterm.unl' (delimiter '|');
	
--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
	create view ec2go_obsl_secd as
		select distinct 'EC:'||ec_acc, e.goterm_name, e.goterm_id,
			t.term_is_obsolete, t.term_is_secondary 
		  from tmp_uniprot_ec_goterm_with_dups e, term t
	         where 'GO:'||e.goterm_id = t.term_ont_id
	           and (t.term_is_obsolete = 't'
		       or t.term_is_secondary = 't')
		order by t.term_is_secondary, t.term_is_obsolete;
	\copy (select * from ec2go_obsl_secd) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ec2go_obsl_secd.unl' with delimiter as '|' null as '';	
	drop view ec2go_obsl_secd;	
		
	delete from tmp_uniprot_ec_goterm_with_dups
		where 'GO:'||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = 't'
		                        or  term_is_secondary = 't'
				     );		


--!echo ' load in information of keywords with specific S-P record'
	drop table if exists tmp_uniprot_sp_kwd ;
	create table tmp_uniprot_sp_kwd (
		mrkr_zdb_id text,
		sp_kwd      text
		);

--!echo 'Load kd_spkeywd.unl'
	copy tmp_uniprot_sp_kwd from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/kd_spkeywd.unl' (delimiter '|');

--!echo 'Update merged gene ids'
	update tmp_uniprot_sp_kwd
	   set mrkr_zdb_id =  (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = mrkr_zdb_id)
         where mrkr_zdb_id in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 

	drop table if exists pre_marker_go_term_evidence ;
	create table pre_marker_go_term_evidence (
                pre_mrkrgoev_zdb_id 	text, 
		mrkr_zdb_id		text,
		go_zdb_id		text,
		mrkrgoev_source		text,
                mrkrgoev_inference 	text,
                mrkrgoev_note           text
	);


create index pmge_mrkr_id_index
  on pre_marker_go_term_evidence (mrkr_zdb_id);


--!echo 'Load spkw'
	insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct sk.mrkr_zdb_id, term_zdb_id, 'ZDB-PUB-020723-1' as pubid, 
		       'UniProtKB-KW:'||sg.sp_kwd_id as text1, 'ZFIN SP keyword 2 GO' as text2
		  from tmp_uniprot_sp_kwd sk,  tmp_uniprot_spkw_goterm_with_dups sg, term
		 where sk.sp_kwd = sg.sp_kwd_name
		   and term_ont_id = 'GO:'||sg.goterm_id;

--!echo 'Load intepro'
	insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct db.linked_recid, term_zdb_id, 'ZDB-PUB-020724-1' as pubid,
		       'InterPro:'||ip.ip_acc as text1, 'ZFIN InterPro 2 GO' as text2
		  from tmp_uniprot_pre_db_link db, tmp_uniprot_ip_goterm_with_dups ip, term
	 	 where db.acc_num = ip.ip_acc
		   and term_ont_id = 'GO:'||ip.goterm_id;
	
--!echo 'Load ec'
    insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source,
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct db.linked_recid, term_zdb_id, 'ZDB-PUB-031118-3' as pubid, 
		       'EC:'||ec.ec_acc as text1, 'ZFIN EC acc 2 GO' as text2
		from tmp_uniprot_pre_db_link db, tmp_uniprot_ec_goterm_with_dups ec, term
		where db.acc_num = ec.ec_acc
		  and term_ont_id = 'GO:'||ec.goterm_id;
               

--!echo 'do not include 'unknown' terms and root terms if any'
        delete from pre_marker_go_term_evidence where go_zdb_id in 
		(select term_zdb_id 
		   from term 
		  where term_ont_id in ('GO:0005554', 'GO:0000004', 'GO:0008372',
					 'GO:0005575', 'GO:0003674', 'GO:0008150'));

-- if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one
-- db trigger is added for this purpose. 


delete from pre_marker_go_term_evidence p
  where exists (select 'x' from marker a
		       	   	  	      where a.mrkr_zdb_id = p.mrkr_zdb_id
					      and a.mrkr_abbrev like 'WITHDRAWN%');

-- if a go term is in one of the two 'do not annoate' subsets remove from load and report. see case 12290
--!echo 'unload restricted go subset annotations';

create view droppedAnnotationsViaSubsetViolations as
select 'The following annotations were not added to ZFIN because the GO term being used in the translation file (interpro2go, ec2go, or UniprotKB_kw2go) is marked as being part of the gocheck_do_not_annotate subset.  See case 12296 for details.','' as b1,'' as b2
 from single
union
select mrkr_zdb_id, term_ont_id, mrkrgoev_source
  from pre_marker_go_Term_evidence, term
where term.term_zdb_id = pre_marker_go_term_evidence.go_zdb_id
 and exists (Select 'x' from term_subset, ontology_subset
       	      	      where termsub_term_zdb_id = term.term_zdb_id
		      and termsub_subset_id = osubset_pk_id
		      and osubset_subset_name = 'gocheck_do_not_annotate');
\copy (select * from droppedAnnotationsViaSubsetViolations) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/droppedAnnotationsViaSubsetViolations.txt' with delimiter as '|' null as '';	
drop view droppedAnnotationsViaSubsetViolations;

delete from pre_marker_go_Term_evidence
 where exists (Select 'x' from term_subset, ontology_subset
       	      	      where termsub_term_zdb_id = go_zdb_id
		      and termsub_subset_id = osubset_pk_id
		      and osubset_subset_name = 'gocheck_do_not_annotate');
		      
--update statistics high for table pre_marker_go_term_evidence;

-- Set IDs of marker_go_term_evidence to be the same as the last time they were imported
-- if they previously existed
UPDATE pre_marker_go_term_evidence p
SET    pre_mrkrgoev_zdb_id = u.mrkrgoev_zdb_id
FROM   tmp_uniprot_last_run_marker_go_term_evidence u
WHERE
    p.mrkr_zdb_id = u.mrkrgoev_mrkr_zdb_id
  AND p.go_zdb_id = u.mrkrgoev_term_zdb_id
  AND p.mrkrgoev_source = u.mrkrgoev_source_zdb_id
  AND p.mrkrgoev_note = u.mrkrgoev_notes
  AND p.mrkrgoev_inference = u.infgrmem_inferred_from;
-- END of Set IDs

-- HERE IS WHERE WE FLATTEN ANY IDS IN pre_marker_go_term_evidence:
-- For example, in previous versions of this script, pre_marker_go_term_evidence had rows of the form:
--
-- ZDB-MRKRGOEV-220118-60715,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR001523,ZFIN InterPro 2 GO
-- ZDB-MRKRGOEV-220118-65280,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR043565,ZFIN InterPro 2 GO
-- ZDB-MRKRGOEV-220118-77844,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR043182,ZFIN InterPro 2 GO
--
-- But since each row is only unique due to the inference_group_member column (InterPro:... in this case), we only need
-- one row in the marker_go_term_evidence table (and therefore only one mrkrgoev ID).  So it makes more sense to have
-- pre_marker_go_term_evidence rows that look like this:
--
-- ZDB-MRKRGOEV-220118-60715,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR001523,ZFIN InterPro 2 GO
-- ZDB-MRKRGOEV-220118-60715,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR043565,ZFIN InterPro 2 GO
-- ZDB-MRKRGOEV-220118-60715,ZDB-GENE-980526-399,ZDB-TERM-091209-2435,ZDB-PUB-020724-1,InterPro:IPR043182,ZFIN InterPro 2 GO
--
-- and when we use 'distinct' to populate marker_go_term_evidence they will become a single row, but when we populate
-- inference_group_member, it will be 3 rows

update pre_marker_go_term_evidence
set pre_mrkrgoev_zdb_id = get_id ('MRKRGOEV')
where pre_mrkrgoev_zdb_id is null;

--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select DISTINCT pre_mrkrgoev_zdb_id from pre_marker_go_term_evidence;
--!echo '		into zdb_active_data'

--!echo 'delete root go terms in bulk' ;

delete from marker_go_term_evidence
       where mrkrgoev_term_zdb_id in ('ZDB-TERM-091209-6070','ZDB-TERM-091209-2432','ZDB-TERM-091209-4029')
       and exists (Select 'x' from pre_marker_go_term_evidence
       	   	  	  where mrkrgoev_mrkr_Zdb_id =mrkr_zdb_id
			  and mrkrgoev_term_zdb_id = go_zdb_id);

delete from record_Attribution
 where recattrib_source_zdb_id = 'ZDB-PUB-031118-1'
 and not exists (Select 'x' from marker_go_term_evidence
     	 		   where mrkrgoev_mrkr_zdb_id = recattrib_datA_zdb_id
			   and mrkrgoev_term_zdb_id in ('ZDB-TERM-091209-4029',
                                         'ZDB-TERM-091209-2432',
                                         'ZDB-TERM-091209-6070'))
and exists (Select 'x' from pre_marker_go_term_evidence
  where mrkr_zdb_id = recattrib_data_zdb_id);
 

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence(mrkrgoev_zdb_id,mrkrgoev_mrkr_zdb_id, mrkrgoev_term_zdb_id,
				mrkrgoev_source_zdb_id, mrkrgoev_evidence_code,mrkrgoev_date_entered,mrkrgoev_date_modified,
				mrkrgoev_annotation_organization,mrkrgoev_external_load_date,mrkrgoev_notes)
		select DISTINCT p.pre_mrkrgoev_zdb_id, p.mrkr_zdb_id, p.go_zdb_id, p.mrkrgoev_source, 'IEA' as iea,
		       now() as time1, now() as time2, 5 as org, now() as time3, p.mrkrgoev_note
		  from pre_marker_go_term_evidence p;
--!echo '		into marker_go_term_evidence'
	
--	db trigger attributes MRKRGOEV to the internal pub record
--	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
--	  	               select mrkrgoev_zdb_id,mrkrgoev_source from pre_marker_go_term_evidence;

--!echo 'db trigger attributes MRKRGOEV to the internal pub record'


-- load inference_group_member
	insert into inference_group_member (infgrmem_inferred_from, infgrmem_mrkrgoev_zdb_id)
			select mrkrgoev_inference, pre_mrkrgoev_zdb_id
			  from pre_marker_go_term_evidence
			 where exists (select * from marker_go_term_evidence where pre_mrkrgoev_zdb_id = mrkrgoev_zdb_id);

--!echo '		into inference_group_member'

---------------- loading cc field -----------------------------

-- loading cc
        drop table if exists tmp_uniprot_mrkr_cc ;
        create table tmp_uniprot_mrkr_cc (
                gene_zdb_id      text,
		sp_acc_num	 text,
                cc_note          text
        );

--!echo 'Load cc_external.unl'
        --load from cc_external.unl delimiter '$' insert into tmp_uniprot_mrkr_cc;
        copy tmp_uniprot_mrkr_cc from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/cc_external.unl' (delimiter '$');

--!echo 'Update merged gene ids'
	update tmp_uniprot_mrkr_cc
	   set gene_zdb_id =  (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = gene_zdb_id)
         where gene_zdb_id in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 


        drop table if exists temporary_nondupl_mrkr_cc ;
        create table temporary_nondupl_mrkr_cc (
                nondupl_gene_zdb_id      text,
		nondupl_sp_acc_num	 text,
                nondupl_cc_note          text
        );

        insert into temporary_nondupl_mrkr_cc (nondupl_gene_zdb_id,nondupl_sp_acc_num,nondupl_cc_note)
         select distinct gene_zdb_id, sp_acc_num, cc_note
	          from tmp_uniprot_mrkr_cc;

        drop table if exists tmp_uniprot_pre_external_note;
        create table tmp_uniprot_pre_external_note(
                p_extnote_zdb_id          text,
                p_extnote_data_zdb_id     text, 
                p_extnote_note            text,
                p_extnote_source_zdb_id   text
        );
        
        insert into tmp_uniprot_pre_external_note (p_extnote_note,p_extnote_data_zdb_id, p_extnote_source_zdb_id)
                select nondupl_cc_note, dblink_zdb_id, 'ZDB-PUB-230615-71'
	          from temporary_nondupl_mrkr_cc, db_link
		 where nondupl_gene_zdb_id = dblink_linked_recid
		   and nondupl_sp_acc_num  = dblink_acc_num
		   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47';

        update tmp_uniprot_pre_external_note
                        set p_extnote_zdb_id = get_id('EXTNOTE');


--!echo 'Insert EXTNOTE into zdb_active_data'
        insert into zdb_active_data (zactvd_zdb_id)
                select p_extnote_zdb_id from tmp_uniprot_pre_external_note;
--!echo '		into zdb_active_data'

--!echo 'Insert into external_note'
        insert into external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note, extnote_source_zdb_id)
                   select * from tmp_uniprot_pre_external_note;
--!echo '		into external_note'

--!echo 'Attribute EXTNOTE to the internal pub record'
        insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
                select p_extnote_zdb_id, p_extnote_source_zdb_id
                from tmp_uniprot_pre_external_note;
--!echo '		into record_attribution'

--!echo 'unload accession# with no attribution'
-- accession from EC, Pfam, POSITE, InterPro, SwissProt
        create view accessionWithNoAttribution as
                select dblink_linked_recid, dblink_acc_num
                  from db_link
                 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-49',
                                                 'ZDB-FDBCONT-040412-50',
                                                 'ZDB-FDBCONT-040412-51',
                                                 'ZDB-FDBCONT-040412-48',
                                                 'ZDB-FDBCONT-040412-47')
                   and not exists (
                        select 'x' from record_attribution
                         where recattrib_data_zdb_id = dblink_zdb_id )
              order by dblink_linked_recid ;

        \copy (select * from accessionWithNoAttribution) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/accession_with_no_attribution' with delimiter as '|' null as '';
        drop view accessionWithNoAttribution;

-- UNCOMMENT these table drops for debugging uniprot load after the fact.
drop table tmp_uniprot_db_link_with_dups;
drop table tmp_uniprot_ec_goterm_with_dups;
drop table tmp_uniprot_ip_goterm_with_dups;
drop table tmp_uniprot_last_run_marker_go_term_evidence;
drop table tmp_uniprot_mrkr_cc;
drop table tmp_uniprot_pre_db_link;
drop table tmp_uniprot_pre_external_note;
drop table tmp_uniprot_sp_kwd;
drop table tmp_uniprot_spkw_goterm_with_dups;

--rollback work;
commit work;
