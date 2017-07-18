drop trigger if exists one_to_one_accession_trigger on one_to_one_accession;

create or replace function one_to_one_accession()
returns trigger as
$BODY$
begin

     perform checkDblinkOneToOneAccessionMapping (NEW.ooa_dblink_zdb_id, NEW.ooa_feature_zdb_id, NEW.ooa_dblink_acc_num);
     RETURN NULL;

end;
$BODY$ LANGUAGE plpgsql;

create trigger one_to_one_accession_trigger before insert or update on one_to_one_accession
 for each row
 execute procedure one_to_one_accession();
