begin work;

delete from gff3 where gff_source = 'ZFIN_knockdown_reagent';
load from 'E_zfin_knockdown_reagents.unl' insert into gff3;
update statistics high for table gff3;

-- commit/rollback applied externaly