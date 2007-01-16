#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  phenote directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#
# This Makefile is specific to the phenote directory.
# Basically, we just want phenote.war to be unzipped in the target directory
# and WE don't want a false sense that WE can edit and deploy zfin-specific
# versions of phenote.war contents.
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/j2ee/phenote

GENERICS =
STATICS  =
SUBDIRS  = deploy

WARS     = phenote.war
DEPLOY   = $(TARGETDIR)/deploy
DATRAN   = $(DEPLOY)/WEB-INF/data_transfer/
SELECT   = select lotofile(obofile_text,
WHERE    = ,'server') from obo_file where obofile_name =
ONTOLOGS = gene_ontology.obo quality.obo zebrafish_anatomy.obo

# ---------------  Special Rules  ---------------------------------------

$(DEPLOY)/index.html : phenote.war
	$(WAR_UNZIP) $(DEPLOY) $(WARS)
	$(CHGRP_FISHADMIN) $(DEPLOY)
	$(CHMOD_774) $(DEPLOY)
	$(foreach ONTO,$(ONTOLOGS),\
		rm -f $(DATRAN)$(ONTO);\
		echo "$(SELECT)'$(DATRAN)$(ONTO)!' $(WHERE) '$(ONTO)'"| \
		$(INFORMIXDIR)/bin/dbaccess $(DBNAME);)
	$(TOUCH) $(DEPLOY)/index.html

# ---------------  Production Rules ------------------------------------

#Use defualt rules for directories without app pages in them.
include $(TOP)/make.default.rules

run :
	$(TOUCH) $(WARS); /local/bin/gmake
