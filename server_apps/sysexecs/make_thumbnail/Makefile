#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps sysexecs make_thumbnail
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This builds the make_thumbnail.sh executable.  Whenever app pages need to 
# make a thumbnail, they call 'make_thumbnail' using the sysexec SQL funtion, 
# which is currently (2000/12) defined in lib/DB_functions/zextend/zextend.c.
# 'make_thumbnail' is the key of the execweb table in the database.  Execweb
# contains two columns, the key, and a path for that key.  The path indicates
# where the executable that corresponds to that key can be found.  That
# executable is then executed.
#
# This makefile creates the make_thumbnail.sh executable and updates the 
# row for make_thumbnail in the execweb table as well.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs/make_thumbnail

# The script is static.  It is the only file in this directory.

STATICS = make_thumbnail.sh

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS = $(SPECIFICTARGETS) $(STATICTARGETS)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck reloadrow clobberrow clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

# The row in execweb points to the script.  Everytime we remake the script
# also reload the row in execweb.

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $< $@
	$(TOUCH) $@
	$(MAKE) reloadrow

reloadrow : clobberrow
	echo 'insert into execweb (execweb_id, execweb_executable) values("make_thumbnail","$(TARGETDIR)/make_thumbnail.sh")' | $(DBACCESS) $(DBNAME)


clobberrow :
	echo 'delete from execweb where execweb_id = "make_thumbnail"' | $(DBACCESS) $(DBNAME)


# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files or subdirs


# In addition to removing the targets (but not the targetdir), 
# clobber also removes the row for encryptpass from the execweb table

clobber : clobberrow		# Remove targets, but not target dir.
	rm -f $(TARGETS)

sanitycheck :			# Maybe other stuff to do here as well.
	$(SPECIFICCHECK) $(GENERICS)


# define one time only target, for when something special needs to be done
# only once.  

onetimeonly :
	$(ONETIMEONLY)



# ---------------  Misc Targets  ----------------------------------------
