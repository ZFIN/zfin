select
   mrkr_zdb_id,
   mrkr_name,
   mrkr_abbrev
from
   marker m2
where
   mrkr_type = "EST"
   and 1 <>                        (
      select
         count(*)
      from
         marker m1,
         marker_relationship
      where
         mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
         and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
         and m1.mrkr_type in (
            select
               mtgrpmem_mrkr_type
            from
               marker_type_group_member
            where
               mtgrpmem_mrkr_type_group="GENEDOM"
         )
      )
   order by
      mrkr_name;
