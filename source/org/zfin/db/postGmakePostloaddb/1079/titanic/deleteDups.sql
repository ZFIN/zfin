--liquibase formatted sql
--changeset sierra:deleteDups

delete from experiment_condition
 where expcond_zdb_id in ('ZDB-EXPCOND-160729-5',
'ZDB-EXPCOND-160729-6',
'ZDB-EXPCOND-160729-4',
'ZDB-EXPCOND-160729-28',
'ZDB-EXPCOND-160729-44',
'ZDB-EXPCOND-160729-43',
'ZDB-EXPCOND-160729-45',
'ZDB-EXPCOND-160729-12',
'ZDB-EXPCOND-160729-13');
