#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, C external database functions
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile is a little complicated.  It needs to be in order to
# avoid hanging the database.  Here's what it does:
# For each altered function:
#  1. Create a local copy of the function's library.
#  2. Drop function in that library from the database.
#  3. Copy the target library to where it will be read by the server.
#  4. Define the function in that library to the database.
#
# Combining any of those steps may cause the database to hang.  In particular
# if you copy a new library over an old while informix is using a function
# in that library, very bad things will happen.
#
# To add a new function, or multiple functions in a single file, to this
# directory you will need to do at least the following.
#
#  o put source code in a single .c file.  Header files are OK, but if you
#    need to put code in multiple .c files then you shouldn't use this
#    makefile.
#  o Create drop_xxxx.sql and create_xxxx.sql files to create and drop the
#    new functions in the database.  "xxxx" in the filenames MUST be the
#    same as the name of the .c file.  For example, the files for get_id.c
#    are create_get_id.sql and drop_get_id.sql.
#  o Add the new .c file to SRCS below.

# ---------------  Variable Definitions  --------------------------------


TOP = ../../..
include $(TOP)/make.include

# Note that sibling directories to this one will have the same target
# directory.

TARGETDIR = $(TARGETROOT)/lib/DB_functions

SRCS = get_id.c get_obj_type.c zero_pad.c get_time.c scrub_char.c \
	zero_pad_int.c xdbget.c xdbput.c
OBJS = $(foreach SRC, $(SRCS), $(patsubst %.c,%.so,$(SRC)))
TARGETDIR_OBJS = $(foreach OBJ, $(OBJS), $(patsubst %,$(TARGETDIR)/%,$(OBJ)))
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
	/bin/chmod o+x,o+r $@

$(TARGETDIR_OBJS) : $(TARGETDIR)/% : %
	-$(DBACCESS) $(DBNAME) $(patsubst %.so,drop_%.sql,$^)  > /dev/null 2>&1
	$(TARGET_CP) $^ $@ 			 # put it where DB will find it
	$(TOUCH) $@
	$(MAKESPECIFIC) $(patsubst %.so,create_%.sql,$^) $(TRANSLATETABLE) tempcreate.sql
	$(DBACCESS) $(DBNAME) tempcreate.sql  	  # create function in DB
	/bin/rm -f tempcreate.sql		  # prevent hanky panky

