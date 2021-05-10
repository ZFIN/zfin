--liquibase formatted sql
--changeset sierra:update_uberon_map.sql

update zfa_uberon_mapping
  set zum_uberon_id = 'UBERON:0005409'
 where zum_uberon_id = 'UBERON:0001007';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001009'
where zum_zfa_id = 'ZFA:0000010';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0005409'
where zum_zfa_id in ( 'ZFA:0009228',
      'ZFA:0009217','ZFA:0005162',
     'ZFA:0000112','ZFA:0005167','ZFA:0000695','ZFA:0000547','ZFA:0001371','ZFA:0000056','ZFA:0005346',
     'ZFA:0005167','ZFA:0001100','ZFA:0000233','ZFA:0001027','ZFA:0005463','ZFA:0000816','ZFA:0001272','ZFA:0001290',
     'ZFA:0000451','ZFA:0001273');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001008'
where zum_zfa_id = 'ZFA:0000163';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000949'
where zum_zfa_id = 'ZFA:0001158';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002330'
where zum_zfa_id = 'ZFA:0001249';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002193'
where zum_zfa_id in ('ZFA:0005023','ZFA:0000385','ZFA:0005830','ZFA:0009250');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002416'
where zum_zfa_id = 'ZFA:0000368';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002204'
where zum_zfa_id in ('ZFA:0000548','ZFA:0000434');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002423'
where zum_zfa_id = 'ZFA:0000036';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001016'
where zum_zfa_id = 'ZFA:0000396';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000990'
where zum_zfa_id = 'ZFA:0000632';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001004'
where zum_zfa_id = 'ZFA:0000272';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001032'
where zum_zfa_id = 'ZFA:0000282';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002105'
where zum_zfa_id = 'ZFA:0001138';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0005726'
where zum_zfa_id in ('ZFA:0001149','ZFA:0001101');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0007037'
where zum_zfa_id in ('ZFA:0000034','ZFA:0001138');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002104'
where zum_zfa_id in ('ZFA:0001127', 'ZFA:0000556','ZFA:0000435','ZFA:0001678','ZFA:0000137');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000924'
where zum_zfa_id = 'ZFA:0000016';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000925'
where zum_zfa_id = 'ZFA:0000017';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000926'
where zum_zfa_id = 'ZFA:0000041';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0003104'
where zum_zfa_id = 'ZFA:0000393';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0001013'
where zum_zfa_id = 'ZFA:0005345';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0000026'
where zum_zfa_id = 'ZFA:0000108';

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0016887'
where zum_zfa_id in ('ZFA:0000020','ZFA:0009176');

update zfa_uberon_mapping
 set zum_uberon_id = 'UBERON:0002539'
where zum_zfa_id = 'ZFA:0001306';
