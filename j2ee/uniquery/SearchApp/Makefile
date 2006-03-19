#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  SearchApp directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/j2ee/uniquery/SearchApp

# Need to come up with a better way to make context descriptors.

CONTEXT_DESCRIPTOR = SearchApp.xml
CONTEXT_DESCRIPTOR_PATH = $(CONTEXT_DIR)/$(CONTEXT_DESCRIPTOR)

GENERICS = syntax_help.jsp  category_search.jsp

STATICS = internal_error.jsp search_parse_error.jsp

SUBDIRS = WEB-INF

# Commend out coz the only effect that is identified so far is
# that "gmake clobber" deletes the context descriptor file, which
# we dont' want.
# ENDEMICTARGETS_POST = $(CONTEXT_DESCRIPTOR_PATH)


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules


# ---------------  Endemic Rules  ----------------------------------------

$(CONTEXT_DESCRIPTOR_PATH) : $(CONTEXT_DESCRIPTOR) $(TRANSLATETABLE)
	$(MAKESPECIFIC) $(CONTEXT_DESCRIPTOR) $(TRANSLATETABLE) $(CONTEXT_DESCRIPTOR_PATH)
	$(SET_CONTEXT_DESCRIPTOR) $(DOMAIN_NAME) $(CONTEXT_DESCRIPTOR)
