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
if (-e AO_translation.unl) then
    /bin/sed 's/$/\|/' AO_translation.unl > n_AO_translation.unl
    /bin/mv n_AO_translation.unl AO_translation.unl
else
    echo "Cannot find AO_translation.unl, continue?";
    set goahead = $< 
    if ($goahead == 'n') then
      exit
    endif 
endif 

#--------------------------------------
# Check and prepare stageKeyword.unl
#---------------------------------------

if (-e stageKeyword.csv) then

    ./parse_stage_keywords.pl

else 
    echo "Cannot find stageKeyword.unl";
endif 

#--------------------------------------
# Run loading
#---------------------------------------

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> loadAO.sql

#-------------------------------------------
# Define batch_xpat_annot_adjust() function
# and run it on entries in AO_translation.unl
#-------------------------------------------

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> batch_xpat_annot_adjust.sql

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> make_annot_translation.sql

#--------------------------------------
# Check keywords in Thisse template
# Email results to <!--|AO_EMAIL_CURATOR|-->
#---------------------------------------

$INFORMIXDIR/bin/dbaccess <!--|DB_NAME|--> check_thisse_keywords.sql

set SUBJECT = "Auto: Problem AO in Thiss Template"
set MAILTO = `echo "<!--|AO_EMAIL_CURATOR|-->" | tr -d '\' `;

echo "From: $LOGNAME" > /tmp/AO_thisse_mail
echo "To: $MAILTO" >> /tmp/AO_thisse_mail
echo "Subject: $SUBJECT" >> /tmp/AO_thisse_mail
echo "Mime-Version: 1.0" >> /tmp/AO_thisse_mail
echo "Content-Type: text/plain" >> /tmp/AO_thisse_mail

echo "Keywords no long exist:" >> /tmp/AO_thisse_mail
cat ./keywordNameNotPrim.err >> /tmp/AO_thisse_mail

echo "\nKeywords has stage problem:" >> /tmp/AO_thisse_mail
cat ./kwdStageInconsis.err >> /tmp/AO_thisse_mail

/usr/lib/sendmail -t -oi < /tmp/AO_thisse_mail

exit;


