select
    upr_id, upr_date, upr_size, upr_md5, upr_path, upr_download_date, upr_release_number, upr_notes
from uniprot_release
where now() - upr_download_date < '48 hours'::interval
   or upr_download_date is null;