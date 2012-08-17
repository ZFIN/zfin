#!/bin/tcsh
# 
# FILE: load_gxp.sh
#
# This master script calls the pre_gxp_load.sql first to load data into
# temp tables and conduct quality check, it aborts if error is detected. 
# It pauses for user input on whether to load real database. 
# It gives table content comparison after the execution.
# 
# INPUT:
#      database name
#      lab name
#      release type (if Thisse)
#
# OUTPUT:
#      (fr_gene.txt)
#      (statistics.txt)
# 
# EFFECT:
#      data loaded into db  
#      image files moved into central repository    
#
# NOTE:
#   inherit Informix environment variables from the shell
#

if ($#argv < 2) then
    echo "Usage: load_gxp.sh  dbname  labname  <datatype>"
    echo " e.g.  load_gxp.sh almdb Thisse fr/eu_nm/eu"
    exit
endif

set dbname = $1
set labname = $2    
if ($#argv == 3) then
    set datatype = $3 
else 
    set datatype = ""
endif

#----------------------------------------
# Set Variables
#----------------------------------------

set assayname = "mRNA in situ hybridization";

# we had one case that the owner of the clone is different
# from the owner of the expression, as well as the publication
# attribution. Thus we have two owners and two pubs defined now.
if ($labname == "Thisse") then
    set mrkrOwnerId = "ZDB-PERS-960805-556"
    set imgOwnerId = "ZDB-PERS-960805-556"
    set genox     = "ZDB-GENOX-041102-1429"  

    if ($datatype == "cb") then 
    	set mrkrPubId    = "ZDB-PUB-010810-1"
    	set xpatPubId    = "ZDB-PUB-010810-1"
	set sourceId = "ZDB-LAB-991005-53"    # ZIRC
	set genePrefix = "sb:"

    else if ( $datatype == "eu" || $datatype == "eu_nm" ) then
    	set mrkrPubId    = "ZDB-PUB-051025-1" 
	set xpatPubId    = "ZDB-PUB-051025-1" 
	set sourceId = ""    
	set genePrefix = "sb:" 

    else if ( $datatype == "sc") then
    	set mrkrPubId    = "ZDB-PUB-080227-22"  
       	set xpatPubId    = "ZDB-PUB-080227-22"  
	set sourceId = "ZDB-LAB-060808-1"    
	set genePrefix = "sc:" 

    else if ( $datatype == "nr") then
        set mrkrOwnerId = "ZDB-PERS-010213-1"
    	set mrkrPubId    = "ZDB-PUB-071118-46"   
      	set xpatPubId    = "ZDB-PUB-080220-1"   
	set sourceId = "ZDB-LAB-010213-1"    
	set genePrefix = "gb:" 

    else   # fr
   	set mrkrPubId    = "ZDB-PUB-040907-1"
   	set xpatPubId    = "ZDB-PUB-040907-1"
	set sourceId = "ZDB-LAB-040907-1"     # I.M.A.G.E. consortium
	set genePrefix = "im:"
    endif
endif

if ($labname == "Talbot") then
    set mrkrOwnerId = "ZDB-PERS-980223-5"
    set imgOwnerId = "ZDB-PERS-980223-5"
    set genox     = "ZDB-GENOX-041102-546"   
    set mrkrPubId     = "ZDB-PUB-031103-24"
    set xpatPubId     = "ZDB-PUB-031103-24"
    set sourceId  = "ZDB-COMPANY-051101-1"    # RZPD 
    set genePrefix = "wu:"

endif

#--------------------------------------------
# Preload 
#
# abort with cleanup on errors, or user request
#--------------------------------------------

$INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load.sql

foreach file (*.err)
    if (! -z $file) then
	echo "ERROR! Check $file!";
	$INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load_cleanup.sql
   	exit;
    endif
end
/bin/rm -f *.err

echo "Ready to load database? (y or n)"
set goahead = $< 
if ($goahead == 'n') then
    $INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load_cleanup.sql
    exit;
endif


#-----------------------------------------------
# Load
#
# record related tables' content before and after
# the load. 
#-----------------------------------------------

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_quantity_check.sql >& preload_quantity.txt

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_func.sql

echo "execute function gxp_load_func('$labname', '$datatype', '$mrkrOwnerId','$imgOwnerId','$mrkrPubId','$xpatPubId','$sourceId','$genox', '$genePrefix','$assayname')" | $INFORMIXDIR/bin/dbaccess $dbname

# when we get a little more confidence on the stableness of the function,
# we will move it to lib/DB_function, and get rid of the creation and 
# deletion. 
echo "drop function gxp_load_func" | $INFORMIXDIR/bin/dbaccess $dbname

$INFORMIXDIR/bin/dbaccess $dbname gxp_load_quantity_check.sql >& postload_quantity.txt

# we add control here to avoid image copying on failure.
echo "Good to continue? (y or n)"
set goahead = $< 
if ($goahead == 'n') then
    # In case of failture,  gxp_load_func() rolls back transaction,
    # but we need to cleanup tables from pre_gxp_load.sql so that to 
    # have a fresh start after we fix things.
    $INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load_cleanup.sql
    exit;
endif


#-----------------------------------------------
# Report table diff, Generate fr_gene translation
# table and statistics report
#
#-----------------------------------------------

echo "== Diff table counts before(<) and after(>) the load =="
echo "== Please compare with the probe # and image # sent  =="

/bin/diff preload_quantity.txt postload_quantity.txt

echo "Generating statistics ..."
if ($labname == "Thisse" && $datatype != "cb") then
    $INFORMIXDIR/bin/dbaccess $dbname get_fr_gene_list.sql
    $INFORMIXDIR/bin/dbaccess $dbname getStat4Load.sql >& statistics.txt 
endif


#--------------------------------------------
# Copy images into central repository 
#
#--------------------------------------------

# add proper path to image file names, and escape special characters
# it is a little redundant, but readability is not bad.
if ( $HOST == "helix" ) then 
    /bin/sed 's/^/images\//' img_oldname_2_newname.txt | \
    /bin/sed 's/ZDB/\/research\/zprod\/loadUp\/imageLoadUp\/ZDB/' | \
    /bin/sed 's/(/\\(/g' | /bin/sed 's/)/\\)/g' >! img_oldname_2_newname.conv
else
    /bin/sed 's/^/images\//' img_oldname_2_newname.txt | \
    /bin/sed 's/ZDB/\/research\/zcentral\/loadUp\/imageLoadUp\/ZDB/' | \
    /bin/sed 's/(/\\(/g' | /bin/sed 's/)/\\)/g' >! img_oldname_2_newname.conv
endif

# create a scrip for the move
echo "#\!/bin/tcsh" >! copy_image.sh

/bin/awk -F\| '{print "/bin/cp ", $1 ".jpg", $2 ".jpg"} \
	       {print "/bin/cp ", $1 "--t.jpg", $2 "_thumb.jpg"}'  img_oldname_2_newname.conv >> copy_image.sh


if ($datatype == "cb") then
    /bin/awk -F\| '{print "/bin/cp ", $1 "--C.jpg", $2 "_annot.jpg"}' img_oldname_2_newname.conv >> copy_image.sh
endif

chmod ug+x copy_image.sh
echo "Copy images to repository..."
./copy_image.sh


#-----------------------------------------------
# Clean up
#----------------------------------------------- 
echo "Ready to drop the temporary tables? (y or n)"
set goahead = $< 
if ($goahead == 'y') then
    $INFORMIXDIR/bin/dbaccess $dbname pre_gxp_load_cleanup.sql
    $INFORMIXDIR/bin/dbaccess $dbname gxp_load_cleanup.sql
    if ($labname == "Thisse" && $datatype != "cb" && $HOST == "helix") then
       	echo "Please submit thisseName_ZfinMarker.txt and statistics.txt to curator." 
    endif
else 
    echo "Abort with the 15 temporary tables in db"
endif

exit;
