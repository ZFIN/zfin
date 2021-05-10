--liquibase formatted sql
--changeset pkalita:CUR-735-b

-- update curation_topic
--   set curtopic_name = 'Zebrafish Sequence Variant'
--   where curtopic_name = 'Mutant sequence without accession';
--
-- update curation_topic
--   set curtopic_name = 'Human Sequence Variant'
--   where curtopic_name = 'Sequence Variant';

insert into curation_topic (curtopic_name)
  values ('Zebrafish Sequence Variant');

update curation
  set cur_topic = 'Zebrafish Sequence Variant'
  where cur_topic = 'Mutant sequence without accession';

delete from curation_topic
  where curtopic_name = 'Mutant sequence without accession';



insert into curation_topic (curtopic_name)
  values ('Human Sequence Variant');

update curation
  set cur_topic = 'Human Sequence Variant'
  where cur_topic = 'Sequence Variant';

delete from curation_topic
  where curtopic_name = 'Sequence Variant';
