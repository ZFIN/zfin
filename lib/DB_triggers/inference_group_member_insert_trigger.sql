create trigger inference_group_member_insert_trigger 
  insert on inference_group_member referencing 
  new as new_igm
  for each row (execute function scrub_char(new_igm.infgrmem_inferred_from)
                  into inference_group_member.infgrmem_inferred_from,
		execute function scrub_char(new_igm.infgrmem_notes)
		  into inference_group_member.infgrmem_notes) ;