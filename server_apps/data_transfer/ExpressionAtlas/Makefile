#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW ExpressionAtlas
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

all:
	gradle -p $(SOURCEROOT) :server_apps:data_transfer:ExpressionAtlas:deployFiles


TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/ExpressionAtlas

STATICS  =  addExpressionAtlasLinks.groovy



