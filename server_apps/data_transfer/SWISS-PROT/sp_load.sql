
-- SWISS-PROT file is parsed, after checked for consistence with ZFIN, into
-- informix loadable .unl files. From original SWISS-PROT file we got, 
-- the SWISS-PROT accession number(AC) and cross database references(DR) are
-- loaded into db_link table, Gene alias(GN) are added to data_alias table,
-- Keyword(KW) and InterPro accession numbers are translated into GO terms
-- through two available translation table and GO ontology file. Comments(CC)
-- are stored in external_note table. All the information loaded to ZFIN from
-- SWISS-PROT is attributed to an internal pub record which explains the whole
-- process of SWISS-PROT loading in record_attribution table.
  	

begin work;

------------------ loading db_link --------------------------

	create temp table db_link_with_dups (
               linked_recid varchar(50),
               db_name varchar(50),
               acc_num varchar(50)
              ) with no log;

--!echo 'Load dr_dblink.unl'
	load from dr_dblink.unl insert into db_link_with_dups;

	create temp table pre_db_link (
               linked_recid varchar(50),
               db_name varchar(50),
               acc_num varchar(50),
               info varchar(80),
	       dblink_zdb_id varchar(50)	
              ) with no log;
	create index pre_db_link_acc_index
			on pre_db_link (acc_num);
	insert into pre_db_link 
               (linked_recid,db_name,acc_num,info)     
	       		select distinct *, expr(today)||" Swiss-Prot" 
			from db_link_with_dups;
	update pre_db_link 
			set dblink_zdb_id = get_id("DBLINK"); 

--!echo 'Insert into zdb_active_data'
	insert into zdb_active_data 
                    	select dblink_zdb_id from pre_db_link;
--!echo 'Insert DBLINK into db_link'
	insert into db_link select * from pre_db_link;

--!echo 'Attribute db links to the internal pub record'
	insert into record_attribution
		select dblink_zdb_id, "ZDB-PUB-020723-2", "related"
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
   	 	dalias_significance	int
              ) with no log;
	insert into pre_ac_alias
	    select get_id("DALIAS"), dblink_zdb_id, 
                   alias_acc_num, 1
	    from pre_db_link db, temp_ac_alias al
 	    where db.acc_num = al.prm_acc_num;

--!echo 'Insert DALIAS into zdb_active_data'
	insert into zdb_active_data 
                    select dalias_zdb_id from pre_ac_alias;

--!echo 'Insert second AC into data_alias'
	insert into data_alias select * from pre_ac_alias;

--!echo 'Attribute second AC to the internal pub record'
	insert into record_attribution 
               select dalias_zdb_id, "ZDB-PUB-020723-2", "related"
	       from pre_ac_alias;

------------------- loading gn alias ------------------------
	
	create temp table gn_dalias_with_dups (
		  prm_acc	      varchar(50),	
                  gnalias_data_zdb_id varchar(50),
     		  gnalias_gname       varchar(120)
		)with no log;

--!echo 'Load gn_dalias.unl'
	load from gn_dalias.unl insert into gn_dalias_with_dups;
	create temp table pre_gn_dalias (
		  dalias_zdb_id	varchar(50),
                  dalias_data_zdb_id varchar(50),
     		  dalias_alias       varchar(120),
		  dalias_significance  int
		)with no log;				 
	insert into pre_gn_dalias 
		(dalias_data_zdb_id, dalias_alias, dalias_significance)
                 select distinct gnalias_data_zdb_id,
			         gnalias_gname, 1
		 from gn_dalias_with_dups;	  
	update pre_gn_dalias
		set dalias_zdb_id = get_id("DALIAS");

--!echo 'Insert DALIAS into zdb_active_data'
	insert into zdb_active_data
		 select dalias_zdb_id from pre_gn_dalias;

--!echo 'Insert GN into data_alias'
	insert into data_alias 
			select * from pre_gn_dalias;

--!echo 'Attribute GN to the internal pub record'
	insert into record_attribution 
		select gn.dalias_zdb_id,"ZDB-PUB-020723-2", "related"
		from pre_gn_dalias gn;

	
------------------------- loading GO term  --------------------

