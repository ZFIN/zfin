create or replace function p_update_related_names (vMarkerZdbId varchar(50), vOldMrkrAbbrev varchar(255), vNewMrkrAbbrev varchar(255))
returns void as $$
	declare vGenotypeZDB genotype.geno_zdb_id%TYPE;
	 vGenoDisplay genotype.geno_display_name%TYPE;
	 vGenoHandle genotype.geno_handle%TYPE;
	 vConstructNameNew construct.construct_name%TYPE;
	 vConstructNameOld construct.construct_name%TYPE;

begin
	
        if (get_obj_type(vMarkerZdbId) in ('GENE')) then

	 if (exists (select 'x' from feature_marker_relationship where fmrel_mrkr_zdb_id = vMarkerZdbId))
	 then
		for vGenotypeZDB in
			select distinct genofeat_geno_zdb_id
			  from genotype_feature, feature_marker_Relationship
			 where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			 and fmrel_mrkr_Zdb_id = vMarkerZdbId
		   loop			 
			select get_genotype_display(vGenotypeZDB) into vGenoDisplay;
			select get_genotype_handle(vGenotypeZDB) into vGenoHandle;
	
			update genotype
			   set geno_display_name = vGenoDisplay,
			       geno_handle = vGenoHandle
			 where geno_zdb_id = vGenotypeZDB;
				 
        	end loop;
	
	end if; -- end if exists...
       end if ; -- if mrkr_type in GENE, etc..
end

$$ LANGUAGE plpgsql;
