--liquibase formatted sql
--changeset pm:INF-3462

update mutation_detail_Controlled_vocabulary set mdcv_term_order=0 where mdcv_term_zdb_id='ZDB-TERM-130401-322';

