#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps sysexecs image_stats
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This builds the get_image_stats.pl script.  Whenever app pages need to 
# know the dimensions of an image contained in a file, the call 
# 'get_image_status' using the sysexec SQL funtion, 
# 'get_image_stats' is the key of the execweb table in the database.  Execweb
# contains two columns, the key, and a path for that key.  The path indicates
# where the executable that corresponds to that key can be found.  That
# executable is then executed.
#
# This makefile creates the get_image_stats.pl script and updates the 
# row for get_image_stats in the execweb table as well.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs/image_stats

# The script is static.  It is the only file in this directory.

STATICS = get_image_stats.pl

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS = $(SPECIFICTARGETS) $(STATICTARGETS)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly reloadrow clobberrow 


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
	echo 'insert into execweb (execweb_id, execweb_executable) values("get_image_stats","$(TARGETDIR)/get_image_stats.pl")' | $(DBACCESS) $(DBNAME)


clobberrow :
	echo 'delete from execweb where execweb_id = "get_image_stats"' | $(DBACCESS) $(DBNAME)


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
