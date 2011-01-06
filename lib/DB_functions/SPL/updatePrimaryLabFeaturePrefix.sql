create procedure updatePrimaryLabFeaturePrefix (vLabZdbId varchar(50), vCurrentDesignation boolean, vPrefix int8)

if (vCurrentDesignation = 't') 
then
       update lab_feature_prefix
         set lfp_current_designation = 'f'
	 where  lfp_lab_zdb_id = vLabZdbId
	 and lfp_prefix_id != vPrefix;

end if;
end procedure;