SELECT person.NAME, 
       publication.zdb_id, 
       authors 
FROM   publication, 
       person 
WHERE  NOT EXISTS (SELECT 'x' 
                   FROM   int_person_pub 
                   WHERE  target_id = publication.zdb_id) 
       AND authors LIKE "%"|| Replace(NAME, "-", ",")|| "%";