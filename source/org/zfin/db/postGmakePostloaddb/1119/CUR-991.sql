--liquibase formatted sql
--changeset pm:CUR-991



update construct set construct_name='Tg(gja9a:Cas.Fanac-2A-mCherry)' where construct_zdb_id='ZDB-TGCONSTRCT-140924-3';
update marker set mrkr_name='Tg(gja9a:Cas.Fanac-2A-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140924-3';
update marker set mrkr_abbrev='Tg(gja9a:Cas.Fanac-2A-mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-140924-3';


update construct_component set cc_component='gja9a'  where cc_component='cx55' and cc_construct_zdb_id='ZDB-TGCONSTRCT-140924-3';
delete from construct_component where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-140924-3';
delete from construct_component where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-140924-3';
update construct_component set cc_component_zdb_id='ZDB-GENE-010619-3'  where cc_component='gja9a' and cc_construct_zdb_id='ZDB-TGCONSTRCT-140924-3';
update construct_component set cc_component_type='promoter of'  where  cc_construct_zdb_id='ZDB-TGCONSTRCT-140924-3' and cc_component='gja9a';


update construct set construct_name='Tg(gja9a:MA-GFP)' where construct_zdb_id='ZDB-TGCONSTRCT-150319-1';

update marker set mrkr_name='Tg(gja9a:MA-GFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150319-1';
update marker set mrkr_abbrev='Tg(gja9a:MA-GFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150319-1';


update construct_component set cc_component='gja9a'  where cc_component='cx55' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150319-1';
update construct_component set cc_component_zdb_id='ZDB-GENE-010619-3'  where cc_component='gja9a' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150319-1';
update construct_component set cc_component_type='promoter of'  where  cc_construct_zdb_id='ZDB-TGCONSTRCT-150319-1' and cc_component='gja9a';
delete from construct_component where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-150319-1';
delete from construct_component where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-150319-1';













