create trigger inference_group_member_inferred_from_update_trigger 
  update of infgrmem_inferred_from on inference_group_member
  referencing old as oldinf  new as newinf
  for each row
	(execute function scrub_char(newinf.infgrmem_inferred_from)
         into inference_group_member.infgrmem_inferred_from) ;