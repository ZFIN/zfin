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

--the unloaded record if already in, only add zdb_id into record_attribution with SWISS_PROT source
--	create temp table exist_record (
--		extrecd_zdb_id	varchar(50),
--		new_zdb_id	varchar(50)
--		) with no log;

------------------ loading db_link --------------------------

	create temp table db_link_with_dups (
               linked_recid varchar(50),
               db_name varchar(50),
               acc_num varchar(50),
               length integer
              ) with no log;

--!echo 'Load dr_dblink.unl'
	load from dr_dblink.unl insert into db_link_with_dups;

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
	create index pre_db_link_acc_index on pre_db_link (acc_num);

	insert into pre_db_link (linked_recid,db_name,acc_num,length,info,acc_num_disp)     
	       		select distinct *, today ||" Swiss-Prot",acc_num 
			  from db_link_with_dups;
        
	update pre_db_link set dblink_fdbcont_zdb_id = 
				(select fdbcont_zdb_id 
			  	   from foreign_db_contains 
				  where lower(trim(db_name))=lower(fdbcont_fdb_db_name) 
				    and fdbcont_fdbdt_data_type like '%cDNA%')
			where db_name = "Genbank"; 

	update pre_db_link set dblink_fdbcont_zdb_id = 
				(select fdbcont_zdb_id 
				   from foreign_db_contains 
				  where lower(trim(db_name))=lower(fdbcont_fdb_db_name)) 
			where db_name <> "Genbank"; 

--!echo 'Genbank has to be treated differently here since the type is unknown'	
	delete from pre_db_link where exists (
		select d.dblink_zdb_id
		from db_link d
		where pre_db_link.linked_recid=d.dblink_linked_recid
                  and pre_db_link.acc_num=d.dblink_acc_num
		  and pre_db_link.db_name <> "Genbank"
		  and pre_db_link.dblink_fdbcont_zdb_id = d.dblink_fdbcont_zdb_id
		);

	delete from pre_db_link where exists (
		select d.dblink_zdb_id
		from db_link d
		where pre_db_link.linked_recid=d.dblink_linked_recid
                  and pre_db_link.acc_num = d.dblink_acc_num
		  and pre_db_link.db_name = "Genbank"
		  and d.dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-36","ZDB-FDBCONT-040412-37")
		) ;

	update pre_db_link set dblink_zdb_id = get_id("DBLINK"); 
select * from pre_db_link
	where linked_recid not in (select mrkr_zdb_id from marker);

--!echo 'Insert into zdb_active_data'
	insert into zdb_active_data 
                  select dblink_zdb_id from pre_db_link p;

--!echo 'Insert DBLINK into db_link'
	insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,
			     dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_length)  
		select linked_recid,acc_num,info,dblink_zdb_id,
			acc_num_disp,dblink_fdbcont_zdb_id,length 
		  from pre_db_link p;

--!echo 'Attribute db links to the internal pub record'
	insert into record_attribution
		select dblink_zdb_id, "ZDB-PUB-020723-2",''
		from pre_db_link;

------------------- loading ac alias ------------------------

	create temp table temp_ac_alias(
		prm_acc_num varchar(50),	
		alias_acc_num varchar(50)
		) with no log; 

--!echo 'Load ac_dalias.unl'
	load from ac_dalias.unl insert into temp_ac_alias;
	create temp table pre_ac_alias(
		dalias_zdb_id		varchar(50),
    		dalias_data_zdb_id	varchar(50),
    		dalias_alias		varchar(120),
   	 	dalias_group		varchar(30)
              ) with no log;
	insert into pre_ac_alias (dalias_data_zdb_id, dalias_alias, dalias_group)
	    select distinct dblink_zdb_id, alias_acc_num, "alias"
	    from pre_db_link db, temp_ac_alias al
 	    where db.acc_num = al.prm_acc_num;

	delete from pre_ac_alias
	       where exists 	
			( select d.dalias_zdb_id
		 	    from data_alias d
			   where d.dalias_data_zdb_id = pre_ac_alias.dalias_data_zdb_id
		  	     and d.dalias_alias = pre_ac_alias.dalias_alias );	
    
        update pre_ac_alias
                set dalias_zdb_id = get_id("DALIAS");

--!echo 'Insert DALIAS into zdb_active_data'
	insert into zdb_active_data 
                    select dalias_zdb_id from pre_ac_alias;

--!echo 'Insert second AC into data_alias'
	insert into data_alias select * from pre_ac_alias;

