--liquibase formatted sql
--changeset sierra:restore-results.sql


delete from tmp_xpatres
 where xpatres_pk_id in (select xpatres_pk_id from expression_result2);

update tmp_xpatres
 set xpatres_subterm_zdb_id = null
 where xpatres_subterm_zdb_id = '';


insert into expression_result2 (xpatres_pk_id, 
                               xpatres_efs_id,
                               xpatres_expression_found,
                               xpatres_superterm_zdb_id,
                               xpatres_subterm_zdb_id)
 select xpatres_pk_id,         
        xpatres_efs_id,
        xpatres_expression_found,
        xpatres_superterm_zdb_id,
        xpatres_subterm_zdb_id
  from tmp_xpatres ;
