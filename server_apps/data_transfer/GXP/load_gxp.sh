#!/bin/tcsh
#
#

if ($#argv < 2) then
    echo "Usage: load_gxp.sh  dbname  labname  <datatype>"
    echo " e.g.  load_gxp.sh almdb Thisse fr"
    exit
endif

# inherit environment variable from the shell
#setenv INFORMIXDIR       /private/apps/Informix/informix_wanda
#setenv INFORMIXSERVER    wanda
#setenv INFORMIXSQLHOSTS  $INFORMIXDIR/etc/sqlhosts.wanda
#setenv ONCONFIG          onconfig.wanda

set dbname = $1
set labname = $2    
set datatype = $3

if ($labname == "Thisse") then
    set submitter = "ZDB-PERS-960805-556"
    set fishline  = "ZDB-FISH-010924-10"

    if ($datatype == "cb") then 
    	set pubId    = "ZDB-PUB-010810-1"
	set sourceId = "ZDB-LAB-991005-53"    # ZIRC
	set genePrefix = "sb:"
    else   # fr
	set pubId    = "ZDB-PUB-040907-1"
	set sourceId = "ZDB-LAB-040907-1"     # I.M.A.G.E. consortium
	set genePrefix = "im:"
    endif
endif

if ($labname == "Talbot") then
    set submitter = "ZDB-PERS-980223-5"
    set fishline  = "ZDB-FISH-960809-7"
    set pubId     = "ZDB-PUB-031103-24"
    set sourceId  = "ZDB-LAB-040914-1"    # RZPD  updated
    set genePrefix = "wu:"

endif

$INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load.sql

foreach file (*.err)
    if (! -z $file) then
	echo "ERROR! Check $file!";
	$INFORMIXDIR/bin/dbaccess $dbname post_gxp_load.sql
   	exit;
    endif
end
/bin/rm -f *.err

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_quantity_check.sql >& preload_quantity.txt

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_func.sql

echo "execute function gxp_load_func('$labname', '$submitter','$pubId','$sourceId','$fishline', '$genePrefix', '$datatype')" | $INFORMIXDIR/bin/dbaccess $1

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_quantity_check.sql >& postload_quantity.txt

echo "======================================================="
echo "== Diff table counts before(<) and after(>) the load =="
diff preload_quantity.txt postload_quantity.txt
echo "======================================================="

echo "Ready to drop the temporary tables? (y or n)"
set goahead = $< 
if ($goahead == 'y') then
    $INFORMIXDIR/bin/dbaccess $dbname post_gxp_load.sql
    echo "Congratulations!" 
else 
    echo "Abort with the 15 temporary tables in db"
endif

exit;
