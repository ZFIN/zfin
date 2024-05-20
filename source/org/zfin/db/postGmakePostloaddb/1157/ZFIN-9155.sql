--liquibase formatted sql
--changeset cmpich:ZFIN-9155.sql

select count(*)
from ensembl_transcript_renaming;

update marker
set mrkr_abbrev = 'sgms1a-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-11986';

update marker
set mrkr_abbrev = 'zgc:165582-202',
    mrkr_name   = 'zgc:165582-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-131113-2029';

update marker
set mrkr_abbrev = 'si:dkeyp-14d3.1-203',
    mrkr_name   = 'si:dkeyp-14d3.1-203'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-5313';

update marker
set mrkr_abbrev = 'tgs1-001',
    mrkr_name   = 'tgs1-001'
where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-20047';

update marker
set mrkr_abbrev = 'tgs1-002',
    mrkr_name   = 'tgs1-002'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240508-2136';

update marker
set mrkr_abbrev = 'parp10-204',
    mrkr_name   = 'parp10-204'
where mrkr_zdb_id = 'ZDB-TSCRIPT-141209-1627';

update marker
set mrkr_abbrev = 'itgb1b.1-202',
    mrkr_name   = 'itgb1b.1-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-110325-1216';

update marker
set mrkr_abbrev = 'parp10-204',
    mrkr_name   = 'parp10-204'
where mrkr_zdb_id = 'ZDB-TSCRIPT-141209-1627';

update marker
set mrkr_abbrev = 'pamr1a-203',
    mrkr_name   = 'pamr1a-203'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-4442';

update marker
set mrkr_abbrev = 'pamr1b-201',
    mrkr_name   = 'pamr1b-201'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-6192';

update marker
set mrkr_abbrev = 'tmc1-201',
    mrkr_name   = 'tmc1-201'
where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-5156';

update marker
set mrkr_abbrev = 'ppt2b-203',
    mrkr_name   = 'ppt2b-203'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-3079';

update marker
set mrkr_abbrev = 'itgb1b.1-202',
    mrkr_name   = 'itgb1b.1-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-110325-1216';

update marker
set mrkr_abbrev = 'si:ch211-204c21.1-205',
    mrkr_name   = 'si:ch211-204c21.1-205'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240503-2477';

update marker
set mrkr_abbrev = 'dnah10-201',
    mrkr_name   = 'dnah10-201'
where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-4572';

update marker
set mrkr_abbrev = 'si:zfos-411a11.2-202',
    mrkr_name   = 'si:zfos-411a11.2-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-131113-1301';

update marker
set mrkr_abbrev = 'si:dkey-91f15.8-203',
    mrkr_name   = 'si:dkey-91f15.8-203'
where mrkr_zdb_id = 'ZDB-TSCRIPT-120213-738';

update marker
set mrkr_abbrev = 'meis2a-203',
    mrkr_name   = 'meis2a-203'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240508-2723';

update marker
set mrkr_abbrev = 'meis2a-205',
    mrkr_name   = 'meis2a-205'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240508-2725';

update marker
set mrkr_abbrev = 'meis2a-206',
    mrkr_name   = 'meis2a-206'
where mrkr_zdb_id = 'ZDB-TSCRIPT-240508-2724';


