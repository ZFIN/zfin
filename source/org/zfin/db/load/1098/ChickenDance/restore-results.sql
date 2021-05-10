--liquibase formatted sql
--changeset sierra:restore-results.sql


delete from tmp_xpatres
 where xpatres_pk_id in (select xpatres_pk_id from expression_result2);

update tmp_xpatres
 set xpatres_subterm_zdb_id = null
 where xpatres_subterm_zdb_id = '';

delete from tmp_xpatres txpatres
 where exists (select 'x' from expression_result2 as er2
                      where er2.xpatres_efs_id = txpatres.xpatres_efs_id
                      and er2.xpatres_expression_found = txpatres.xpatres_expression_found
                      and er2.xpatres_superterm_zdb_id = txpatres.xpatres_superterm_zdb_id
                      and er2.xpatres_subterm_zdb_id = txpatres.xpatres_subterm_zdb_id
                      and er2.xpatres_subterm_zdb_id is not null
                      )
and txpatres.xpatres_subterm_zdb_id is not null;


delete from tmp_xpatres txpatres
 where exists (select 'x' from expression_result2 as er2
                      where er2.xpatres_efs_id = txpatres.xpatres_efs_id
                      and er2.xpatres_expression_found = txpatres.xpatres_expression_found
                      and er2.xpatres_superterm_zdb_id = txpatres.xpatres_superterm_zdb_id
                      and er2.xpatres_subterm_zdb_id = txpatres.xpatres_subterm_zdb_id
                      and er2.xpatres_subterm_zdb_id is null
                      )
and txpatres.xpatres_subterm_zdb_id is null;


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
