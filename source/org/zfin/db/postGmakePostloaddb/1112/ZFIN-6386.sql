--liquibase formatted sql
--changeset pm:ZFIN-6386


insert into expression_result2 (xpatres_efs_id,xpatres_expression_found, xpatres_superterm_zdb_id) values(84425,'t', 'ZDB-TERM-100331-2193');
insert into expression_result2 (xpatres_efs_id,xpatres_expression_found, xpatres_superterm_zdb_id) values(84425,'t', 'ZDB-TERM-100331-2134');
delete from expression_result2 where xpatres_efs_id=84425 and xpatres_superterm_zdb_id='ZDB-TERM-100331-10';


insert into expression_result2 (xpatres_efs_id,xpatres_expression_found, xpatres_superterm_zdb_id) values(84422,'t', 'ZDB-TERM-100331-2193');
insert into expression_result2 (xpatres_efs_id,xpatres_expression_found, xpatres_superterm_zdb_id) values(84422,'t', 'ZDB-TERM-100331-2134');


