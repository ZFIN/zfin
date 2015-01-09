select zdb_id
 from publication
where pub_completion_date is null
and pub_is_indexed = 'f'
and pub_file is not null
and get_date_from_id(zdb_id,"YYYYDDMM") > '20110101';