-- load in go term ontology
	create temp table goterm_onto_with_dups (
		goterm_id	varchar(10),
		goterm_name	varchar(100),
		goterm_onto	varchar(30)
	)with no log;

--!echo 'Load ontology.unl'
	load from ontology.unl insert into goterm_onto_with_dups;

-- eliminate duplication from the original file
	create temp table goterm_onto (		
		goterm_id	varchar(10),
		goterm_name	varchar(100),
		goterm_onto	varchar(30)
	)with no log;
	create index goterm_onto_index on goterm_onto (goterm_id);
	insert into goterm_onto
		select distinct * from goterm_onto_with_dups;

-- load in GO term with multiple ids
	create temp table secid_goterm_onto_with_dups (
		sec_goterm_id	varchar(10),	
		prm_goterm_id	varchar(10),	
		goterm_name	varchar(100),
		goterm_onto	varchar(30)
	)with no log;

--!echo 'Load ontsecgoid.unl'
	load from ontsecgoid.unl insert into secid_goterm_onto_with_dups;
-- eliminate duplication 
	create temp table secid_goterm_onto (
		sec_goterm_id	varchar(10),	
		prm_goterm_id	varchar(10),	
		goterm_name	varchar(100),
		goterm_onto	varchar(30)
	)with no log;
	insert into secid_goterm_onto 
		select distinct * from secid_goterm_onto_with_dups;

-- load in information from two GO term translation tables
-- 1. Swiss-Prot keyword to GO	
-- 2. InterPro to GO
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

-- load in information of keywords with specific S-P record
	create temp table sp_kwd (
		mrkr_zdb_id varchar(50),
		sp_kwd      varchar(80)
		)with no log;

--!echo 'Load kd_spkeywd.unl'
	load from kd_spkeywd.unl insert into sp_kwd;

	create temp table marker_goid_with_dups (
		mrkr_zdb_id	varchar(50),
		goterm_id	varchar(10)	
	)with no log;
	insert into marker_goid_with_dups
		select sk.mrkr_zdb_id, sg.goterm_id
		from sp_kwd sk,  spkw_goterm_with_dups sg
		where sk.sp_kwd = sg.sp_kwd;
	create temp table marker_goid (
		mrkr_zdb_id	varchar(50),
		goterm_id	varchar(10),
		goterm_source	varchar(50)
	)with no log;
	create index marker_goid_id_index 
		 	on marker_goid (goterm_id);
	insert into marker_goid 
			select distinct *,"ZDB-PUB-020723-1" 
			from marker_goid_with_dups;
	delete from marker_goid_with_dups;
	insert into marker_goid_with_dups
		select db.linked_recid, ip.goterm_id
		from pre_db_link db, ip_goterm_with_dups ip
		where db.acc_num = ip.ip_acc;
	insert into marker_goid
			select distinct *, "ZDB-PUB-020724-1"
			from marker_goid_with_dups;	

	create temp table go_term_with_dups (
		goterm_id	varchar(10),
		goterm_name	varchar(100),	
		goterm_onto	varchar(30)
	)with no log;
	insert into go_term_with_dups
		select o.goterm_id, o.goterm_name, o.goterm_onto
		from goterm_onto o, marker_goid m
		where m.goterm_id = o.goterm_id;
	insert into go_term_with_dups
		select o.prm_goterm_id, o.goterm_name, o.goterm_onto
		from secid_goterm_onto o, marker_goid m
		where m.goterm_id = o.sec_goterm_id;
	create temp table pre_go_term (
		goterm_zdb_id	varchar(50),
		goterm_id	varchar(10),
		goterm_name	varchar(100),	
		goterm_onto	varchar(30)
	)with no log;

	insert into pre_go_term (goterm_id, goterm_name, goterm_onto)
			select distinct * from go_term_with_dups;
	update pre_go_term set goterm_zdb_id = get_id("GOTERM");

--!echo 'Insert GOTERM into zdb_active_data'
	insert into zdb_active_data 
		select goterm_zdb_id from pre_go_term;

--!echo 'Insert into go_term'
	insert into go_term
			select * from pre_go_term;

