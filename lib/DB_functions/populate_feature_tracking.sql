create or replace function populate_feature_tracking (featureAbbrev varchar(70),
       		 			    featureName varchar(255),
					    featureZdbId text)

returns void as $$
   declare vOk int := 0;

   begin

    vOk = (select count(*) from feature_tracking where ft_Feature_abbrev = featureAbbrev and featureZdbId != ft_feature_zdb_id);

   raise notice 'feature tracking vOk %', vOk;
  

   if (vOk > 0)

   then 
   	raise exception 'FAIL!: this line designation has already been used.';
   else
	raise notice 'feature abbrev: %', featureAbbrev;
	insert into feature_Tracking (ft_feature_abbrev, ft_feature_name,ft_feature_zdb_id)
    	values (featureAbbrev,featureName,featureZdbId);
   end if;

end

$$ LANGUAGE plpgsql
