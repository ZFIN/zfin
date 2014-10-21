unload to 'reporter-names.txt'
select mrkr_abbrev
from marker
where mrkr_zdb_id[1,7] = 'ZDB-EFG';

!/private/bin/perl -p -i -e 's/\|//' reporter-names.txt