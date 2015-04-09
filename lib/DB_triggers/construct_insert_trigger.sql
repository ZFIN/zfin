create trigger construct_insert_trigger insert on 
 construct referencing new as new_Construct
 for each row (
        execute function scrub_char(new_construct.construct_name 
    ) into construct.construct_name
--,
--      	   execute function update_date_modified (new_construct.construct_zdb_id)
--      into construct.construct_date_modified;
);