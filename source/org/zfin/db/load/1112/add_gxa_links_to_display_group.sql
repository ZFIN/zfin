--liquibase formatted sql
--changeset sierra:add_gxa_links_to_display_groups.sql

insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id,
       fdbcdgm_group_id)
  select fdbcont_zdb_id, '8' 
    from foreign_db_contains, foreign_db
    where fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = 'ExpressionAtlas';
