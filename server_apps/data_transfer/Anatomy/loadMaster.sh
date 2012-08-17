#!/bin/tcsh
#
# FILE: loadMaster.sh
#
# It loads AO, then run annotation adjustment and thisse
# template keywords verification.
#
# INPUT:
#      .unl from parseOBO.pl
#      AO_translation.unl (optional)
#      stageKeyword.csv
# OUPUT:
#      STDOUT: confirm when no AO_translation.unl
#              SQL execution output  
#      email to <!--|AO_EMAIL_CURATOR|--> with thiss keywords check results
#
setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS <!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->

#--------------------------------------
# Check and prepare AO_translation.unl
#---------------------------------------
echo "=== Process AO_translation.unl, Verify stage abbrev ==="
if (-e AO_translation.unl) then
    # ensure one and only one trailing bar 
    /bin/sed 's/\|$//' AO_translation.unl | /bin/sed 's/$/\|/' > n_AO_translation.unl
    /bin/mv n_AO_translation.unl AO_translation.unl

    $INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> verify_stage_abbrev.sql

else
    echo "WARNING: Cannot find AO_translation.unl";

endif

echo "Good to continue? ";

set goahead = $< 
if ($goahead == 'n') then
      exit
endif 


#--------------------------------------
# Check and prepare stageKeyword.unl
#---------------------------------------
echo "=== Process stageKeyword.csv  ==="
if (-e stageKeyword.csv) then

    ./parse_stage_keywords.pl

else 
    echo "WARNING: Cannot find stageKeyword.unl";
endif 

#--------------------------------------
# Run loading
#---------------------------------------
echo "=== Load AO  ==="
$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> loadAO.sql

#-------------------------------------------
# Define batch_xpat_annot_adjust() function
# and run it on entries in AO_translation.unl
# then, check annotation again for any omission
#-------------------------------------------
echo "=== Adjust annotation  ==="
$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> batch_xpat_annot_adjust.sql

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> make_annot_translation.sql

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> check_annotation.sql

#--------------------------------------
# Check keywords in Thisse template
# Email results to <!--|AO_EMAIL_CURATOR|-->
#---------------------------------------
echo "=== Check Thisse keyword file  ==="
$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> check_thisse_keywords.sql


set SUBJECT = "Auto: Problem AO in Thiss Template"
set MAILTO = `echo "<!--|AO_EMAIL_CURATOR|-->" | tr -d '\\' `;

echo "From: $LOGNAME" > /tmp/AO_thisse_mail
echo "To: $MAILTO" >> /tmp/AO_thisse_mail
echo "Subject: $SUBJECT" >> /tmp/AO_thisse_mail
echo "Mime-Version: 1.0" >> /tmp/AO_thisse_mail
echo "Content-Type: text/plain" >> /tmp/AO_thisse_mail

echo "Keywords no longer being the primary name :" >> /tmp/AO_thisse_mail
echo "-------------------------------------------" >>  /tmp/AO_thisse_mail
cat ./kwdNameNotPrim.err >> /tmp/AO_thisse_mail

echo " " >> /tmp/AO_thisse_mail

echo "Keywords has stage problem:" >> /tmp/AO_thisse_mail
echo "-----------------------------" >>  /tmp/AO_thisse_mail
cat ./kwdStageInconsis.err >> /tmp/AO_thisse_mail

/usr/lib/sendmail -t -oi < /tmp/AO_thisse_mail

/bin/rm -f /tmp/AO_thisse_mail

exit;


