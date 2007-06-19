create procedure p_update_related_genotype_names (vZDBid varchar(50))

	define vDataType varchar(30) ;
	define vGenotypeFtrCount integer ;	
	define vFmrelCount integer ;
	define vRelatedFtr varchar(50);
	define vGenotypeZDB like genotype.geno_zdb_id;
	define vGenoDisplay like genotype.geno_display_name;
	define vGenoHandle like genotype.geno_handle;
		

	if get_obj_type(vZDBid) = 'ALT'
	then 
  	  let vDataType = 'feature' ;

	elif get_obj_type(vZDBid) != 'ALT'
        then
	  let vDataType = 'marker' ;

	end if ; -- end if get_obj_type = 'ALT'

	if vDataType = "feature"
	then


	  let vGenotypeFtrCount = (Select count(*) 
			  	     from genotype_feature
			  	     where genofeat_feature_zdb_id = vZDBid);


	  if vGenotypeFtrCount > 0
	  then 

		foreach
			select distinct genofeat_geno_zdb_id
			  into vGenotypeZDB
			  from genotype_feature
			 where genofeat_feature_zdb_id = vZDBid
			 
			execute function get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			execute function get_genotype_handle(vGenotypeZDB) into vGenoHandle;
			execute function regen_names_genotype(vGenotypeZDB);					
	
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
		end foreach

	  end if ; -- end if vGenotypeFtrCount > 0

 	elif vDataType = 'marker'
	then
	  let vFmrelCount = (select count(*)
                                from feature_marker_relationship, 
                                     genotype_feature
                                where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
				and fmrel_mrkr_zdb_id = vZDBid
                               ); 

	  if vFmrelCount > 0  

	  then 
		foreach
			select distinct genofeat_geno_zdb_id
			  into vGenotypeZDB
			  from genotype_feature, feature_marker_relationship
			 where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			   and fmrel_mrkr_zdb_id = vZDBid
			 
			execute function get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			execute function get_genotype_handle(vGenotypeZDB) into vGenoHandle;
			execute function regen_names_genotype(vGenotypeZDB);	
		
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
		end foreach
		   
	  end if ;

	end if;  -- end if vDataType = 'marker'

end procedure ;