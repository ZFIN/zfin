#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Server apps, cron directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This directory puts the contents of ZFIN crontab files under 
# CVS control.  We do this for a number of reasons:
#
# 1. It just seems like a good idea to have the crontab files under
#    source code control, just in case we inadvertently removed it.
#
# 2. We occassionally switch the web site from the production server
#    to the backup/development server.  Switching the crontab files
#    from one machine to the other (and then back) has been problematic.
#    Hopefully, this makefile will smooth that process.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance/validatedata

GENERICS = Check-Term-Stage-Range_d.sql linkageHasMember.sql StrGeneAbbreviation.sql MarkerGoEvidenceDuplicate.sql Check-Marker-Go-Secondary-Annotation_d.sql PublicationDuplicate.sql ProbeGeneRelationship.sql MarkerGoInference.sql EstContainedInRelationship.sql Check-EST-One-Marker_w.sql XXGenesNoClones.sql RefseqAccessionFormat.sql AssoicateDataCurationOrthology.sql Check-EST-Contained-In-Relationship_m.sql Check-For-Orphaned-Features_q.sql Check-Tg-Feature-Without-Construct_m.sql

STATICS = validate-data-email-report.properties validate-data-email-report.template

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


