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

--!echo 'Update merged gene ids'
	update db_link_with_dups
	   set linked_recid = (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = linked_recid)
         where linked_recid in (select zrepld_old_zdb_id 
                                  from zdb_replaced_data); 

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
        
-- per curator's request, GenBank and GenPept accession are no longer loaded from SP file
-- due to additional entries for certain database, we need to specify the db types
	update pre_db_link set dblink_fdbcont_zdb_id = 
				(select fdbcont_zdb_id 
				   from foreign_db_contains 
				  where lower(db_name)=lower(fdbcont_fdb_db_name)
                                    and fdbcont_organism_common_name = "Zebrafish"
                    and (  (fdbcont_fdbdt_super_type = "protein" 
                            and fdbcont_fdbdt_data_type = "domain")
                        or (fdbcont_fdbdt_super_type = "sequence" 
                            and fdbcont_fdbdt_data_type = "Polypeptide")
                        )
				); 

	delete from pre_db_link where exists (
		select d.dblink_zdb_id
		  from db_link d
		 where pre_db_link.linked_recid=d.dblink_linked_recid
           and pre_db_link.acc_num=d.dblink_acc_num
		   and pre_db_link.dblink_fdbcont_zdb_id = d.dblink_fdbcont_zdb_id
		);

	update pre_db_link set dblink_zdb_id = get_id("DBLINK"); 


--!echo 'Insert into zdb_active_data'
	insert into zdb_active_data (zactvd_zdb_id)
                  select dblink_zdb_id from pre_db_link p;

			 
--!echo 'Insert DBLINK into db_link'
	insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,
			     dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_length)  
		select linked_recid,acc_num,info,dblink_zdb_id,
			   acc_num_disp,dblink_fdbcont_zdb_id,length 
		  from pre_db_link p;

--!echo 'Attribute db links to the internal pub record'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
		select dblink_zdb_id, "ZDB-PUB-020723-2"
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
   	 	dalias_group		varchar(30),
		dalias_alias_lower	varchar(255)
              ) with no log;
	insert into pre_ac_alias (dalias_data_zdb_id, dalias_alias, 
		dalias_group, dalias_alias_lower)
	    select distinct dblink_zdb_id, alias_acc_num, "alias", 
		lower(alias_acc_num)
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
	insert into zdb_active_data (zactvd_zdb_id)
                    select dalias_zdb_id from pre_ac_alias;

--!echo 'Insert second AC into data_alias'
	insert into data_alias select * from pre_ac_alias;

--!echo 'Attribute second AC to the internal pub record'
	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
               select dalias_zdb_id, "ZDB-PUB-020723-2"
	       from pre_ac_alias;


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
		select distinct "SP_KW:"||sp_kwd_id, s.goterm_name, s.goterm_id,
			g.goterm_is_obsolete, g.goterm_is_secondary 
		  from spkw_goterm_with_dups s, go_term g
		 where s.goterm_id = g.goterm_go_id
	 	   and (g.goterm_is_obsolete = "t"
		       or g.goterm_is_secondary = "t")
		  order by g.goterm_is_secondary, g.goterm_is_obsolete;	
	delete from spkw_goterm_with_dups
		where goterm_id in (select goterm_go_id
				       from go_term
			              where goterm_is_obsolete = "t"
		                        or  goterm_is_secondary = "t"
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
			g.goterm_is_obsolete, g.goterm_is_secondary 
		  from ip_goterm_with_dups i,  go_term g
	         where i.goterm_id = g.goterm_go_id
	           and (g.goterm_is_obsolete = "t"
	             or g.goterm_is_secondary = "t")
		order by g.goterm_is_secondary, g.goterm_is_obsolete;	
	delete from ip_goterm_with_dups
		where goterm_id in (select goterm_go_id
				       from go_term
			              where goterm_is_obsolete = "t"
		                        or  goterm_is_secondary = "t"
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
			g.goterm_is_obsolete, g.goterm_is_secondary 
		  from ec_goterm_with_dups e, go_term g
	         where e.goterm_id = g.goterm_go_id
	           and (g.goterm_is_obsolete = "t"
		       or g.goterm_is_secondary = "t")
		order by g.goterm_is_secondary, g.goterm_is_obsolete;	
	delete from ec_goterm_with_dups
		where goterm_id in (select goterm_go_id
				       from go_term
			              where goterm_is_obsolete = "t"
		                        or  goterm_is_secondary = "t"
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

	create temp table pre_marker_go_evidence (
                mrkrgoev_zdb_id 	varchar(50), 
		mrkr_zdb_id		varchar(50),
		go_zdb_id		varchar(50),
		mrkrgoev_source		varchar(50),
                mrkrgoev_inference 	varchar(80),
		mrkrgoev_contributed_by	varchar(80)	 
	)with no log;

--!echo 'Load spkw'
	insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_contributed_by)
		select distinct sk.mrkr_zdb_id, goterm_zdb_id, "ZDB-PUB-020723-1", 
		       "SP_KW:"||sg.sp_kwd_id, "ZFIN SP keyword 2 GO"
		  from sp_kwd sk,  spkw_goterm_with_dups sg, go_term
		 where sk.sp_kwd = sg.sp_kwd_name
		   and goterm_go_id = sg.goterm_id;

--!echo 'Load intepro'
	insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source, 
					    mrkrgoev_inference, mrkrgoev_contributed_by)
		select distinct db.linked_recid, goterm_zdb_id, "ZDB-PUB-020724-1",
		       "InterPro:"||ip.ip_acc,	"ZFIN InterPro 2 GO"
		  from pre_db_link db, ip_goterm_with_dups ip, go_term
	 	 where db.acc_num = ip.ip_acc
		   and goterm_go_id = ip.goterm_id;
	
--!echo 'Load ec'
        insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source,  
					    mrkrgoev_inference, mrkrgoev_contributed_by)
		select distinct db.linked_recid, goterm_zdb_id, "ZDB-PUB-031118-3", 
		       "EC:"||ec.ec_acc, "ZFIN EC acc 2 GO"
		from pre_db_link db, ec_goterm_with_dups ec, go_term
		where db.acc_num = ec.ec_acc
		  and goterm_go_id = ec.goterm_id;


	update pre_marker_go_evidence set mrkrgoev_zdb_id = get_id ("MRKRGOEV");

