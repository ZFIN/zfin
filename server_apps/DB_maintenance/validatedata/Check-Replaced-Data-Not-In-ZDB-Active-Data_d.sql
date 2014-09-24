SELECT zrepld_old_zdb_id,
       zrepld_new_zdb_id,
       zrepld_old_name
FROM   zdb_replaced_data
WHERE  zrepld_old_zdb_id IN (SELECT zactvd_zdb_id
                             FROM   zdb_active_data);