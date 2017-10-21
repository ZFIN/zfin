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

set pdqpriority 50;

--the unloaded record if already in, only add zdb_id into record_attribution with SWISS_PROT source
--	create temp table exist_record (
--		extrecd_zdb_id	varchar(50),
--		new_zdb_id	varchar(50)
--		) with no log;

------------------ loading db_link --------------------------

--!echo 'Create temp table db_link_with_dups'
	create temp table db_link_with_dups (
               linked_recid varchar(50),
               db_name varchar(50),
               acc_num varchar(50),
               length integer
              ) with no log;

--!echo 'Load from dr_dblink.unl'
	load from dr_dblink.unl insert into db_link_with_dups;
--!echo '		from dr_dblink.unl'


--!echo 'Update merged gene ids'
	update db_link_with_dups
	   set linked_recid = (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = linked_recid)
         where linked_recid in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 

--!echo 'Create temp table pre_db_link'
	create temp table pre_db_link (
               linked_recid varchar(50),
               db_name varchar(50),
               acc_num varchar(50),
               info varchar(80),
	       	   dblink_zdb_id varchar(50),
               acc_num_disp varchar(50),
               dblink_fdbcont_zdb_id varchar(50),
               length integer
              ) with no log;
              
--!echo 'Create pre_db_link_acc_index on pre_db_link (acc_num)'              
	create index pre_db_link_acc_index on pre_db_link (acc_num,linked_recid,dblink_fdbcont_zdb_id);

	insert into pre_db_link (linked_recid,db_name,acc_num,length,info,acc_num_disp)     
	       		select distinct *, today ||" Swiss-Prot",acc_num 
			      from db_link_with_dups;
--!echo '		into pre_db_link'

-- per curator's request, GenBank and GenPept accession are no longer loaded from SP file
-- due to additional entries for certain database, we need to specify the db types
	update pre_db_link set dblink_fdbcont_zdb_id = 
				(select fdbcont_zdb_id 
				   from foreign_db_contains, foreign_db, foreign_db_data_type 
				  where lower(db_name)=lower(fdb_db_name)
                                    and fdbcont_fdb_db_id = fdb_db_pk_id
				    and fdbcont_fdbdt_id = fdbdt_pk_id
                                    and fdbcont_organism_common_name = "Zebrafish"
                    and (  (fdbdt_super_type = "protein" 
                            and fdbdt_data_type = "domain")
                        or (fdbdt_super_type = "sequence" 
                            and fdbdt_data_type = "Polypeptide")
                        )
				); 

	delete from pre_db_link where exists (
		select d.dblink_zdb_id
		  from db_link d
		 where pre_db_link.linked_recid=d.dblink_linked_recid
           and pre_db_link.acc_num=d.dblink_acc_num
		   and pre_db_link.dblink_fdbcont_zdb_id = d.dblink_fdbcont_zdb_id
		);
--!echo '		from pre_db_link'

        update statistics high for table pre_db_link;

	update pre_db_link set dblink_zdb_id = get_id("DBLINK"); 


--!echo 'Insert into zdb_active_data'
	insert into zdb_active_data (zactvd_zdb_id)
                  select dblink_zdb_id from pre_db_link p;
--!echo '		into zdb_active_data'

			 
--!echo 'Insert DBLINK into db_link'
	insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,
			     dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_length)  
		select linked_recid,acc_num,info,dblink_zdb_id,
			   acc_num_disp,dblink_fdbcont_zdb_id,length 
		  from pre_db_link p;
--!echo '		into db_link'

--!echo 'Attribute db links to the internal pub record'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select dblink_zdb_id, "ZDB-PUB-020723-2"
		  from pre_db_link;
--!echo '		into record_attribution'


------------------- loading ac alias ------------------------
-- from June, 2010, no longer load dblink ids into data_alias table (see FB case 5770) -----------------

--!echo 'per curator's request, no longer load gn alias -----------------

------------------------- loading marker go evidence  --------------------


-- load in information from three GO term translation tables
-- 1. Swiss-Prot keyword to GO	
-- 2. InterPro to GO
-- 3. ec to GO

	create temp table spkw_goterm_with_dups (
		sp_kwd_id	varchar(50),
		sp_kwd_name  	varchar(80),
		goterm_name  	varchar(100),
		goterm_id  	varchar(10)
	)with no log;

--!echo 'Load sp_mrkrgoterm.unl: spkwtogo translation table'
	load from sp_mrkrgoterm.unl insert into spkw_goterm_with_dups;
	create index spkw_goterm_with_dups_keyword_index
		on spkw_goterm_with_dups (sp_kwd_name);

