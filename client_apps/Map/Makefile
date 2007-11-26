#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Genomic Map Viewer Applet.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile has two target dirs.  The first is actually a staging dir
# where the work is done.  The second is the final target dir where the
# JAR gets put.


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/Staging/client_apps/Map
STAGINGDIR = $(TARGETDIR)
STAGINGZMAPPER = $(STAGINGDIR)/zmapper
FINALTARGETDIR = $(TARGETROOT)/client_apps/Map

IMAGES = mapkey.gif
TARGETIMAGES = $(foreach IMG, $(IMAGES), $(FINALTARGETDIR)/$(IMG))


# ---- Misc Java varaibles

# Use JAVAC14 & JAVAHOME14 because it's an applet and we want clients to be able to
# run it with older java plugins.  Still need to define classpath.

CLASSPATH = $(STAGINGDIR):$(JAVAHOME14)/lib/classes.zip:$(TOP)/lib/Java

IMPORTPATH = $(TARGETROOT)/lib/Java
NETSCAPEPATH = $(IMPORTPATH)/netscape
NETSCAPESYMLINK = $(STAGINGDIR)/netscape


# ---- Java source and destination variables

GENERICS = zmapper/MarkerBuilder.java
STATICS  = mapplet.java \
           zmapper/BackBone.java zmapper/Bin.java zmapper/BinGroup.java \
           zmapper/LayoutManager.java zmapper/MapViewer.java zmapper/Marker.java
JAVAS    = $(GENERICS) $(STATICS)
CLASSES  = $(foreach JAVA, $(JAVAS), $(patsubst %.java,%.class,$(JAVA)))

STAGINGJAVAS   = $(foreach JAVA, $(JAVAS), $(STAGINGDIR)/$(JAVA))
STAGINGCLASSES = $(foreach CLASS, $(CLASSES), $(STAGINGDIR)/$(CLASS))

JAR = $(FINALTARGETDIR)/mapplet-1.0.jar


# ---- Define endemic targets so we can use make.default.rules.

ENDEMICTARGETS_POSTTARGETDIR = $(NETSCAPESYMLINK) $(STAGINGZMAPPER) $(FINALTARGETDIR)
ENDEMICTARGETS_POSTTARGETS = $(JAR) $(TARGETIMAGES)
ENDEMICTARGETS_CLEAN = $(STAGINGJAVAS) $(STAGINGCLASSES)


# ---------------  Production Rules  ------------------------------------

# Use default rules for most of the work

include $(TOP)/make.default.rules

$(FINALTARGETDIR) $(STAGINGZMAPPER) :
	$(TARGET_MKDIR) $@

$(TARGETIMAGES) : $(FINALTARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@


# ----- Rules for compiling Java sources to class files

# When any class file needs to be remade, remake all of them.

$(STAGINGCLASSES) : $(STAGINGJAVAS)
	$(JAVAC14) $(JAVACFLAGS) -classpath $(CLASSPATH) $(STAGINGJAVAS)


# ----- Rules for creating the JAR file.

$(NETSCAPESYMLINK) :
	ln -s $(NETSCAPEPATH) $(NETSCAPESYMLINK)

$(JAR) : $(STAGINGCLASSES) $(NETSCAPESYMLINK)
	cd $(STAGINGDIR); $(JAVAHOME)/bin/jar -cvf $(JAR) $(CLASSES) netscape
	chmod o+r $@
