
copy (
select *
from tmp_vega_thisse_report
order by 1,4 ) to 'vega_thisse_report.unl'  DELIMITER ' '
;


copy gff3 from 'terms_missing_obo_id.txt' delimiter '|';

copy (
select *
from tmp_vega_thisse_report
order by 1,2 ) to 'vega_thisse_report.unl' delimiter ' '
;
