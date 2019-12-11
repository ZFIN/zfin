--liquibase formatted sql
--changeset pm:PUB-628

update journal set jrnl_name='Journal of Biomedical Semantics' where jrnl_zdb_id='ZDB-JRNL-140513-27';

