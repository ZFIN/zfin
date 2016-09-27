select zdb_id
 from publication
where pub_completion_date is null
and pub_is_indexed = 'f'
and not exists (
  select 'x' from publication_file
  inner join publication_file_type on publication_file_type.pft_pk_id = publication_file.pf_file_type_id
  where pf_pub_zdb_id = zdb_id
  and pft_type = 'Original Article'
)
and get_date_from_id(zdb_id,"YYYYDDMM") > '20110101';