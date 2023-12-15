select zrepld_old_zdb_id,  zrepld_new_zdb_id, zrepld_date_created
from zdb_replaced_data
where zrepld_date_created > now() - interval '8 day';
