
begin work;

------------------------- loading marker go evidence  --------------------


-- load in information from one GO term translation table, ec2go

--!echo 'Load ec_mrkrgoterm.unl: ectogo translation table'

	create temporary table ec_goterm_with_dups (
		ec_acc  text,
		goterm_name  text,
		goterm_id text
		);
        copy ec_goterm_with_dups from '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ec_mrkrgoterm.unl' (delimiter '|');
	
--!echo 'unload obsolete or secondary goterm, send to curators, delete from loading'
	create view ec2go_obsl_secd as
		select distinct 'EC:'||ec_acc, e.goterm_name, e.goterm_id,
			t.term_is_obsolete, t.term_is_secondary 
		  from ec_goterm_with_dups e, term t
	         where 'GO:'||e.goterm_id = t.term_ont_id
	           and (t.term_is_obsolete = 't'
		       or t.term_is_secondary = 't')
		order by t.term_is_secondary, t.term_is_obsolete;
	\copy (select * from ec2go_obsl_secd) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ec2go_obsl_secd.unl' with delimiter as '|' null as '';	
	drop view ec2go_obsl_secd;
		
	delete from ec_goterm_with_dups
		where 'GO:'||e.goterm_id in (select term_ont_id
				       from term
			              where term_is_obsolete = 't'
		                        or  term_is_secondary = 't'
				     );		

	create temporary table pre_marker_go_evidence (
                mrkrgoev_zdb_id 	text, 
		mrkr_zdb_id		text,
		go_zdb_id		text,
		mrkrgoev_source		text,
                mrkrgoev_inference 	text
	);

	
--!echo 'Load ec'
        insert into pre_marker_go_evidence (mrkr_zdb_id, go_zdb_id, mrkrgoev_source,  
					    mrkrgoev_inference, mrkrgoev_contributed_by)
		select distinct db.dblink_linked_recid, term_zdb_id, 'ZDB-PUB-031118-3' as pubid, 
		       'EC:'||ec.ec_acc
		from db_link db, ec_goterm_with_dups ec, term
		where db.dblink_acc_num = ec.ec_acc
		  and term_ont_id = 'GO:'||ec.goterm_id;


	update pre_marker_go_evidence set mrkrgoev_zdb_id = get_id ('MRKRGOEV');

--!echo 'do not include 'unknown' terms and root terms if any'
        delete from pre_marker_go_evidence where go_zdb_id in 
		(select term_zdb_id 
		   from term 
		  where term_ont_id in ('GO:0005554', 'GO:0000004', 'GO:0008372',
					 'GO:0005575', 'GO:0003674', 'GO:0008150'));

-- if a known go term is assigned to the same marker that has an unknown go term, delete the unknown one
-- db trigger is added for this purpose. 


--!echo 'Insert MRKRGOEV into zdb_active_data'
	insert into zdb_active_data
		select mrkrgoev_zdb_id from pre_marker_go_evidence;

--!echo 'Insert into marker_go_term_evidence'
	insert into marker_go_term_evidence(mrkrgoev_zdb_id,mrkrgoev_mrkr_zdb_id, mrkrgoev_term_zdb_id,
				mrkrgoev_source_zdb_id, mrkrgoev_evidence_code,
				mrkrgoev_date_entered,mrkrgoev_date_modified,mrkrgoev_annotation_organization,mrkrgoev_external_load_date)
		select p.mrkrgoev_zdb_id,p.mrkr_zdb_id, p.go_zdb_id,
		       p.mrkrgoev_source, 'IEA' as iea, now() as time1, now() as time2, '5' as org, now() as time3
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


\copy (select * from pre_marker_go_evidence) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/checkec2go' with delimiter as '|' null as '';

--rollback work;
commit work;
