--liquibase formatted sql
--changeset sierra:fix_activation.sql

update publication
 set status = 'active'
where exists (select 'x' from to_activate
                     where pub_pmc_id = pmcId)
and status != 'active';

insert into pub_tracking_history
  (pth_pub_zdb_id,
   pth_status_id,
   pth_status_set_by)
select zdb_id, 20, 'ZDB-PERS-170918-1'
  from publication, to_activate
  where accession_no = pmcId::integer
 and not exists (select 'x' from pub_tracking_history
                        where pth_pub_zdb_id = zdb_id
                        and pth_status_id != '1');