--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
-- order the boolean column secondary, obsolete makes the obsolete(t) comes first,
-- secondary(t) comes second, if obsolete&secondary(?) comes last. 
	unload to "spkw2go_obsl_secd.unl" 
		select distinct "UniProtKB-KW:"||sp_kwd_id, s.goterm_name, s.goterm_id,
			t.term_is_obsolete, t.term_is_secondary 
		  from spkw_goterm_with_dups s, term t
		 where "GO:"||s.goterm_id = t.term_ont_id
	 	   and (t.term_is_obsolete = "t"
		       or t.term_is_secondary = "t")
		  order by t.term_is_secondary, t.term_is_obsolete;	
	delete from spkw_goterm_with_dups
		where "GO:"||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = "t"
		                        or  term_is_secondary = "t"
				     );		

	create temp table ip_goterm_with_dups (
		ip_acc  varchar(20),
		goterm_name  varchar(100),
		goterm_id  varchar(10)
		)with no log;

--!echo 'Load ip_mrkrgoterm.unl: iptogo translation table'
	load from ip_mrkrgoterm.unl insert into ip_goterm_with_dups;
--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
	unload to "ip2go_obsl_secd.unl" 
		select distinct "InterPro:"||ip_acc, i.goterm_name, i.goterm_id, 
			t.term_is_obsolete, t.term_is_secondary 
		  from ip_goterm_with_dups i,  term t
	         where "GO:"||i.goterm_id = t.term_ont_id
	           and (t.term_is_obsolete = "t"
	             or t.term_is_secondary = "t")
		order by t.term_is_secondary, t.term_is_obsolete;	
	delete from ip_goterm_with_dups 
		where "GO:"||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = "t"
		                        or  term_is_secondary = "t"
				     );		
--!echo 'Load ec_mrkrgoterm.unl: ectogo translation table'

	create temp table ec_goterm_with_dups (
		ec_acc  varchar(20),
		goterm_name  varchar(100),
		goterm_id  varchar(10)
		)with no log;

	load from ec_mrkrgoterm.unl insert into ec_goterm_with_dups;
--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
	unload to "ec2go_obsl_secd.unl" 
		select distinct "EC:"||ec_acc, e.goterm_name, e.goterm_id,
			t.term_is_obsolete, t.term_is_secondary 
		  from ec_goterm_with_dups e, term t
	         where "GO:"||e.goterm_id = t.term_ont_id
	           and (t.term_is_obsolete = "t"
		       or t.term_is_secondary = "t")
		order by t.term_is_secondary, t.term_is_obsolete;	
	delete from ec_goterm_with_dups 
		where "GO:"||goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = "t"
		                        or  term_is_secondary = "t"
				     );		


--!echo ' load in information of keywords with specific S-P record'
	create temp table sp_kwd (
		mrkr_zdb_id varchar(50),
		sp_kwd      varchar(80)
		)with no log;

--!echo 'Load kd_spkeywd.unl'
	load from kd_spkeywd.unl insert into sp_kwd;

--!echo 'Update merged gene ids'
	update sp_kwd
	   set mrkr_zdb_id =  (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = mrkr_zdb_id)
         where mrkr_zdb_id in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 

	create temp table pre_marker_go_term_evidence (
                pre_mrkrgoev_zdb_id 	varchar(50), 
		mrkr_zdb_id		varchar(50),
		go_zdb_id		varchar(50),
		mrkrgoev_source		varchar(50),
                mrkrgoev_inference 	varchar(80),
                mrkrgoev_note           lvarchar
	)with no log;


create index pmge_mrkr_id_index
  on pre_marker_go_term_evidence (mrkr_zdb_id)
  using btree in idxdbs2;


--!echo 'Load spkw'
	insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct sk.mrkr_zdb_id, term_zdb_id, "ZDB-PUB-020723-1", 
		       "UniProtKB-KW:"||sg.sp_kwd_id, "ZFIN SP keyword 2 GO"
		  from sp_kwd sk,  spkw_goterm_with_dups sg, term
		 where sk.sp_kwd = sg.sp_kwd_name
		   and term_ont_id = "GO:"||sg.goterm_id;

--!echo 'Load intepro'
	insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct db.linked_recid, term_zdb_id, "ZDB-PUB-020724-1",
		       "InterPro:"||ip.ip_acc, "ZFIN InterPro 2 GO"
		  from pre_db_link db, ip_goterm_with_dups ip, term
	 	 where db.acc_num = ip.ip_acc
		   and term_ont_id = "GO:"||ip.goterm_id;
	
--!echo 'Load ec'
        insert into pre_marker_go_term_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source,  
					    mrkrgoev_inference, mrkrgoev_note)
		select distinct db.linked_recid, term_zdb_id, "ZDB-PUB-031118-3", 
		       "EC:"||ec.ec_acc, "ZFIN EC acc 2 GO"
		from pre_db_link db, ec_goterm_with_dups ec, term
		where db.acc_num = ec.ec_acc
		  and term_ont_id = "GO:"||ec.goterm_id;


               

	update pre_marker_go_term_evidence set pre_mrkrgoev_zdb_id = get_id ("MRKRGOEV");

--!echo 'do not include "unknown" terms and root terms if any'
        delete from pre_marker_go_term_evidence where go_zdb_id in 
		(select term_zdb_id 
		   from term 
		  where term_ont_id in ("GO:0005554", "GO:0000004", "GO:0008372",
					 "GO:0005575", "GO:0003674", "GO:0008150"));

