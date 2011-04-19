create trigger phenotype_statement_update_trigger update 
    of phenos_entity_1_subterm_zdb_id,phenos_entity_1_superterm_zdb_id, phenos_entity_2_subterm_zdb_id, phenos_entity_2_superterm_zdb_id,phenos_quality_zdb_id 
    on phenotype_statement referencing new as new_phenos
    
    for each row
        (
         
        execute procedure p_term_is_not_obsolete_or_secondary(new_phenos.phenos_quality_zdb_id 
    ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_phenos.phenos_entity_1_superterm_zdb_id 
    ),
        execute procedure p_term_is_not_obsolete_or_secondary(new_phenos.phenos_entity_1_subterm_zdb_id 
    ),
	execute procedure p_term_is_not_obsolete_or_secondary(new_phenos.phenos_entity_2_superterm_zdb_id 
    ),
	execute procedure p_term_is_not_obsolete_or_secondary(new_phenos.phenos_entity_2_subterm_zdb_id 
    )
)
;