--!echo 'do not include "unknown" terms and root terms if any'
        delete from pre_marker_go_evidence where go_zdb_id in 
		(select goterm_zdb_id 
		   from go_term 
		  where goterm_go_id in ("0005554", "0000004", "0008372",
					 "0005575", "0003674", "0008150"));

-- if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one
-- db trigger is added for this purpose. 


--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select mrkrgoev_zdb_id from pre_marker_go_evidence;

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence(mrkrgoev_zdb_id,mrkrgoev_mrkr_zdb_id, mrkrgoev_go_term_zdb_id,
				mrkrgoev_source_zdb_id, mrkrgoev_evidence_code,
				mrkrgoev_date_entered,mrkrgoev_date_modified,mrkrgoev_contributed_by,
				mrkrgoev_modified_by)
		select p.mrkrgoev_zdb_id, p.mrkr_zdb_id, p.go_zdb_id,
		       p.mrkrgoev_source, "IEA", CURRENT,CURRENT,
		       p.mrkrgoev_contributed_by, p.mrkrgoev_contributed_by
		  from pre_marker_go_evidence p
		  where not exists (Select 'x' from marker a
		       	   	  	      where a.mrkr_zdb_id = p.mrkr_zdb_id
					      and a.mrkr_abbrev not like 'WITHDRAWN%');

	
--	db trigger attributes MRKRGOEV to the internal pub record
--	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
--	  	               select mrkrgoev_zdb_id,mrkrgoev_source from pre_marker_go_evidence;

-- load inference_group_member
	insert into inference_group_member (infgrmem_inferred_from, infgrmem_mrkrgoev_zdb_id)
			select mrkrgoev_inference, mrkrgoev_zdb_id
			  from pre_marker_go_evidence;
		

---------------- loading cc field -----------------------------

-- loading cc
        create temp table temp_mrkr_cc (
                gene_zdb_id      varchar(50),
		sp_acc_num	 varchar(50),
                cc_note          lvarchar(32613)
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
        create temp table pre_external_note(
                p_extnote_zdb_id          varchar(50),
                p_extnote_data_zdb_id     varchar(50), 
                p_extnote_note            lvarchar(32613)
        )with no log;
         insert into pre_external_note (p_extnote_note,p_extnote_data_zdb_id)
                select cc_note, dblink_zdb_id
	          from temp_mrkr_cc, db_link
		 where gene_zdb_id = dblink_linked_recid
		   and sp_acc_num  = dblink_acc_num
		   and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-47";

        update pre_external_note
                        set p_extnote_zdb_id = get_id("EXTNOTE");


--!echo 'Insert EXTNOTE into zdb_active_data'
        insert into zdb_active_data (zactvd_zdb_id)
                select p_extnote_zdb_id from pre_external_note;

--!echo 'Attribute EXTNOTE to the internal pub record'
        insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
                select p_extnote_zdb_id, "ZDB-PUB-020723-2"
                from pre_external_note;

--!echo 'Insert into external_note'
        insert into external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note)
                   select * from pre_external_note;

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
   		   and dblink_zdb_id not in (
			select  recattrib_data_zdb_id
          		  from  record_attribution )
	      order by dblink_linked_recid ;


--rollback work;
commit work;
