
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

GENERICS = loadMIRANDALinks.pl loadSupport.pl loadVegaTscripts.pl \
parseAssembly.pl parseMIRANDA.pl parseMicroRNA.pl parseStar.pl \
parseSuppt.pl parseVega.pl parseVegaProt.pl


STATICS = loadVegaTscripts.sql loadMicroRNA.sql loadVegaProt.sql Danio_rerio.VEGA.sep.pep.tot.fa \
loadMIRANDALinks.sql miRNA.csv makeCuratedFasta.pl makeCuratedStemFasta.pl makeLoadedFastaMature.pl \
makeNUCLsMatureMicroRNA.sql makeLoadedFastaStem.pl


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules
