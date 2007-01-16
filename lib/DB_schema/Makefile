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

TARGETDIR = $(TARGETROOT)/lib/DB_schema

SUBDIRS = FRODO 

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Special Rules  ---------------------------------------

postloaddb : clean clobber all