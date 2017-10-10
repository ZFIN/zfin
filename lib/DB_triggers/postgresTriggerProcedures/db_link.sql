drop trigger if exists db_link_trigger on db_link;

create or replace function db_link()
returns trigger as
$BODY$

declare dblink_acc_num db_link.dblink_acc_num%TYPE := scrub_char(NEW.dblink_acc_num);

declare dblink_acc_num_display db_link.dblink_acc_num_display%TYPE := get_dblink_acc_num_display(NEW.dblink_fdbcont_zdb_id, 
			       					      				 NEW.dblink_acc_num);


begin

     NEW.dblink_acc_num = dblink_acc_num;
     NEW.dblink_acc_num_display = dblink_acc_num_display;

     perform p_dblink_has_parent(NEW.dblink_linked_recid) ;

     perform p_check_caps_acc_num(NEW.dblink_fdbcont_zdb_id, NEW.dblink_acc_num);

     perform checkDblinkTranscriptWithdrawn(NEW.dblink_zdb_id,
					    NEW.dblink_linked_recid,
					    NEW.dblink_fdbcont_zdb_id);
 --TODO: return into two variables
-- get_genbank_dblink_length_type 
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger db_link_trigger after insert or update on db_link
 for each row
 execute procedure db_link();
