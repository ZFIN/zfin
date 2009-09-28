
#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SWISS-PROT automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# See the README file. 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/transcript

GENERICS = loadVegaTscripts.pl parseVega.pl parseMicroRNA.pl loadVegaTscripts.sql loadMicroRNA.sql parseVegaProt.pl loadVegaProt.sql Danio_rerio.VEGA.sep.pep.tot.fa loadMIRANDALinks.sql loadMIRANDALinks.pl parseMIRANDA.pl parseStar.pl miRNA.csv makeCuratedFasta.pl makeCuratedStemFasta.pl makeNUCLsMatureMicroRNA.sql loadSupport.pl parseSuppt.pl makeLoadedFastaStem.pl makeLoadedFastaMature.pl parseAssembly.pl makeMatureMiRBASEFasta.pl makeStemLoopMiRBASEFasta.pl matureZebrafishMiRBASE.fa stemLoopZebrafishMiRBASE.fa\

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules
