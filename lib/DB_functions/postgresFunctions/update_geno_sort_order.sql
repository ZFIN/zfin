create or replace function update_geno_sort_order (genoZdbId text)
  returns text as $complexitySortValue$ 
  
  declare featureId  feature.feature_zdb_id%TYPE; 
   	  complexitySortValue  feature_type_ordering.fto_priority%TYPE;
   	  priority  feature_type_ordering.fto_priority%TYPE;
   	  featureType     feature_type.ftrtype_type_display%TYPE; 
   	  alleleCount int;
  begin 
  complexitySortValue = '999999999';

   	
	   for featureId, featureType in
		select feature_zdb_id, ftrtype_type_display
		from genotype_Feature, feature, feature_type
		where genofeat_geno_zdb_id = genoZdbId
		and feature_type = ftrtype_name
		and genofeat_feature_Zdb_id = feature_zdb_id
	   loop

		if featureType = 'Transgenic Insertion' then
			select count(*) into alleleCount from feature_marker_relationship 
			where fmrel_ftr_zdb_id = featureId 
			and fmrel_type = 'is allele of';
			
			if alleleCount < 1 then
				featureType := 'Transgenic Insertion, non-allelic';
			end if;
		end if;

		select fto_priority  into priority 
		from feature_type_ordering
		where fto_name = featureType;

		if priority is null then
		   raise exception 'FAIL!: priority does not have a value';
		   end if ;
			
		complexitySortValue = complexitySortValue + priority;


	   end loop;

  if (complexitySortValue is null)

     then complexitySortValue = '999999999';

  end if;

  return complexitySortValue;
  end 

$complexitySortValue$ LANGUAGE plpgsql;
