--liquibase formatted sql
--changeset xshao:PUB-527

create temp table tmp_pub_527 (pubid text);

insert into tmp_pub_527
select distinct zdb_id
  from publication
 where exists(select 1 from pub_tracking_history
               where pth_pub_zdb_id = zdb_id
                 and pth_status_id = 1)
   and jtype='Journal'
   and pub_arrival_date between '2010-01-01' and '2016-12-31'
   and exists(select 1 from record_attribution 
               where zdb_id = recattrib_source_zdb_id)
   and not exists(select 1 from record_attribution 
                   where zdb_id = recattrib_source_zdb_id 
                     and recattrib_data_zdb_id like 'ZDB-XPAT%')
   and not exists(select 1 from pheno_term
                   where zdb_id = pt_pub_zdb_id)
   and not exists(select 1 from publication_file 
                   where zdb_id = pf_pub_zdb_id);

insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by, pth_claimed_by)
  select pubid, 6, 'ZDB-PERS-050706-1', 'ZDB-PERS-030612-1'
    from tmp_pub_527;

drop table tmp_pub_527;
