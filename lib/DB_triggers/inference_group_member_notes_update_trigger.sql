create trigger inference_group_member_notes_update_trigger 
  update of infgrmem_notes on inference_group_member
  referencing old as oldinf  new as newinf
  for each row
	(execute function scrub_char(newinf.infgrmem_notes)
         into inference_group_member.infgrmem_notes) ;