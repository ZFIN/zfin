#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, MergerServlet directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/j2ee/MergerServlet

# Need to come up with a better way to make context descriptors.

CONTEXT_DESCRIPTOR = MergerServlet.xml
CONTEXT_DESCRIPTOR_PATH = $(CONTEXT_DIR)/$(CONTEXT_DESCRIPTOR)

SUBDIRS = WEB-INF

# Commend out coz the only effect that is identified so far is
# that "gmake clobber" deletes the context descriptor file, which
# we dont' want.
#ENDEMICTARGETS_POST = $(CONTEXT_DESCRIPTOR_PATH)


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules



# ---------------  Endemic Rules  ----------------------------------------

# Place the web application context descriptor file where Tomcat will find it.

$(CONTEXT_DESCRIPTOR_PATH) : $(CONTEXT_DESCRIPTOR) $(TRANSLATETABLE)
	$(MAKESPECIFIC) $(CONTEXT_DESCRIPTOR) $(TRANSLATETABLE) $(CONTEXT_DESCRIPTOR_PATH)
	$(SET_CONTEXT_DESCRIPTOR) $(DOMAIN_NAME) $(CONTEXT_DESCRIPTOR)
