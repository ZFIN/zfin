SELECT zobjtype_name,
       zobjtype_is_data,
       zobjtype_is_source
FROM   zdb_object_type
WHERE  ( zobjtype_is_data = 't'
         AND zobjtype_name NOT IN (SELECT Get_obj_type(zactvd_zdb_id)
                                   FROM   zdb_active_data) )
        OR ( zobjtype_is_data = 'f'
             AND zobjtype_name IN (SELECT Get_obj_type(zactvd_zdb_id)
                                   FROM   zdb_active_data) )
        OR ( zobjtype_is_source = 't'
             AND zobjtype_name NOT IN (SELECT Get_obj_type(zactvs_zdb_id)
                                       FROM   zdb_active_source) )
        OR ( zobjtype_is_source = 'f'
             AND zobjtype_name IN (SELECT Get_obj_type(zactvs_zdb_id)
                                   FROM   zdb_active_source) )