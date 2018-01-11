drop trigger if exists genotype_feature_trigger on genotype_feature;

create or replace function genotype_feature()
returns trigger as
$BODY$

begin

     perform  p_check_tginsertion_has_construct_relationship(
			NEW.genofeat_feature_zdb_id);
     perform p_update_related_genotype_names(NEW.genofeat_feature_zdb_id);
     
     RETURN NULL;

end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_feature_trigger after insert or update on genotype_feature
 for each row
 execute procedure genotype_feature();
