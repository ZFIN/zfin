create trigger expression_experiment2_insert_trigger
insert on expression_experiment2 referencing new 
    as new_xpatex
    for each row(
    	execute procedure p_insert_into_record_attribution_tablezdbids(new_xpatex.xpatex_zdb_id 
    ,new_xpatex.xpatex_source_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_xpatex.xpatex_gene_zdb_id 
    ,new_xpatex.xpatex_source_zdb_id ),
        execute procedure check_xpat_null_valid(new_xpatex.xpatex_gene_zdb_id, xpatex_probe_feature_zdb_id, xpatex_atb_zdb_id));
