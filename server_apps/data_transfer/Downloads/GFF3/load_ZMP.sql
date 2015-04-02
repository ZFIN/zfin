! echo "load_zmp.sql <- zmp.unl"

begin work;

delete from gff3 where gff_source = 'ZMP';

load from 'zmp.unl' insert into gff3;
update statistics high for table gff3;
