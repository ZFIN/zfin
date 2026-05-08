-- Find zdb_object_type rows whose home table or home zdb-id column does not
-- exist in the database. zobjtype_home_schema (default 'public') and
-- zobjtype_home_table together identify the table.
SELECT zobjtype_name,
       zobjtype_home_schema,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column
FROM   zdb_object_type ot
WHERE  NOT EXISTS (
           SELECT 1
           FROM   information_schema.tables t
           WHERE  t.table_schema = ot.zobjtype_home_schema
             AND  t.table_name   = ot.zobjtype_home_table)
   OR  NOT EXISTS (
           SELECT 1
           FROM   information_schema.columns c
           WHERE  c.table_schema = ot.zobjtype_home_schema
             AND  c.table_name   = ot.zobjtype_home_table
             AND  c.column_name  = ot.zobjtype_home_zdb_id_column);
