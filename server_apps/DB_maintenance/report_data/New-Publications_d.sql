SELECT zdb_ID,
       authors,
       title,
       Year(pub_date) AS pyear,
       jrnl_abbrev
FROM   publication,
       journal
WHERE  Get_date_from_id(zdb_id, 'YYYYMMDD') = '$YEAR$MONTH$DAY'
       AND status = 'active'
       AND jrnl_zdb_id = pub_jrnl_zdb_id
UNION
 SELECT zdb_ID,
       authors,
       title,
       Year(pub_date) AS pyear,
       jrnl_abbrev
FROM   publication,
       updates,
       journal
WHERE  zdb_id = rec_id
       AND jrnl_zdb_id = pub_jrnl_zdb_id
       AND field_name = 'status'
       AND new_value = 'active'
       AND to_char(upd_when,'%m') = '$MONTH'
       AND to_char(upd_when,'%d') = '$DAY'
       AND to_char(upd_when,'%Y') = '$YEAR'
ORDER  BY zdb_id  
