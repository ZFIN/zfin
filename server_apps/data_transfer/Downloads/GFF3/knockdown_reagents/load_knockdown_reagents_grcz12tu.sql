delete from gff3 where gff_source = 'ZFIN_knockdown_reagent_GRCz12tu';
\copy gff3 from 'E_zfin_knockdown_reagents_grcz12tu.unl' delimiter '|';