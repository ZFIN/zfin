delete from gff3 where gff_source = 'ZFIN_knockdown_reagent';
copy gff3 from 'E_zfin_knockdown_reagents.unl' delimiter '|';
