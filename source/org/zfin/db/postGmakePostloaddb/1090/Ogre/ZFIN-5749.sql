--liquibase formatted sql
--changeset prita:ZFIN-5749



update  expression_result2 set xpatres_subterm_zdb_id='ZDB-TERM-100722-72' where xpatres_subterm_zdb_id='ZDB-TERM-100722-103';
update  apato_infrastructure set api_entity_2_subterm_Zdb_id='ZDB-TERM-100722-72' where api_entity_2_subterm_zdb_id='ZDB-TERM-100722-103';
update  apato_infrastructure set api_entity_1_subterm_Zdb_id='ZDB-TERM-100722-72' where api_entity_1_subterm_zdb_id='ZDB-TERM-100722-103';
update  expression_pattern_infrastructure set xpatinf_subterm_Zdb_id='ZDB-TERM-100722-72' where xpatinf_subterm_Zdb_id='ZDB-TERM-100722-103';
update  phenotype_statement set phenos_entity_1_subterm_zdb_id='ZDB-TERM-100722-72' where phenos_entity_1_subterm_zdb_id='ZDB-TERM-100722-103' ;
update  phenotype_statement set phenos_entity_2_subterm_zdb_id='ZDB-TERM-100722-72' where phenos_entity_2_subterm_zdb_id='ZDB-TERM-100722-103' ;



