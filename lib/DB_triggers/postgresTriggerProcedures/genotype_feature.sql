drop trigger if exists genotype_feature_trigger on genotype_feature;

create or replace function genotype_feature()
returns trigger as
$BODY$

begin

     select  p_check_tginsertion_has_construct_relationship(
			NEW.genofeat_feature_zdb_id);
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger genotype_feature_trigger before insert or update on genotype_feature
 for each row
 execute procedure genotype_feature();
