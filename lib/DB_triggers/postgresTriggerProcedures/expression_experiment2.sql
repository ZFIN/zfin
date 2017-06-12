drop trigger if exists expression_experiment2_trigger on expression_experiment2;

create or replace function expression_experiment2()
returns trigger as
$BODY$

begin
     
     select p_insert_into_record_attribution_tablezdbids(NEW.xpatex_zdb_id 
    ,NEW.xpatex_source_zdb_id );

     select p_insert_into_record_attribution_datazdbids(NEW.xpatex_gene_zdb_id 
    ,NEW.xpatex_source_zdb_id );

     select check_xpat_null_valid(NEW.xpatex_gene_zdb_id, NEW.xpatex_probe_feature_zdb_id, NEW.xpatex_atb_zdb_id);

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger expression_experiment2_trigger before insert or update on expression_experiment2
 for each row
 execute procedure expression_experiment2();
