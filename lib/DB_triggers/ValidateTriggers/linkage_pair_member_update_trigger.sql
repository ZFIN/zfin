--see linkage_pair_member_update_trigger
 
  create trigger linkage_pair_member_update_trigger 
   update of lpmem_linkage_pair_zdb_id
   on linkage_pair_member
   referencing new as new_linkage_pair_member
   for each row
	(execute procedure p_2_linkage_pairs
	(new_linkage_pair_member.lpmem_linkage_pair_zdb_id));
