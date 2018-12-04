--liquibase formatted sql
--changeset xshao:DLOAD-510

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select gene_id, pub_id 
    from gene_attri
   where not exists( select 'x' from record_attribution
                      where gene_id = recattrib_data_zdb_id
                        and pub_id = recattrib_source_zdb_id);
drop table gene_attri;
