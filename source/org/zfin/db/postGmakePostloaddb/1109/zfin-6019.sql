--liquibase formatted sql
--changeset xshao:ZFIN-6019

update image
   set img_fig_zdb_id = 'ZDB-FIG-190718-11'
 where img_zdb_id in ('ZDB-IMAGE-040212-6', 'ZDB-IMAGE-040212-7');

insert into expression_figure_stage (efs_xpatex_zdb_id, efs_fig_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id)
  values ('ZDB-XPAT-040212-1', 'ZDB-FIG-190718-11', 'ZDB-STAGE-010723-2', 'ZDB-STAGE-010723-2');

update image
   set img_fig_zdb_id = 'ZDB-FIG-190718-13'
 where img_zdb_id in ('ZDB-IMAGE-040212-8', 'ZDB-IMAGE-040212-9');

insert into expression_figure_stage (efs_xpatex_zdb_id, efs_fig_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id)
  values ('ZDB-XPAT-040212-1', 'ZDB-FIG-190718-13', 'ZDB-STAGE-010723-2', 'ZDB-STAGE-010723-2');

delete from phenotype_statement
 where phenos_pk_id = '8079';

delete from phenotype_experiment
 where phenox_fig_zdb_id = 'ZDB-FIG-070117-592';

delete from record_attribution
 where recattrib_data_zdb_id = 'ZDB-FIG-070117-592';

delete from figure
 where fig_zdb_id = 'ZDB-FIG-070117-592';

delete from zdb_active_data
 where zactvd_zdb_id = 'ZDB-FIG-070117-592';

