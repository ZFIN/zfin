drop trigger if exists genotype_feature_trigger on genotype_feature;

create or replace function genotype_feature()
returns trigger as
$BODY$
declare 
  genoDisplayName genotype.geno_display_name%TYPE;


begin
    
    select scrub_char(get_genotype_display(NEW.genofeat_geno_zdb_id)) into genoDisplayName;
    raise notice 'genoDisplayName %', genoDisplayName;

    update genotype
      set geno_display_name = genoDisplayName
     where geno_zdb_id = NEW.genofeat_geno_zdb_id;

    raise notice 'displayName: %', genoDisplayName;

    perform p_check_tginsertion_has_construct_relationship(NEW.genofeat_feature_zdb_id);

    RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_feature_trigger after insert or update on genotype_feature
 for each row
 execute procedure genotype_feature();
