drop trigger if exists inference_group_member_trigger on inference_group_member;

create or replace function inference_group_member()
returns trigger as
$BODY$
declare infgrmem_inferred_from inference_group_member.infgrmem_inferred_from%TYPE := scrub_char(NEW.infgrmem_inferred_from);
declare infgrmem_notes inference_group_member.infgrmem_notes%TYPE :=  ;

begin
     
 
     NEW.infgrmem_inferred_from = infgrmem_inferred_from;

     NEW.infgrmem_notes = infgrmem_notes;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger inference_group_member_trigger before insert or update on inference_group_member
 for each row
 execute procedure inference_group_member();
