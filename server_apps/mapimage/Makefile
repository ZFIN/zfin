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

# TARGETDIR is handled by Staging directory

TARGETDIR = $(TARGETROOT)/Staging/server_apps/mapimage
STAGINGDIR = $(TARGETDIR)
STAGINGZMDIR = $(STAGINGDIR)/zmapper
FINALTARGETDIR = $(TARGETROOT)/server_apps/mapimage

IMAGES = mapkey.gif
TARGETIMAGES = $(foreach IMG, $(IMAGES), $(FINALTARGETDIR)/$(IMG))


# ---- Misc Java varaibles

# Use the standard JAVAC, and JAVACFLAGS.  Still need to define classpath.

CLASSPATH = $(STAGINGDIR):$(TOP)/lib/Java/Acme.jar:$(JDBCHOME)/lib/ifxjdbc.jar

ZMSRCDIR = ../../client_apps/Map/zmapper
NETSCAPEPATH = $(TARGETROOT)/lib/Java/netscape
NETSCAPESYMLINK = $(STAGINGDIR)/netscape


# ---- Java source and destination variables

# Can't use just GENERICS and STATICS b/c of zmapper complications.

STATICS    = mapimage.java cgi_lib.java
GENERICS   = SQLQuery.java
ZMGENERICS =
ZMSTATICS  = zmapper/BackBone.java zmapper/Bin.java zmapper/BinGroup.java \
	zmapper/LayoutManager.java zmapper/MapViewer.java zmapper/Marker.java \
	zmapper/MarkerBuilder.java
ZMSTAGINGSPECIFICS = $(foreach ZMG, $(ZMGENERICS), $(STAGINGDIR)/$(ZMG))
ZMSTAGINGSTATICS   = $(foreach ZMS, $(ZMSTATICS), $(STAGINGDIR)/$(ZMS))

JAVAS          = $(STATICS) $(GENERICS) $(ZMGENERICS) $(ZMSTATICS)
CLASSES        = $(foreach JAVA, $(JAVAS), $(patsubst %.java,%.class,$(JAVA)))
STAGINGJAVAS   = $(foreach JAVA, $(STATICS), $(STAGINGDIR)/$(JAVA)) \
	         $(ZMSTAGINGSPECIFICS) $(ZMSTAGINGSTATICS)
STAGINGCLASSES = $(foreach CLASS, $(STAGINGJAVAS), $(patsubst %.java,%.class,$(CLASS)))

JAR = $(FINALTARGETDIR)/mapplet-1.0.jar


# ---- Define endemic targets so we can use make.default.rules.

ENDEMICTARGETS_POSTTARGETDIR = $(NETSCAPESYMLINK) $(STAGINGZMDIR) $(FINALTARGETDIR)
ENDEMICTARGETS_POSTTARGETS = $(JAR) $(TARGETIMAGES)
ENDEMICTARGETS_CLEAN = $(STAGINGJAVAS) $(STAGINGCLASSES)


# ---------------  Production Rules  ------------------------------------

# Use default rules for most of the work

include $(TOP)/make.default.rules

$(FINALTARGETDIR) $(STAGINGZMDIR) :
	$(TARGET_MKDIR) $@

$(TARGETIMAGES) : $(FINALTARGETDIR)/% : %
	$(TARGET_CP) $^ $@
	$(TOUCH) $@


# ----- Rules for compiling Java sources to class files

$(ZMSTAGINGSPECIFICS) : $(STAGINGZMDIR)/% : $(ZMSRCDIR)/%
	$(MAKESPECIFIC) $< $(TRANSLATETABLE) $@

$(ZMSTAGINGSTATICS) : $(STAGINGZMDIR)/% : $(ZMSRCDIR)/%
	$(TARGET_CP) $^ $@
	$(TOUCH) $@

# When any class file needs to be remade, remake all of them.

$(STAGINGCLASSES) : $(STAGINGJAVAS)
	$(JAVAC) $(JAVACFLAGS) -classpath $(CLASSPATH) $(STAGINGJAVAS)


# ----- Rules for creating the JAR file.

$(NETSCAPESYMLINK) :
	rm -f $(NETSCAPESYMLINK)
	ln -s $(NETSCAPEPATH) $(NETSCAPESYMLINK)

$(JAR) : $(STAGINGCLASSES) $(NETSCAPESYMLINK)
	rm -rf $(STAGINGDIR)/netscape
	cp -rf $(TOP)/lib/Java/netscape $(STAGINGDIR)/netscape
	cd $(STAGINGDIR); $(JAVAHOME)/bin/jar -cvf $(JAR) $(CLASSES) netscape
	chmod o+r $@
