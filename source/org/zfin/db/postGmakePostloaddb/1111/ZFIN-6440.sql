--liquibase formatted sql
--changeset christian:zfin-6440


update  controlled_vocabulary set cv_name_definition = 'Drosophila melanogaster' where cv_term_name = 'Dme.';
update  controlled_vocabulary set cv_name_definition = 'Homo sapiens' where cv_term_name = 'Hsa.';
update  controlled_vocabulary set cv_name_definition = 'Caenorhabditis elegans' where cv_term_name = 'Cel.';
