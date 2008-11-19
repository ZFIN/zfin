
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
		select distinct db.dblink_linked_recid, goterm_zdb_id, "ZDB-PUB-031118-3", 
		       "EC:"||ec.ec_acc, "ZFIN EC acc 2 GO"
		from db_link db, ec_goterm_with_dups ec, go_term
		where db.dblink_acc_num = ec.ec_acc
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
		select mrkrgoev_zdb_id,mrkr_zdb_id, go_zdb_id,
		       mrkrgoev_source, "IEA", CURRENT,CURRENT,
		       mrkrgoev_contributed_by, mrkrgoev_contributed_by
		  from pre_marker_go_evidence;

	
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
