--liquibase formatted sql
--changeset rtaylor:ZFIN-8188

select zdb_id from publication where pub_abstract ilike '%</div>' and pub_abstract not ilike '%<div%';

update publication
set pub_abstract = left(pub_abstract, length(pub_abstract) - 6)
where pub_abstract ilike '%</div>'
  and pub_abstract not ilike '%<div%';
