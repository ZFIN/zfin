drop trigger if exists marker_relationship_trigger on marker_relationship;

create or replace function marker_relationship()
returns trigger as
$BODY$

begin
     perform p_mrel_grpmem_correct (
           NEW.mrel_mrkr_1_zdb_id, 
           NEW.mrel_mrkr_2_zdb_id, 
           NEW.mrel_type
         );
     perform checkTscriptType (NEW.mrel_mrkr_1_zdb_id, 
                              NEW.mrel_mrkr_2_zdb_id,
                              NEW.mrel_type);
     
     RETURN null;

end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_relationship_trigger before insert or update on marker_relationship
 for each row
 execute procedure marker_relationship();
