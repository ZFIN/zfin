CREATE OR REPLACE FUNCTION count_substring_plus_one(text, text) RETURNS INTEGER AS '
	DECLARE
		sub   ALIAS FOR $2;
		str   VARCHAR;
		pos   INTEGER;
		total INTEGER;
	BEGIN
		str   := $1;
		total := 1;
		LOOP
			pos := strpos(str, sub);
			IF pos = 0 THEN
				RETURN total;
			ELSE
				total := total + 1;
				str = substr(str, pos + 1);
			END IF;
		END LOOP;
		RETURN total;
	END;
' LANGUAGE 'plpgsql';

SELECT publication.zdb_id, 
       person.NAME, 
       person.zdb_id, 
       first_name, 
       last_name, 
       authors 
FROM   publication, 
       person 
WHERE  count_substring_plus_one(authors, ',') > (SELECT count(*) 
                                                   FROM   int_person_pub 
                                                  WHERE  target_id = publication.zdb_id) 
       AND authors LIKE '%' 
                        || Replace(NAME, '-', ',') 
                        || '%' 
       AND EXISTS (SELECT 'x'
                       FROM   int_person_pub 
                       WHERE  target_id = publication.zdb_id) 
       AND NOT EXISTS (SELECT 'x' 
                       FROM   int_person_pub b 
                       WHERE  b.source_id = person.zdb_id 
                              AND b.target_id = publication.zdb_id) 
ORDER  BY publication.zdb_id; 

DROP FUNCTION count_substring_plus_one(text, text);
