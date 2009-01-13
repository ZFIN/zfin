#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, zextend.so external database 
# function library
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 
# This makefile is a little complicated.  It needs to be in order to 
# avoid hanging the database.  Here's what it does:
#  1. Creates a local copy of the target library.
#  2. Drops all functions in that library from the database.
#  3. Copies the target library to where it will be read by the server.
#  4. Defines all functions in that library to the database.
#
# Combining any of those steps may cause the database to hang.  In particular
# if you copy the new library over the old while informix is using a function
# that is in that library, very bad things will happen.
#
# Thought about using a staging directory here to handle the 
# create_functions.sql file, but opted not to go through the work for just
# one file.  This makefile creates a specific version of that file right
# before it needs it, and then deletes it right after it's done with it.
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

# Note that sibling directories to this one will have the same target 
# directory.

TARGETDIR = $(TARGETROOT)/lib/DB_functions

# zextend.c is the only thing we need to compile in this directory.
# It includes md5.c, which also includes md5.h, so we care about those, but
# they are picked up automatically when we compile zextend.c

# STATICS  = md5.c md5.h zextend.c drop_functions.sql
# GENERICS = create_functions.sql

SRCS = zextend.c
OBJS = zextend.so

TARGETDIR_OBJS = $(TARGETDIR)/zextend.so
ENDEMICTARGETS_POSTTARGETDIR = $(TARGETDIR_OBJS)
ENDEMICTARGETS_CLEAN = $(OBJS)


# ---- C compilation defines

CC= /local/apps/Lang/SUNWspro/bin/cc

INCDIR = $(INFORMIXDIR)/incl/public
LIBDIR = $(INFORMIXDIR)/lib 

# Use xcode because we are creating an so.  See cc man page for details.

CFLAGS = -fast -xcode=pic32 -xtarget=ultra2 -m64 -xarch=sparcvis
MICFLAGS = -DPLATFORM=solaris -g -DELF -v -DPOSIX_SIGNALS -DSOLARIS -I $(INCDIR)
MILIBS = -lsocket -lnsl -L$(LIBDIR)


# ---------------  Production Rules  ------------------------------------

include $(TOP)/make.default.rules

$(OBJS) : %.so : %.c
	$(CC) $(CFLAGS) -I $(INCDIR) $(MICFLAGS) -G $^ -o $@

$(TARGETDIR_OBJS) : $(TARGETDIR)/% : %
	-$(DBACCESS) $(DBNAME) drop_functions.sql > /dev/null 2>&1
	$(TARGET_CP) $^ $@		  # put it where DB will find it
	$(TOUCH) $@
	$(MAKESPECIFIC) create_functions.sql $(TRANSLATETABLE) cftemp.sql
	$(DBACCESS) $(DBNAME) cftemp.sql  	  # create functions in DB
	/bin/rm -f cftemp.sql			  # prevent hanky panky