-- if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one
-- db trigger is added for this purpose. 


delete from pre_marker_go_term_evidence p
  where exists (select 'x' from marker a
		       	   	  	      where a.mrkr_zdb_id = p.mrkr_zdb_id
					      and a.mrkr_abbrev like 'WITHDRAWN%');

-- if a go term is in one of the two "do not annoate" subsets remove from load and report. see case 12290
!echo "unload restricted go subset annotations";

unload to droppedAnnotationsViaSubsetViolations.txt
select "The following annotations were not added to ZFIN because the GO term being used in the translation file (interpro2go, ec2go, or UniprotKB_kw2go) is marked as being part of the 'gocheck_do_not_annotate' subset.  See case 12296 for details.","",""
 from single
union
select mrkr_zdb_id, term_ont_id, mrkrgoev_source
  from pre_marker_go_Term_evidence, term
where term.term_zdb_id = pre_marker_go_term_evidence.go_zdb_id
 and exists (Select 'x' from term_subset, ontology_subset
       	      	      where termsub_term_zdb_id = term.term_zdb_id
		      and termsub_subset_id = osubset_pk_id
		      and osubset_subset_name in ("gocheck_do_not_annotate"));

delete from pre_marker_go_Term_evidence
 where exists (Select 'x' from term_subset, ontology_subset
       	      	      where termsub_term_zdb_id = go_zdb_id
		      and termsub_subset_id = osubset_pk_id
		      and osubset_subset_name in ("gocheck_do_not_annotate"));



update statistics high for table pre_marker_go_term_evidence;

--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select pre_mrkrgoev_zdb_id from pre_marker_go_term_evidence;
--!echo '		into zdb_active_data'

!echo "delete root go terms in bulk" ;

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
		select p.pre_mrkrgoev_zdb_id, p.mrkr_zdb_id, p.go_zdb_id, p.mrkrgoev_source, "IEA", 
		       CURRENT, CURRENT, '5', CURRENT, p.mrkrgoev_note
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
        create temp table temp_mrkr_cc (
                gene_zdb_id      varchar(50),
		sp_acc_num	 varchar(50),
                cc_note          lvarchar(8192)
        )with no log;

--!echo 'Load cc_external.unl'
        load from cc_external.unl delimiter '$' insert into temp_mrkr_cc;

--!echo 'Update merged gene ids'
	update temp_mrkr_cc
	   set gene_zdb_id =  (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = gene_zdb_id)
         where gene_zdb_id in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 


        create temp table temp_nondupl_mrkr_cc (
                nondupl_gene_zdb_id      varchar(50),
		nondupl_sp_acc_num	 varchar(50),
                nondupl_cc_note          lvarchar(8192)
        )with no log;

        insert into temp_nondupl_mrkr_cc (nondupl_gene_zdb_id,nondupl_sp_acc_num,nondupl_cc_note)
         select distinct gene_zdb_id, sp_acc_num, cc_note
	          from temp_mrkr_cc;

        create temp table pre_external_note(
                p_extnote_zdb_id          varchar(50),
                p_extnote_data_zdb_id     varchar(50), 
                p_extnote_note            lvarchar(8192),
                p_extnote_source_zdb_id   varchar(50)
        )with no log;
        
        insert into pre_external_note (p_extnote_note,p_extnote_data_zdb_id, p_extnote_source_zdb_id)
                select nondupl_cc_note, dblink_zdb_id, "ZDB-PUB-020723-2"
	          from temp_nondupl_mrkr_cc, db_link
		 where nondupl_gene_zdb_id = dblink_linked_recid
		   and nondupl_sp_acc_num  = dblink_acc_num
		   and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-47";

        update pre_external_note
                        set p_extnote_zdb_id = get_id("EXTNOTE");


--!echo 'Insert EXTNOTE into zdb_active_data'
        insert into zdb_active_data (zactvd_zdb_id)
                select p_extnote_zdb_id from pre_external_note;
--!echo '		into zdb_active_data'

--!echo 'Insert into external_note'
        insert into external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note, extnote_source_zdb_id)
                   select * from pre_external_note;
--!echo '		into external_note'

--!echo 'Attribute EXTNOTE to the internal pub record'
        insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
                select p_extnote_zdb_id, p_extnote_source_zdb_id
                from pre_external_note;
--!echo '		into record_attribution'

--!echo 'unload accession# with no attribution'
-- accession from EC, Pfam, POSITE, InterPro, SwissProt
	unload to "accession_with_no_attribution" 
		select dblink_linked_recid, dblink_acc_num
 		  from db_link
 		 where dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-49",
						 "ZDB-FDBCONT-040412-50",
					 	 "ZDB-FDBCONT-040412-51",
						 "ZDB-FDBCONT-040412-48",
						 "ZDB-FDBCONT-040412-47")
                   and not exists (
                        select 'x' from record_attribution
                         where recattrib_data_zdb_id = dblink_zdb_id )
	      order by dblink_linked_recid ;


--rollback work;
commit work;
