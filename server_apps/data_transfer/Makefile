#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Data Transfer server apps directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Targets made by this makefile are used to transfer data into or out of
# ZFIN.  They are not directly callable from the web site.
#
# Outgoing data transfer files go in one of two places:
#  FTP:  /pub/data_transfer
#  HTTP: /data_transfer
# Over the long run ZFIN is probably moving from FTP to HTTP for outgoing
# data transfer.  HTTP is slightly slower, but using HTTP only means
# that we have one fewer thing to keep running.

# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer

SUBDIRS = MEOW RefSeq SWISS-PROT GXP GO ZIRC Downloads GenPept ZGC \
	  Sanger Genbank LoadGO BLAST Anatomy ORTHO PATO DOI Load Microarray

GENERICS = pull.sh

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  --------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@


# ---------------  Misc Targets  ----------------------------------------

# The push target runs all of the push scripts in the data transfer directories.
# It is useful whenever we move zfin.org from one machine to another and need
# to populate all of the outgoing data transfer files.

push : $(SUBDIRS)
	$(MAKE) -C Downloads run
	$(MAKE) -C GO run
	$(MAKE) -C Sanger run
	$(MAKE) -C ZIRC push
	$(MAKE) -C BLAST run
