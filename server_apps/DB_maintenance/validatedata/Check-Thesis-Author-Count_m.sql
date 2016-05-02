-- This query uses a `like` statement because the num_auths column doesn't seem to be
-- very reliable. Similarly using jrnl_name catches more than using j_type.

SELECT zdb_id, title, authors
FROM publication
INNER JOIN journal ON journal.jrnl_zdb_id = publication.pub_jrnl_zdb_id
WHERE journal.jrnl_name LIKE '%Thesis%'
AND publication.authors LIKE '%,%,%'
