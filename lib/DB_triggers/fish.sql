DROP TRIGGER IF EXISTS fish_trigger
ON fish;
DROP TRIGGER IF EXISTS fish_affected_trigger
ON fish;
drop trigger if exists fish_after
on fish;
DROP TRIGGER IF EXISTS fish_pheno_count_trigger
on FISH;


CREATE OR REPLACE FUNCTION fish_after()
  RETURNS trigger AS $BODY$

  BEGIN                                        
    update fish                                    
      set fish_name = get_fish_full_name(NEW.fish_Zdb_id,NEW.fish_genotype_zdb_id, get_fish_name(NEW.fish_zdb_id))
                      where fish_zdb_id = NEW.fish_zdb_id;          
    update fish 
      set fish_phenotypic_construct_count = (Select count(fmrel_ftr_zdb_id) from genotype_feature, feature_marker_relationship
     						 where genofeat_geno_zdb_id = NEW.fish_genotype_zdb_id
      						  and fmrel_type = 'contains phenotypic sequence feature'
      						  and fmrel_ftr_zdb_id = genofeat_feature_zdb_id)
      where exists (select 'x' from genotype_feature, feature_marker_relationship
                        	where genofeat_geno_zdb_id = NEW.fish_genotype_zdb_id
                        	 and fmrel_type = 'contains phenotypic sequence feature'
                        	 and fmrel_ftr_zdb_id = genofeat_feature_zdb_id);
                                                                    
    return new;                                    
   END;     

$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fish_trigger()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.fish_name_order = zero_pad(NEW.fish_name); 
  NEW.fish_full_name = NEW.fish_name; 
   RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fish_pheno_count()
  RETURNS trigger as $BODY$ 

  BEGIN

  Select count(fmrel_ftr_zdb_id) from
                        genotype_feature, feature_marker_relationship
                        where genofeat_geno_zdb_id = NEW.fish_genotype_zdb_id
                         and fmrel_type = 'contains phenotypic sequence feature'
                         and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
                         and exists (select 'x' from genotype_feature, feature_marker_relationship
                        		where genofeat_geno_zdb_id = NEW.fish_genotype_zdb_id
                        		and fmrel_type = 'contains phenotypic sequence feature'
                        		and fmrel_ftr_zdb_id = genofeat_feature_zdb_id) 
      into NEW.fish_phenotypic_construct_count;

RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fish_affected()
  RETURNS trigger AS $BODY$

  BEGIN
    SELECT * FROM getFishOrder(NEW.fish_zdb_id,NEW.fish_genotype_zdb_id)
       INTO NEW.fish_order,NEW.fish_functional_affected_gene_count;
       RETURN NEW;
  END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER fish_trigger
  BEFORE  INSERT OR UPDATE  ON fish
  FOR EACH ROW EXECUTE PROCEDURE fish_trigger();

CREATE TRIGGER fish_affected_trigger
  BEFORE  INSERT OR UPDATE  ON fish
  FOR EACH ROW EXECUTE PROCEDURE fish_affected();

CREATE TRIGGER fish_pheno_count_trigger
  BEFORE INSERT OR UPDATE ON fish
  FOR EACH ROW EXECUTE PROCEDURE fish_pheno_count();

CREATE TRIGGER fish_after
  AFTER INSERT on fish
  FOR EACH ROW EXECUTE PROCEDURE fish_after();

