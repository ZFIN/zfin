#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Java routines that are callable from
# SQL.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
# that is in that library, very bad things can happen.
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR  = $(TARGETROOT)/lib/DB_functions/Java

# STATICSQLS  = drop_functions.sql
# GENERICSQLS = create_functions.sql

# ---- Java variables

JAVAS     = Zeropad.java
CLASSES   = $(foreach JVS, $(JAVAS), $(patsubst %.java,%.class,$(JVS)))
JAR       = zeropad.jar
TARGETJAR = $(TARGETDIR)/$(JAR)

# Use the standard JAVAC, and JAVACFLAGS.  Still need to define classpath.

CLASSPATH = .:$(JAVAHOME)/lib/classes.zip:$(INFORMIXDIR)/extend/krakatoa/krakatoa.jar:$(TOP)/lib/Java


TARGETS = $(TARGETJAR)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly


all : $(TARGETDIR) $(TARGETS) 

$(TARGETDIR) :
	$(TARGET_MKDIR) $@


# When any class file needs to be remade, remake all of them.

$(CLASSES) : $(JAVAS)
	$(JAVAC) $(JAVACFLAGS) -classpath $(CLASSPATH) $(JAVAS)

$(JAR) : $(CLASSES)
	$(JAVAHOME)/bin/jar -cf $(JAR) $(CLASSES)
	chmod o+r $(JAR)

# copy it to final destination and then load it into DB.

$(TARGETJAR) : $(JAR)
	-$(DBACCESS) $(DBNAME) drop_functions.sql # drop functions in DB
	$(TARGET_CP) $^ $@			  # put where DB will find it
	$(MAKESPECIFIC) create_functions.sql $(TRANSLATETABLE) cftemp.sql
	$(DBACCESS) $(DBNAME) cftemp.sql	  # create functions in DB
	rm -f cftemp.sql			  # prevent hanky panky


# ---------------  Maintenance Rules  -----------------------------------

clean :
	rm -rf $(CLASSES) $(JAR)


clobber :	# Remove targets, but not target dir.
	rm -rf $(TARGETS) 


# check generics & possibly statics as well for values that should
# be represented with tags instead of hard coded values

sanitycheck : 
	$(SPECIFICCHECK) $(JAVAS)


# define one time only target

onetimeonly :
	$(ONETIMEONLY)
