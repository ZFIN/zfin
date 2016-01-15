SELECT publication.zdb_id,
       person.NAME,
       person.zdb_id,
       first_name,
       last_name,
       authors
FROM   publication,
       person
WHERE  num_auths > (SELECT Count(*)
                    FROM   int_person_pub
                    WHERE  target_id = publication.zdb_id)
       AND authors LIKE "%"
                        || Replace(NAME, "-", ",")
                        || "%"
       AND NOT EXISTS (SELECT 'x'
                       FROM   int_person_pub b
                       WHERE  b.source_id = person.zdb_id
                              AND b.target_id = publication.zdb_id)
ORDER  BY publication.zdb_id;