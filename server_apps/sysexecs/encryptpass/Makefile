#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Server Apps sysexecs encryptpass
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This builds the encryptpass executable.  Whenever app pages need to 
# encrypt a password they call encryptpass using the sysexec SQL funtion, 
# which is currently (2000/12) defined in lib/DB_functions/zextend/zextend.c.
# That function is passed a string ('encryptpass') that uniquely identifies 
# the encryptpass executablet within ZFIN.  The sysexec function uses that 
# string to lookup a row in the execweb table in the database.  It gets the 
# full path of the executable from that row.
#
# This makefile creates the encryptpass executable and updates the row for
# encryptpas in the execweb table as well.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs/encryptpass

# The script is generic.  The C program is static.  Therefore it can be 
# made in this directory.  It is also a simple, 1 program file so there are
# no object files either.

GENERICS = encryptpass

CSRCS = encode.c

SPECIFICTARGETS = $(foreach SPEC, $(GENERICS), $(TARGETDIR)/$(SPEC))
ENCODETARGET = $(TARGETDIR)/encode

TARGETS = $(SPECIFICTARGETS) $(ENCODETARGET)



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck reloadrow clobberrow clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS)


$(TARGETDIR) :
	$(TARGET_MKDIR) $(TARGETDIR);

# The row in execweb points to the script.  Everytime we remake the script
# also reload the row in execweb.

$(SPECIFICTARGETS) : $(TARGETDIR)/% : %
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@
	/bin/chmod 751 $@
	$(MAKE) reloadrow

reloadrow : clobberrow
	echo 'insert into execweb (execweb_id, execweb_executable) values("encryptpass","$(TARGETDIR)/encryptpass")' | $(DBACCESS) $(DBNAME)


clobberrow :
	echo 'delete from execweb where execweb_id = "encryptpass"' | $(DBACCESS) $(DBNAME)


$(ENCODETARGET) : $(CSRCS)
	/local/bin/gcc $(CSRCS) -o $(ENCODETARGET)
	/bin/chmod 751 $(ENCODETARGET)

# ---------------  Maintenance Rules  -----------------------------------

clean :				# No local intermediate files or subdirs


# In addition to removing the targets (but not the targetdir), 
# clobber also removes the row for encryptpass from the execweb table

clobber : clobberrow		# Remove targets, but not target dir.
	/bin/rm -f $(TARGETS)

sanitycheck :			# Maybe other stuff to do here as well.
	$(SPECIFICCHECK) $(GENERICS)


# define one time only target, for when something special needs to be done
# only once.  

onetimeonly :
	$(ONETIMEONLY)



# ---------------  Misc Targets  ----------------------------------------