--!echo 'Attribute GOTERM to the internal pub record'
	insert into record_attribution 
		select goterm_zdb_id,"ZDB-PUB-020723-2", "related"
		from go_term;
 
	
-- Load marker_go_term 
	create temp table marker_goterm_with_dups (	
		mrkr_zdb_id 	varchar(50),
		mrkr_goterm_zdb_id	varchar(50),
		mrkr_goterm_source	varchar(50)	
	)with no log;
	insert into marker_goterm_with_dups
		select m.mrkr_zdb_id, g.goterm_zdb_id, m.goterm_source
		from marker_goid m, go_term g
		where m.goterm_id = g.goterm_go_id;
	insert into marker_goterm_with_dups
		select m.mrkr_zdb_id, g.goterm_zdb_id, m.goterm_source
		from marker_goid m, go_term g, secid_goterm_onto o
		where m.goterm_id = o.sec_goterm_id and
			o.prm_goterm_id = g.goterm_go_id;
	
	create temp table pre_marker_goterm (
		mrkrgo_zdb_id	varchar(50),
		mrkr_zdb_id 	varchar(50),
		mrkr_goterm_zdb_id	varchar(50)	
	)with no log;
	insert into pre_marker_goterm (mrkr_zdb_id, mrkr_goterm_zdb_id)
		select distinct mrkr_zdb_id, mrkr_goterm_zdb_id 
		from marker_goterm_with_dups;
	update pre_marker_goterm set mrkrgo_zdb_id = get_id ("MRKRGO");

--!echo 'Insert MRKRGO into zdb_active_data'
	insert into zdb_active_data
		select mrkrgo_zdb_id from pre_marker_goterm;

--!echo 'Insert into marker_go_term'
	insert into marker_go_term 
		select mrkrgo_zdb_id, mrkr_zdb_id, mrkr_goterm_zdb_id
		from pre_marker_goterm;

	create temp table mrkrgo_source (
		mrkrgo_zdb_id	varchar(50),
		mrkrgo_source	varchar(50)
	)with no log;
	insert into mrkrgo_source
		select m1.mrkrgo_zdb_id, m2.mrkr_goterm_source
		from marker_go_term m1, marker_goterm_with_dups m2
		where m1.mrkrgo_mrkr_zdb_id = m2.mrkr_zdb_id and
		      m1.mrkrgo_go_term_zdb_id = m2.mrkr_goterm_zdb_id;	
	  
--!echo 'Attribute MRKRGO to the internal pub record'
	insert into record_attribution
	  	select *, "related" from mrkrgo_source;

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence
		select *, "IEA" from mrkrgo_source;



-- loading cc 
	create temp table temp_mrkr_cc (
		data_zdb_id	 varchar(50),
		cc_note 	 clob
	)with no log;

--!echo 'Load cc_external.unl'
	load from cc_external.unl insert into temp_mrkr_cc;

	create temp table pre_external_note(
  	 	extnote_zdb_id		varchar(50),
   	 	extnote_note		clob  	 
  	)with no log;
	insert into pre_external_note (extnote_note)
		select distinct cc_note
		from temp_mrkr_cc;
	update pre_external_note 
			set extnote_zdb_id = get_id("EXTNOTE");


--!echo 'Insert EXTNOTE into zdb_active_data'
	insert into zdb_active_data
		select extnote_zdb_id from pre_external_note;

--!echo 'Insert into external_note'
	insert into external_note
		select * from pre_external_note;
	
--!echo 'Attribute EXTNOTE to the internal pub record'
 	insert into record_attribution 
		select extnote_zdb_id, "ZDB-PUB-020723-2", "related" 
		from pre_external_note;

	
	create temp table data_external_note_with_dups(
    		dextnote_data_zdb_id	varchar(50),
    		dextnote_extnote_zdb_id varchar(50)
 	 )with no log;
	insert into data_external_note_with_dups
		select data_zdb_id, extnote_zdb_id
		from temp_mrkr_cc t, external_note e
		where t.cc_note = e.extnote_note;

--!echo 'Insert into data_external_note'
	insert into data_external_note
			select distinct * from data_external_note_with_dups;


commit work;