--liquibase formatted sql
--changeset cmpich:14141
update construct_component set cc_component_zdb_id='ZDB-GENE-990415-172' where cc_component='neurod' and cc_construct_zdb_id='ZDB-TGCONSTRCT-120119-2';
update construct_component set cc_component_zdb_id='ZDB-GENE-990415-172' where cc_component='neurod' and cc_construct_zdb_id='ZDB-TGCONSTRCT-120113-1';
update construct_component set cc_component_zdb_id='ZDB-GENE-990415-172' where cc_component='neurod' and cc_construct_zdb_id='ZDB-TGCONSTRCT-080701-1';
