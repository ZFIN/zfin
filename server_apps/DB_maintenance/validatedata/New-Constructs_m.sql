select mrkr_name, mrkr_zdb_id,  name
 from marker, person
 where mrkr_type in ('TGCONSTRCT','PTCONSTRCT','ETCONSTRCT','GTCONSTRCT')
 and mrkr_owner not in ('ZDB-PERS-100329-1','ZDB-PERS-981201-7')
and get_date_from_id(mrkr_zdb_id,"YYYY-MM-DD") > today - 30 units day
 and zdb_id = mrkr_owner;