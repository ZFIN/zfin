--liquibase formatted sql
--changeset pm:CUR-949



update construct set construct_name='Tg(en.sill,hsp70l:mCherry)' where construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update marker set mrkr_name='Tg(en.sill,hsp70l:mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-1';
update marker set mrkr_abbrev ='Tg(en.sill,hsp70l:mCherry)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-1';

update construct set construct_name='Tg(en.sill,hsp70l:GAL4-VP16)' where construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update marker set mrkr_name='Tg(en.sill,hsp70l:GAL4-VP16)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-2';
update marker set mrkr_abbrev ='Tg(en.sill,hsp70l:GAL4-VP16)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct set construct_name='Tg(en.sill,hsp70l:GCaMP6S)' where construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update marker set mrkr_name='Tg(en.sill,hsp70l:GCaMP6S)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-3';
update marker set mrkr_abbrev ='Tg(en.sill,hsp70l:GCaMP6S)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191120-3';

update construct_component set cc_component='en.sill'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_zdb_id='ZDB-ENHANCER-191120-1'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_category='promoter component' where cc_component='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';

update construct_component set cc_component=','  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-11'  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_category='promoter component' where cc_component=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';

update construct_component set cc_component='hsp70l'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_zdb_id='ZDB-GENE-050321-1'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_category='promoter component' where cc_component='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';

update construct_component set cc_component=':'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-10'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_category='promoter component' where cc_component=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';

update construct_component set cc_component='mCherry'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_zdb_id='ZDB-EFG-080214-1'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_category='coding sequence component' where cc_component='mCherry' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='mCherry' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-1';


update construct_component set cc_component='en.sill'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-ENHANCER-191120-1'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='promoter component' where cc_component='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component=','  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-11'  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='promoter component' where cc_component=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component='hsp70l'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-GENE-050321-1'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='promoter component' where cc_component='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component=':'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-10'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='promoter component' where cc_component=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component='GAL4'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-EFG-080319-1'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='coding sequence component' where cc_component='GAL4' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='GAL4' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component='-'  where cc_order=8 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='coding sequence component' where cc_component='-' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='text component' where cc_component_zdb_id='-' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';

update construct_component set cc_component='VP16'  where cc_order=9 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_zdb_id='ZDB-EREGION-130306-1'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_category='coding sequence component' where cc_component='VP16' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='VP16' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-2';




update construct_component set cc_component='en.sill'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_zdb_id='ZDB-ENHANCER-191120-1'  where cc_order=3 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_category='promoter component' where cc_component='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='en.sill' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';

update construct_component set cc_component=','  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-11'  where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_category='promoter component' where cc_component=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=',' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';

update construct_component set cc_component='hsp70l'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_zdb_id='ZDB-GENE-050321-1'  where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_category='promoter component' where cc_component='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_type='promoter of' where cc_component_zdb_id='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';

update construct_component set cc_component=':'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_zdb_id='ZDB-CV-150506-10'  where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_category='promoter component' where cc_component=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_type='controlled vocab component' where cc_component_zdb_id=':' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';

update construct_component set cc_component='GCaMP6S'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_zdb_id='ZDB-EFG-120320-3'  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_category='coding sequence component' where cc_component='GCaMP6S' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';
update construct_component set cc_component_type='coding sequence of' where cc_component_zdb_id='GCaMP6S' and cc_construct_zdb_id='ZDB-TGCONSTRCT-191120-3';








update  construct_marker_relationship set conmrkrrel_relationship_type='promoter of' where   conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-191120-1' and conmrkrrel_mrkr_zdb_id='ZDB-ENHANCER-191120-1';
update  marker_relationship set mrel_type='promoter of' where   mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-191120-1' and mrel_mrkr_2_zdb_id='ZDB-ENHANCER-191120-1';


update  construct_marker_relationship set conmrkrrel_relationship_type='promoter of' where   conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-191120-2' and conmrkrrel_mrkr_zdb_id='ZDB-ENHANCER-191120-1';
update  marker_relationship set mrel_type='promoter of' where   mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-191120-2' and mrel_mrkr_2_zdb_id='ZDB-ENHANCER-191120-1';


update  construct_marker_relationship set conmrkrrel_relationship_type='promoter of' where   conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-191120-3' and conmrkrrel_mrkr_zdb_id='ZDB-ENHANCER-191120-1';
update  marker_relationship set mrel_type='promoter of' where   mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-191120-3' and mrel_mrkr_2_zdb_id='ZDB-ENHANCER-191120-1';













