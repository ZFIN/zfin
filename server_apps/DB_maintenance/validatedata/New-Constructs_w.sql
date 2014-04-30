select mrkr_zdb_id, mrkr_name
  from marker
  where mrkr_owner not in (
'ZDB-PERS-981201-7',
'ZDB-PERS-000912-1',
'ZDB-PERS-100329-1')
and mrkr_type in ('TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT')
and get_date_from_id(mrkr_zdb_id, "YYYYMMDD") > "20140401";