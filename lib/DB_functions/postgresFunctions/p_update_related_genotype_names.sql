create or replace function  p_update_related_genotype_names (vZDBid text)
returns void as $$
declare vDataType varchar(30) ;
	 vGenotypeFtrCount integer ;	
	 vFmrelCount integer ;
	 vRelatedFtr text;
	 vGenotypeZDB  genotype.geno_zdb_id%TYPE;
	 vGenoDisplay  genotype.geno_display_name%TYPE;
	 vGenoHandle  genotype.geno_handle%TYPE;
		
begin 
	if get_obj_type(vZDBid) = 'ALT'
	then 
  	   vDataType = 'feature' ;

	elsif get_obj_type(vZDBid) != 'ALT'
        then
	   vDataType = 'marker' ;

	end if ; -- end if get_obj_type = 'ALT'

	if vDataType = 'feature'
	then


	   vGenotypeFtrCount = (Select count(*) 
			  	     from genotype_feature
			  	     where genofeat_feature_zdb_id = vZDBid);


	  if vGenotypeFtrCount > 0
	  then 

		for vGenotypeZDB in
			select distinct genofeat_geno_zdb_id
			  into vGenotypeZDB
			  from genotype_feature
			 where genofeat_feature_zdb_id = vZDBid
		loop
			select get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			select get_genotype_handle(vGenotypeZDB) into vGenoHandle;		
	
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
		end loop;

	  end if ; -- end if vGenotypeFtrCount > 0

 	elsif vDataType = 'marker'
	then
	   vFmrelCount = (select count(*)
                                from feature_marker_relationship, 
                                     genotype_feature
                                where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
				and fmrel_mrkr_zdb_id = vZDBid
                               ); 

	  if vFmrelCount > 0  

	  then 
		for vGenotypeZDB in
			select distinct genofeat_geno_zdb_id
			  from genotype_feature, feature_marker_relationship
			 where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			   and fmrel_mrkr_zdb_id = vZDBid
		loop	 
			 select get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			 select get_genotype_handle(vGenotypeZDB) into vGenoHandle;
		
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
		end loop;
		   
	  end if ;

	end if;  -- end if vDataType = 'marker'
end
$$ LANGUAGE plpgsql
