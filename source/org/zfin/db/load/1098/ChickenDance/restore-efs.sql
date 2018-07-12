--liquibase formatted sql
--changeset sierra:restore-efs


delete from tmp_efs
 where efs_pk_id in (select efs_pk_id from expression_figure_stage);

delete from tmp_efs as tefs
 where exists (select 'x' from expression_figure_stage as efs
                      where efs.efs_xpatex_zdb_id = tefs.efs_xpatex_zdb_id
                      and efs.efs_start_stg_zdb_id = tefs.efs_start_stg_zdb_id
                      and efs.efs_end_stg_zdb_id = tefs.efs_end_stg_zdb_id
                      and efs.efs_fig_zdb_id = tefs.efs_fig_zdb_id);


insert into expression_figure_stage (efs_pk_id,
       efs_xpatex_zdb_id,
       efs_fig_zdb_id,
       efs_start_stg_zdb_id,
       efs_end_stg_zdb_id)
 select efs_pk_id, efs_xpatex_zdb_id, efs_fig_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id
  from tmp_efs;
