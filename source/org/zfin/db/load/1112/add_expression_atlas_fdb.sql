--liquiabse formatted sql
--changeset sierra:add_expression_atlas_fdb.sql

insert into foreign_db (fdb_db_name,    
                        fdb_db_query,
                        fdb_url_suffix,
                        fdb_db_display_name,
                        fdb_db_significance)
values ('ExpressionAtlas',
       'http://www.ebi.ac.uk/gxa/genes/',
       '',
       'ExpressionAtlas',
       '2');

create temp table tmp_id (id text);
insert into tmp_id 
  select get_id('FDBCONT') from single;

insert into foreign_db_contains (fdbcont_zdb_id, 
                                 fdbcont_organism_common_name,
                                 fdbcont_fdbdt_id, 
                                 fdbcont_fdb_db_id
       )
select id, 'Zebrafish',13,(select fdb_db_pk_id from foreign_db where fdb_db_name = 'ExpressionAtlas') 
  from tmp_id;

