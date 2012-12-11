-- take feature_tracking feature_name update trigger off

create procedure p_update_related_names (vMarkerZdbId varchar(50), vOldMrkrAbbrev varchar(255), vNewMrkrAbbrev varchar(255))

	define vGenotypeZDB like genotype.geno_zdb_id;
	define vGenoDisplay like genotype.geno_display_name;
	define vGenoHandle like genotype.geno_handle;
	define vFeatureNameNew like feature.feature_name;
	define vFeatureNameOld like feature.feature_name;
	define vFeatureAbbrev like feature.feature_abbrev;
	
        if (get_obj_type(vMarkerZdbId) in ('GENE','TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT')) then

	
	 if (exists (select 'x' from feature_marker_relationship where fmrel_mrkr_zdb_id = vMarkerZdbId))
	 then

		foreach 
			select feature_abbrev, feature_name into vFeatureAbbrev, vFeatureNameOld
			       		     	     from feature, feature_marker_relationship
			       			     where feature_zdb_id = fmrel_ftr_zdb_id
			       			     and fmrel_mrkr_Zdb_id = vMarkerZdbId
			
			let vFeatureNameNew = replace(vFeatureNameOld,vOldMrkrAbbrev,vNewMrkrAbbrev); 
			
 --    			insert into record_old values (vMarkerZdbId, vFeatureNameOld, vFeatureNameNew);

			if ((vFeatureNameOld != vFeatureNameNew) and vFeatureAbbrev = vFeatureNameOld )
			then
				update feature
		    		 set (feature_name,feature_abbrev) = (replace(feature_name,vOldMrkrAbbrev,vNewMrkrAbbrev),
				     				      replace(feature_abbrev,vOldMrkrAbbrev,vNewMrkrAbbrev)
									
									)
		    		 where exists (select 'x' from feature_marker_relationship 
						          where fmrel_ftr_Zdb_id = feature_zdb_id
		    	         			  and fmrel_mrkr_Zdb_id = vMarkerZdbId
				 			  and feature_name = vFeatureNameOld);
			end if;

			if ((vFeatureNameOld != vFeatureNameNew) and vFeatureAbbrev != vFeatureNameOld)
			then
				update feature
		    		 set feature_name = replace(feature_name,vOldMrkrAbbrev,vNewMrkrAbbrev)
		    		 where exists (select 'x' from feature_marker_relationship 
						          where fmrel_ftr_Zdb_id = feature_zdb_id
		    	         			  and fmrel_mrkr_Zdb_id = vMarkerZdbId
				 			  and feature_name = vFeatureNameOld);	
			end if;  
	        end foreach; 
			   
         
	 foreach
			select distinct genofeat_geno_zdb_id
			  into vGenotypeZDB
			  from genotype_feature, feature_marker_Relationship
			 where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			 and fmrel_mrkr_Zdb_id = vMarkerZdbId
			 
			execute function get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			execute function get_genotype_handle(vGenotypeZDB) into vGenoHandle;		
	
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
        end foreach
	
	end if; -- end if exists...
       end if ; -- if mrkr_type in GENE, etc..


end procedure ;