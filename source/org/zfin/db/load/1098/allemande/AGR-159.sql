--liquibase formatted sql
--changeset sierra:AGR-159.sql

alter table expression_pattern_assay
 add column xpatassay_mmo_id text;

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000658'
where xpatassay_name = 'mRNA in situ hybridization';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000339'
where xpatassay_name = 'Northern blot';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000498'
where xpatassay_name = 'Immunohistochemistry';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000669'
where xpatassay_name = 'Western blot';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000655'
where xpatassay_name = 'Reverse transcription PCR';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000670'
where xpatassay_name = 'Intrinsic fluorescence';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000651'
where xpatassay_name = 'Nuclease protection assay';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000654'
where xpatassay_name = 'Primer extension';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000645'
where xpatassay_name = 'cDNA clones';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000640'
where xpatassay_name = 'other';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000534'
where xpatassay_name = 'Mass Spectrometry';

update expression_pattern_assay
  set xpatassay_mmo_id = 'MMO:0000661'
where xpatassay_name = 'Gene Product Function';
