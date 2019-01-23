--liquibase formatted sql
--changeset prita:ONT-673.sql

alter table expression_experiment2
 ADD COLUMN xpatex_assay_term_zdb_id text;
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-141' where xpatex_assay_name='cDNA clones';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-204' where xpatex_assay_name='Western blot';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-556' where xpatex_assay_name='Immunohistochemistry';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-638' where xpatex_assay_name='other';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-477' where xpatex_assay_name='mRNA in situ hybridization';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-72' where xpatex_assay_name='Reverse transcription PCR';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-386' where xpatex_assay_name='Primer extension';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-113' where xpatex_assay_name='Gene Product Function';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-198' where xpatex_assay_name='Nuclease protection assay';
update expression_experiment2
set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-331' where xpatex_assay_name='Mass Spectrometry';
update expression_experiment2 set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-454' where xpatex_assay_name= 'Northern blot';
update expression_experiment2 set xpatex_assay_term_zdb_id = 'ZDB-TERM-190122-319' where xpatex_assay_name='Intrinsic fluorescence';
 

alter table expression_experiment2
 add constraint xpatex_assay_term_zdb_id_fk_odc foreign key (xpatex_assay_term_zdb_id)
 references term(term_zdb_id) on delete cascade;

alter table expression_experiment2
drop column xpatex_assay_name;
