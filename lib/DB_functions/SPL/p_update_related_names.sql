create procedure p_update_related_names (vMarkerZdbId varchar(50), vOldMrkrAbbrev varchar(70), vNewMrkrAbbrev varchar(70))

	define vGenotypeZDB like genotype.geno_zdb_id;
	define vGenoDisplay like genotype.geno_display_name;
	define vGenoHandle like genotype.geno_handle;
		
        if (get_obj_type(vMarkerZdbId) in ('GENE','TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT')) then

	 if (exists (select 'x' from feature_marker_relationship where fmrel_mrkr_zdb_id = vMarkerZdbId))
	 then
		update feature
		    set (feature_name, feature_abbrev) = (replace(feature_name,vOldMrkrAbbrev,vNewMrkrAbbrev),replace(feature_abbrev,vOldMrkrAbbrev,vNewMrkrAbbrev))
		    where exists (select 'x' from feature_marker_relationship where fmrel_ftr_Zdb_id = feature_zdb_id
		    	  	 	     and fmrel_mrkr_Zdb_id = vMarkerZdbId);
			   
			   
         
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