create or replace function regen_construct_marker(constructZdbId text)
returns void as $$

 declare mrel_id  marker_relationship.mrel_zdb_id%TYPE;
  mrel_1_id  marker_relationship.mrel_mrkr_1_zdb_id%TYPE;
  mrel_2_id  marker_relationship.mrel_mrkr_2_zdb_id%TYPE;
  mrel_type  marker_relationship.mrel_type%TYPE;

  begin
  

    if exists (select 'z' from marker where mrkr_Zdb_id = constructZdbId)
    then

       update marker
    	   set mrkr_name = (Select construcT_name 
	       		   	   from construct 
				   where construct_Zdb_id = constructZdbId
				   and construct_zdb_id = mrkr_zdb_id)
  	   where exists (Select 'x' from construct
  	       	       		where construct_zdb_id = constructZdbId
				and mrkr_Zdb_id = construct_zdb_id);

       update marker
    	   set mrkr_abbrev = (Select construct_name
	       		     	from construct 
				      where construct_Zdb_id = mrkr_zdb_id
      		  	     	       and construct_zdb_id = constructZdbId)
  	   where exists (Select 'x' from construct
  	       	             where construct_zdb_id = mrkr_zdb_id
		             and construct_zdb_id = constructZdbId);

       delete from marker_relationship
       	      where constructZdbID = mrel_mrkr_1_zdb_id;


	for mrel_id, mrel_1_id, mrel_2_id, mrel_type in
 	       select get_id('MREL'), conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type 
	       	      from construct_marker_relationship
		      where conmrkrrel_construct_zdb_id = constructZdbId 
	    loop
		insert into zdb_Active_data
	       	       values (mrel_id);

	insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type)
 	       values (mrel_id, mrel_1_id, mrel_2_id, mrel_type);

	insert into record_Attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id)
            select distinct mrel_id, recattrib_source_zdb_id
    	    	   from record_attribution
		   where recattrib_data_zdb_id = constructZdbId;
        end loop;

    elsif exists ( select 'x' from zdb_Active_data where zactvd_zdb_id = constructZdbId) 
    then 
   
       insert into marker (mrkr_zdb_id,
       	      	   	   mrkr_name,
			   mrkr_abbrev,
			   mrkr_owner,
			   mrkr_comments,
			   mrkr_type)
	select construct_zdb_id,
	       construct_name,
	       lower(construct_name),
	       construct_owner_zdb_id,
	       construct_comments,
	       construct_type
	  from construct
	  where construct_zdb_id = constructZdbId;


	for  mrel_id, mrel_1_id, mrel_2_id, mrel_type in
 	       select get_id('MREL'), conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type  
	       	      from construct_marker_relationship
		      where conmrkrrel_construct_zdb_id = constructZdbId 
	
	    loop

        insert into zdb_Active_data
	       values (mrel_id);

	insert into marker_relationship (mrel_zdb_id, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_type)
 	       values (mrel_id, mrel_1_id, mrel_2_id, mrel_type);

	insert into record_Attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id)
            select distinct mrel_id, recattrib_source_zdb_id
    	    	   from record_attribution
		   where recattrib_data_zdb_id = constructZdbId;
        end loop;
    else
    
      delete from construct
      	     where construct_zdb_id = constructZdbId;
    end if;

  end;

$$ LANGUAGE plpgsql;
