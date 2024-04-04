--liquibase formatted sql
--changeset rtaylor:ZFIN-9021.sql

drop view if exists pub_location_metrics;
create view pub_location_metrics as
select distinct pub.zdb_id,

                -- candidate columns for category in outer group by --
                status,
                pub_is_indexed,
                case when pub.pub_is_indexed = 't' then 'Indexed' else 'Unindexed' end as pub_indexed_status,
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
                pts_status = 'READY_FOR_INDEXING' and nvl(ptl_location, '') not in ('PUB_INDEXER_1', 'PUB_INDEXER_2', 'PUB_INDEXER_3') as pub_is_unprioritized,
                ptl_location,
                case when pts_status = 'READY_FOR_INDEXING' and nvl(ptl_location, '') not in ('PUB_INDEXER_1', 'PUB_INDEXER_2', 'PUB_INDEXER_3') then 'Unprioritized' else ptl_location_display end as pub_location_or_prioritization_status

from publication pub
         left outer join pub_tracking_history history on pub.zdb_id = history.pth_pub_zdb_id
         left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id
         left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id
where pub.jtype = 'Journal';

-- select * from pub_location_metrics;