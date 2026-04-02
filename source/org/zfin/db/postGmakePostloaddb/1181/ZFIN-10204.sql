--liquibase formatted sql
--changeset rtaylor:ZFIN-10204

-- Replace obsolete GO:0005615 (extracellular space) with GO:0005576 (extracellular region)
-- Old term: ZDB-TERM-091209-4069 (GO:0005615, obsolete extracellular space)
-- New term: ZDB-TERM-091209-4030 (GO:0005576, extracellular region)

update expression_result2
   set xpatres_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where xpatres_subterm_zdb_id = 'ZDB-TERM-091209-4069';

update expression_result2
   set xpatres_superterm_zdb_id = 'ZDB-TERM-091209-4030'
 where xpatres_superterm_zdb_id = 'ZDB-TERM-091209-4069';

update expression_pattern_infrastructure
   set xpatinf_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where xpatinf_subterm_zdb_id = 'ZDB-TERM-091209-4069';

update expression_pattern_infrastructure
   set xpatinf_superterm_zdb_id = 'ZDB-TERM-091209-4030'
 where xpatinf_superterm_zdb_id = 'ZDB-TERM-091209-4069';

update phenotype_statement
   set phenos_entity_1_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where phenos_entity_1_subterm_zdb_id = 'ZDB-TERM-091209-4069';

update phenotype_statement
   set phenos_entity_2_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where phenos_entity_2_subterm_zdb_id = 'ZDB-TERM-091209-4069';

update phenotype_statement
   set phenos_entity_1_superterm_zdb_id = 'ZDB-TERM-091209-4030'
 where phenos_entity_1_superterm_zdb_id = 'ZDB-TERM-091209-4069';

update phenotype_statement
   set phenos_entity_2_superterm_zdb_id = 'ZDB-TERM-091209-4030'
 where phenos_entity_2_superterm_zdb_id = 'ZDB-TERM-091209-4069';

update apato_infrastructure
   set api_entity_1_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where api_entity_1_subterm_zdb_id = 'ZDB-TERM-091209-4069';

update apato_infrastructure
   set api_entity_2_subterm_zdb_id = 'ZDB-TERM-091209-4030'
 where api_entity_2_subterm_zdb_id = 'ZDB-TERM-091209-4069';
