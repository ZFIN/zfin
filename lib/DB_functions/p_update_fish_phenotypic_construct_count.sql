create or replace function p_update_fish_phenotypic_construct_count (vFeatureZdbId text, vFmrelType text)
returns void as $$

declare genotypeZdbId text;
declare counter int;

begin

if (vFmrelType = 'contains phenotypic sequence feature')
 then 
    for genotypeZdbId in 
        select distinct genofeat_geno_zdb_id
	  from genotype_feature, feature_marker_relationship
  	  where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
	  and fmrel_ftr_zdb_id = vFeatureZdbId
	 loop
	raise notice 'genoId: %', genotypeZdbId;
	 select count(distinct fmrel_ftr_zdb_id) into counter
           from feature_marker_relationship, genotype_feature
	   where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
	   and genofeat_geno_zdb_id = genotypeZdbId;
	raise notice 'counter: %', counter;
 	 update fish
		set fish_phenotypic_construct_count = counter
		where fish_genotype_zdb_id = genotypeZdbId;
    end loop;

end if;
end

$$ LANGUAGE plpgsql;
