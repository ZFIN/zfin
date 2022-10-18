-- ignore merged records
delete from temp_8191 where old_data_zdb_id in (select zrepld_old_zdb_id from zdb_replaced_data);

UPDATE record_attribution ra
SET recattrib_data_zdb_id = t.old_data_zdb_id
    FROM temp_8191 t
    WHERE ra.recattrib_pk_id = t.recattrib_pk_id
    AND ra.recattrib_data_zdb_id = t.new_data_zdb_id;

drop table temp_8191;
