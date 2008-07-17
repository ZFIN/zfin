create procedure p_update_clone_relationship (clone_mrkr_zdb_id varchar(50),
       		 clone_problem_type varchar(60))

if clone_problem_type is not null
then
       update marker_relationship 
         set (mrel_type, mrel_comments) = ('gene has artifact', null)
         where mrel_mrkr_2_zdb_id = clone_mrkr_zdb_id 
         and mrel_type in ('gene hybridized by small segment', 'gene encodes small segment');


end if ;

end procedure;