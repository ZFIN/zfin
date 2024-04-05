--liquibase formatted sql
--changeset rtaylor:ZFIN-9021.sql

DROP VIEW IF EXISTS pub_location_metrics;
CREATE VIEW pub_location_metrics AS

WITH subquery AS (
    SELECT DISTINCT
        pub.zdb_id,

        -- candidate columns for category in outer group by --
        status,
        pub_is_indexed,
        CASE WHEN pub_is_indexed THEN 'Indexed' ELSE 'Unindexed' END AS pub_indexed_status,
        pts_status_display,
        ptl_location_display,
        -- end candidate columns for category in outer group by --

        -- candidate columns for date expression --
        pub_indexed_date,
        pub_arrival_date,
        pth_status_insert_date,
        -- end candidate columns for date expression --

        -- filter to current only --
        pth_status_is_current,
        -- end filter to current only --

        -- column for "Is Unprioritized" logic --
        pts_status = 'READY_FOR_INDEXING'
            AND nvl (ptl_location, '')
            NOT IN ('PUB_INDEXER_1', 'PUB_INDEXER_2', 'PUB_INDEXER_3') AS pub_is_unprioritized
        -- end column for "Is Unprioritized" logic --
    FROM
        publication pub
        LEFT OUTER JOIN pub_tracking_history history ON pub.zdb_id = history.pth_pub_zdb_id
        LEFT OUTER JOIN pub_tracking_status status ON history.pth_status_id = status.pts_pk_id
        LEFT OUTER JOIN pub_tracking_location location ON history.pth_location_id = location.ptl_pk_id
    WHERE
        pub.jtype = 'Journal'
)
SELECT
    *,
    -- Add column that shows 'Unprioritized' if the publication is unprioritized, otherwise show the location --
    CASE WHEN pub_is_unprioritized THEN
        'Unprioritized'
    ELSE
        ptl_location_display
    END AS pub_location_or_prioritization_status
FROM
    subquery;
