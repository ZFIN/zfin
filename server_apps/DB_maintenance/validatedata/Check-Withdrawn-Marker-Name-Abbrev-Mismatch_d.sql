SELECT m.mrkr_zdb_id,
       m.mrkr_name,
       m.mrkr_abbrev
FROM   marker m
WHERE  m.mrkr_abbrev LIKE 'WITHDRAWN:%'
       AND m.mrkr_name NOT LIKE 'WITHDRAWN:%'
UNION
SELECT m.mrkr_zdb_id,
       m.mrkr_name,
       m.mrkr_abbrev
FROM   marker m
WHERE  m.mrkr_name LIKE 'WITHDRAWN:%'
       AND m.mrkr_abbrev NOT LIKE 'WITHDRAWN:%';