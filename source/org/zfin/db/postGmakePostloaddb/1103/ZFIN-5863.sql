--liquibase formatted sql
--changeset xshao:ZFIN-5863

update expression_result2
   set xpatres_superterm_zdb_id = 'ZDB-TERM-100331-1056'
 where xpatres_superterm_zdb_id = 'ZDB-TERM-100331-35'
   and exists(select 1 
                from expression_figure_stage, expression_experiment2
               where xpatres_efs_id = efs_pk_id 
                 and efs_xpatex_zdb_id = xpatex_zdb_id 
                 and xpatex_assay_name in ('Reverse transcription PCR', 'Western blot', 'Northern blot'));
                 
update expression_result
   set xpatres_superterm_zdb_id = 'ZDB-TERM-100331-1056'
 where xpatres_superterm_zdb_id = 'ZDB-TERM-100331-35'
   and exists(select 1 
                from expression_experiment
               where xpatres_xpatex_zdb_id = xpatex_zdb_id  
                 and xpatex_assay_name in ('Reverse transcription PCR', 'Western blot', 'Northern blot'));    
