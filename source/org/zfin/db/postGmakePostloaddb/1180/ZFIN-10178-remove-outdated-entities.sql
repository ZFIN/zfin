--liquibase formatted sql
--changeset rtaylor:zfin-10178-remove-outdated-entities

-- Remove outdated INFGRP, DBLINK, and LINK entities from
-- publication ZDB-PUB-040304-6 (Smart EJ et al., 2004).

-- linkage_membership has a restrict FK to zdb_active_data, so delete first
delete from linkage_membership
where lnkgm_linkage_zdb_id in (
    'ZDB-LINK-040414-1',
    'ZDB-LINK-040414-2',
    'ZDB-LINK-040414-3'
);

-- linkage_member_temp has no FK cascade, so delete explicitly
delete from linkage_member_temp
where linkage_zdb_id in (
    'ZDB-LINK-040414-1',
    'ZDB-LINK-040414-2',
    'ZDB-LINK-040414-3'
);

-- Deleting from zdb_active_data cascades to record_attribution,
-- db_link, linkage, linkage_old, linkage_single, linkage_member,
-- and linkage_membership_search.
delete from zdb_active_data
where zactvd_zdb_id in (
    'ZDB-INFGRP-040421-247',
    'ZDB-INFGRP-040421-38',
    'ZDB-DBLINK-040416-15',
    'ZDB-LINK-040414-1',
    'ZDB-LINK-040414-2',
    'ZDB-LINK-040414-3'
);
