
begin work;

------------------------- loading marker go evidence  --------------------


-- load in information from one GO term translation table, ec2go

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
		where "GO:"||e.goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = "t"
		                        or  term_is_secondary = "t"
				     );		

	create temp table pre_marker_go_evidence (
                mrkrgoev_zdb_id 	varchar(50), 
		mrkr_zdb_id		varchar(50),
		go_zdb_id		varchar(50),
		mrkrgoev_source		varchar(50),
                mrkrgoev_inference 	varchar(80),
		mrkrgoev_contributed_by	varchar(80)	 
	)with no log;

	
--!echo 'Load ec'
        insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source,  
					    mrkrgoev_inference, mrkrgoev_contributed_by)
		select distinct db.dblink_linked_recid, term_zdb_id, "ZDB-PUB-031118-3", 
		       "EC:"||ec.ec_acc, "ZFIN EC acc 2 GO"
		from db_link db, ec_goterm_with_dups ec, term
		where db.dblink_acc_num = ec.ec_acc
		  and term_ont_id = "GO:"||ec.goterm_id;


	update pre_marker_go_evidence set mrkrgoev_zdb_id = get_id ("MRKRGOEV");

--!echo 'do not include "unknown" terms and root terms if any'
        delete from pre_marker_go_evidence where go_zdb_id in 
		(select term_zdb_id 
		   from term 
		  where term_ont_id in ("GO:0005554", "GO:0000004", "GO:0008372",
					 "GO:0005575", "GO:0003674", "GO:0008150"));

-- if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one
-- db trigger is added for this purpose. 


--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select mrkrgoev_zdb_id from pre_marker_go_evidence;

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence(mrkrgoev_zdb_id,mrkrgoev_mrkr_zdb_id, mrkrgoev_term_zdb_id,
				mrkrgoev_source_zdb_id, mrkrgoev_evidence_code,
				mrkrgoev_date_entered,mrkrgoev_date_modified,mrkrgoev_contributed_by,
				mrkrgoev_modified_by)
		select p.mrkrgoev_zdb_id,p.mrkr_zdb_id, p.go_zdb_id,
		       p.mrkrgoev_source, "IEA", CURRENT,CURRENT,
		       p.mrkrgoev_contributed_by, p.mrkrgoev_contributed_by
		  from pre_marker_go_evidence p
		  where not exists (Select 'x' from marker a
		  	    	   	   where a.mrkr_zdb_id = p.mrkr_zdb_id
					   and a.mrkr_abbrev like 'WITHDRAWN%');

	
--	db trigger attributes MRKRGOEV to the internal pub record
--	insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
--	  	               select mrkrgoev_zdb_id,mrkrgoev_source from pre_marker_go_evidence;

-- load inference_group_member
	insert into inference_group_member (infgrmem_inferred_from, infgrmem_mrkrgoev_zdb_id)
			select mrkrgoev_inference, mrkrgoev_zdb_id
			  from pre_marker_go_evidence;


unload to 'checkec2go' select * from pre_marker_go_evidence;

--rollback work;
commit work;
