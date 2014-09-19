SELECT zobjtype_name,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column
FROM   zdb_object_type
WHERE  zobjtype_home_table NOT IN (SELECT tabname
                                   FROM   systables)
        OR zobjtype_home_zdb_id_column NOT IN (SELECT colname
                                               FROM   zdb_object_type a,
                                                      systables b,
                                                      syscolumns c
                                               WHERE
           zobjtype_home_table = b.tabname
           AND b.tabid = c.tabid);