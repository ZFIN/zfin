create procedure populate_feature_tracking (featureAbbrev varchar(70),
       		 			    featureName varchar(255),
					    featureZdbId varchar(50))


   define vOk int;
   let vOk = 0;

   let vOk = (select count(*) from feature_tracking where ft_Feature_abbrev = featureAbbrev);

   if (vOk > 0)

   then 
   	raise exception -746,0,"FAIL!: this line designation has already been used.";
   else

	insert into feature_Tracking (ft_feature_abbrev, ft_feature_name,ft_feature_zdb_id)
    	values (featureAbbrev,featureName,featureZdbId);
   end if;

end procedure;