--!echo 'Attribute second AC to the internal pub record'
	insert into record_attribution 
               select dalias_zdb_id, "ZDB-PUB-020723-2",''
	       from pre_ac_alias;


------------------------ loading gn alias --------------------
       create temp table gn_dalias_with_dups (
                  prm_acc             varchar(50),      
                  gnalias_data_zdb_id varchar(50),
                  gnalias_gname       varchar(120)
                )with no log;

--!echo 'Load gn_dalias.unl'
        load from gn_dalias.unl insert into gn_dalias_with_dups;
        create temp table pre_gn_dalias (
                  dalias_zdb_id varchar(50),
                  dalias_data_zdb_id varchar(50),
                  dalias_alias       varchar(120),
                  dalias_group       varchar(30)
                )with no log;                            
        insert into pre_gn_dalias 
                (dalias_data_zdb_id, dalias_alias, dalias_group)
                 select distinct gnalias_data_zdb_id,
                                 gnalias_gname, "alias"
                 from gn_dalias_with_dups;    

	delete from pre_gn_dalias
	       where exists 
               	       ( select d.dalias_zdb_id
                  	   from data_alias d
                	  where d.dalias_data_zdb_id = pre_gn_dalias.dalias_data_zdb_id
                  	    and d.dalias_alias = pre_gn_dalias.dalias_alias ); 

        update pre_gn_dalias
                set dalias_zdb_id = get_id("DALIAS");

--!echo 'Insert DALIAS into zdb_active_data'
        insert into zdb_active_data
                 select dalias_zdb_id from pre_gn_dalias;
select * from pre_gn_dalias where dalias_data_zdb_id not in (
	select zactvd_zdb_id from zdb_active_data);

--!echo 'Insert GN into data_alias'
        insert into data_alias 
                        select * from pre_gn_dalias;

--!echo 'Attribute GN to the internal pub record'
        insert into record_attribution 
                (recattrib_data_zdb_id, recattrib_source_zdb_id)
                select gn.dalias_zdb_id,"ZDB-PUB-020723-2"
                from pre_gn_dalias gn;





------------------------- loading marker go evidence  --------------------


-- load in information from three GO term translation tables
-- 1. Swiss-Prot keyword to GO	
-- 2. InterPro to GO
-- 3. ec to GO

	create temp table spkw_goterm_with_dups (
		sp_kwd  varchar(80),
		goterm_name  varchar(100),
		goterm_id  varchar(10)
	)with no log;

--!echo 'Load sp_mrkrgoterm.unl: spkwtogo translation table'
	load from sp_mrkrgoterm.unl insert into spkw_goterm_with_dups;
	create index spkw_goterm_with_dups_keyword_index
		on spkw_goterm_with_dups (sp_kwd);

	create temp table ip_goterm_with_dups (
		ip_acc  varchar(20),
		goterm_name  varchar(100),
		goterm_id  varchar(10)
		)with no log;

--!echo 'Load ip_mrkrgoterm.unl: iptogo translation table'
	load from ip_mrkrgoterm.unl insert into ip_goterm_with_dups;

--!echo 'Load ec_mrkrgoterm.unl: ectogo translation table'

	create temp table ec_goterm_with_dups (
		ec_acc  varchar(20),
		goterm_name  varchar(100),
		goterm_id  varchar(10)
		)with no log;

	load from ec_mrkrgoterm.unl insert into ec_goterm_with_dups;

--!echo ' load in information of keywords with specific S-P record'
	create temp table sp_kwd (
		mrkr_zdb_id varchar(50),
		sp_kwd      varchar(80)
		)with no log;

--!echo 'Load kd_spkeywd.unl'
	load from kd_spkeywd.unl insert into sp_kwd;


	create temp table pre_marker_go_evidence (
                mrkrgoev_zdb_id 	varchar(50), 
		mrkr_zdb_id		varchar(50),
		go_zdb_id		varchar(50),
		mrkrgoev_source		varchar(50),
                mrkrgoev_evidence_code	char(3),
                mrkrgoev_notes 		varchar(255),
		mrkrgoev_ref		varchar(80)	 
	)with no log;

--!echo 'Load spkw'
	insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, mrkrgoev_ref)
		select distinct sk.mrkr_zdb_id, goterm_zdb_id, "ZDB-PUB-020723-1", "ZFIN SP keyword 2 GO"
		  from sp_kwd sk,  spkw_goterm_with_dups sg, go_term
		 where sk.sp_kwd = sg.sp_kwd
		   and goterm_go_id = sg.goterm_id;

