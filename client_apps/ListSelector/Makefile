#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Linking Authors to Publications Applet.
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

# This makefile lies about what the target dir is.  TARGETDIR is actually a
# staging dir.  FINALTARGETDIR is the real target.

TARGETDIR = $(TARGETROOT)/Staging/client_apps/ListSelector
STAGINGDIR = $(TARGETDIR)
FINALTARGETDIR = $(TARGETROOT)/client_apps/ListSelector

# ---- Misc Java variables

# Use the standard JAVAC_14, and JAVACFLAGS.  Still need to define classpath.

SRCIFMXPATH   = $(JDBCHOME)/lib
IFMXJARS      = ifxjdbc.jar ifxlsupp.jar
SRCIFMXJARS   = $(foreach SJAR,$(IFMXJARS),$(SRCIFMXPATH)/$(SJAR))
DESTIFMXJARS  = $(foreach DJAR,$(IFMXJARS),$(FINALTARGETDIR)/$(DJAR))
EMPTY         =
SPACE         = $(EMPTY) $(EMPTY)
IFMXCLASSPATH = $(subst $(SPACE),:,$(DESTIFMXJARS))

CLASSPATH = $(STAGINGDIR):$(JAVAHOME)/lib/classes.zip:$(TOP)/lib/Java:$(IFMXCLASSPATH)


# ---- Java source and destination variables

GENERICS = SQLQuery.java
STATICS  = ListItem.java ListSelector.java Selectable.java \
           SelectedPanel.java SystemsPanel.java SelectablePanel.java
JAVAS    = $(STATICS) $(GENERICS)
CLASSES  = $(foreach JAVA, $(JAVAS), $(patsubst %.java,%.class,$(JAVA)))

STAGINGJAVAS   = $(foreach JAVA, $(JAVAS), $(STAGINGDIR)/$(JAVA))
STAGINGCLASSES = $(foreach CLASS, $(CLASSES), $(STAGINGDIR)/$(CLASS))

JAR = $(FINALTARGETDIR)/listselector.jar


# ---- Define endemic targets so we can use make.default.rules.

ENDEMICTARGETS_POSTTARGETDIR = $(FINALTARGETDIR)
ENDEMICTARGETS_POSTTARGETS = $(DESTIFMXJARS) $(JAR)
ENDEMICTARGETS_CLEAN = $(STAGINGJAVAS) $(STAGINGCLASSES)


# ---------------  Production Rules  ------------------------------------

# Use default rules for most of the work

include $(TOP)/make.default.rules

$(FINALTARGETDIR) :
	$(TARGET_MKDIR) $@


# ----- Rules for compiling Java sources to class files

# When any class file needs to be remade, remake all of them.

$(STAGINGCLASSES) : $(STAGINGJAVAS)
	$(JAVAC) $(JAVACFLAGS) -classpath $(CLASSPATH) $(STAGINGJAVAS)


# ----- Rules for creating the JAR files.

$(JAR) : $(STAGINGCLASSES)
	cd $(STAGINGDIR); $(JAVAHOME)/bin/jar -cvf $(JAR) $(CLASSES)
	chmod o+r $@

$(DESTIFMXJARS) : $(FINALTARGETDIR)/% : $(SRCIFMXPATH)/%
	$(TARGET_CP) $^ $@
