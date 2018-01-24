SELECT zobjtype_name,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column
FROM   zdb_object_type
WHERE  zobjtype_home_table NOT IN (SELECT table_name
                                   FROM   information_schema.tables
                                   WHERE table_schema = 'public')
        OR zobjtype_home_zdb_id_column NOT IN (SELECT column_name
                                               FROM   zdb_object_type a,
                                                      information_schema.columns b
                                               WHERE
           zobjtype_home_table = b.table_name);