--!echo 'Load intepro'
	insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, mrkrgoev_ref)
		select distinct db.linked_recid, goterm_zdb_id, "ZDB-PUB-020724-1", "ZFIN InterPro 2 GO"
		  from pre_db_link db, ip_goterm_with_dups ip, go_term
	 	 where db.acc_num = ip.ip_acc
		   and goterm_go_id = ip.goterm_id;
	
--!echo 'Load ec'
        insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, mrkrgoev_ref)
		select distinct db.linked_recid, goterm_zdb_id, "ZDB-PUB-031118-3", "ZFIN EC acc 2 GO"
		from pre_db_link db, ec_goterm_with_dups ec, go_term
		where db.acc_num = ec.ec_acc
		  and goterm_go_id = ec.goterm_id;


	update pre_marker_go_evidence set mrkrgoev_zdb_id = get_id ("MRKRGOEV");
	update pre_marker_go_evidence set mrkrgoev_evidence_code = "IEA";

--!echo 'these 3 have term name "* unknown", we donot want to include'
        delete from pre_marker_go_evidence where go_zdb_id in 
		(select goterm_zdb_id from go_term where goterm_name like "% unknown");

--!echo 'if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one'
	delete from zdb_active_data where zactvd_zdb_id in 
				      (	select m.mrkrgoev_zdb_id 
					  from marker_go_term_evidence m, go_term u,
						pre_marker_go_evidence p, go_term g
					 where m.mrkrgoev_mrkr_zdb_id = p.mrkr_zdb_id
					   and m.mrkrgoev_go_term_zdb_id = u.goterm_zdb_id	
					   and p.go_zdb_id  = g.goterm_zdb_id
					   and u.goterm_ontology = g.goterm_ontology
					   and u.goterm_go_id in ("0005554", "0000004", "0008372")
					   and u.goterm_go_id <> g.goterm_go_id  );




--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select mrkrgoev_zdb_id from pre_marker_go_evidence;

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence(mrkrgoev_zdb_id,mrkrgoev_mrkr_zdb_id, mrkrgoev_go_term_zdb_id,
				mrkrgoev_source_zdb_id, mrkrgoev_evidence_code,mrkrgoev_notes,
				mrkrgoev_date_entered,mrkrgoev_date_modified,mrkrgoev_contributed_by,
				mrkrgoev_modified_by)
		select mrkrgoev_zdb_id,mrkr_zdb_id, go_zdb_id,
		       mrkrgoev_source, mrkrgoev_evidence_code,mrkrgoev_notes,
		       CURRENT,CURRENT,mrkrgoev_ref, mrkrgoev_ref 
		  from pre_marker_go_evidence;

	
--	db trigger attributes MRKRGOEV to the internal pub record
--	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
--	  	               select mrkrgoev_zdb_id,mrkrgoev_source from pre_marker_go_evidence;


	

---------------- loading cc field -----------------------------

-- loading cc
        create temp table temp_mrkr_cc (
                data_zdb_id      varchar(50),
                cc_note          varchar(255)
        )with no log;

--!echo 'Load cc_external.unl'
        load from cc_external.unl insert into temp_mrkr_cc;

        create temp table pre_external_note(
                extnote_zdb_id          varchar(50),
                extnote_note_file       varchar(255), 
                extnote_note            clob
        )with no log;
         insert into pre_external_note (extnote_note,extnote_note_file)
                select FILETOCLOB(cc_note,'client','external_note','extnote_note'),cc_note from temp_mrkr_cc;

        update pre_external_note
                        set extnote_zdb_id = get_id("EXTNOTE");


--!echo 'Insert EXTNOTE into zdb_active_data'
        insert into zdb_active_data
                select extnote_zdb_id from pre_external_note;

--!echo 'Insert into external_note'
        insert into external_note
                select extnote_zdb_id, extnote_note  from pre_external_note;

--!echo 'Attribute EXTNOTE to the internal pub record'
        insert into record_attribution
                select extnote_zdb_id, "ZDB-PUB-020723-2",''
                from pre_external_note;


        create temp table data_external_note_with_dups(
                dextnote_data_zdb_id    varchar(50),
                dextnote_extnote_zdb_id varchar(50)
         )with no log;


        insert into data_external_note_with_dups
                select data_zdb_id, extnote_zdb_id
                from temp_mrkr_cc t, pre_external_note e
                where t.cc_note = e.extnote_note_file;

--!echo 'Insert into data_external_note'
        insert into data_external_note (dextnote_data_zdb_id, dextnote_extnote_zdb_id)
                        select distinct * from data_external_note_with_dups;


--rollback work;
commit work;
