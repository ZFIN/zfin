--linkage_pair_member_insert_trigger
-------------------------------------------------------------
--trigger that checks the insert or update of linkage_pair table to make 
--sure it does not have more than 2 linkage pair members. 
--REPLACES:
--sub linkagePairHas2Members 

   create trigger linkage_pair_member_insert_trigger insert on linkage_pair_member
   referencing new as new_linkage_pair_member
   for each row 
	(execute procedure p_2_linkage_pairs
	(new_linkage_pair_member.lpmem_linkage_pair_zdb_id));
