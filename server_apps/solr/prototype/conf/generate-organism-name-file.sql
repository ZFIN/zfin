unload to 'organism-name.txt'
select organism_common_name
from organism;

!/private/bin/perl -p -i -e 's/\|//' organism-name.txt
