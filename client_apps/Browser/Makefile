#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Anatomical disctionary browser Applet.
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

TARGETDIR = $(TARGETROOT)/Staging/client_apps/Browser
STAGINGDIR = $(TARGETDIR)
FINALTARGETDIR = $(TARGETROOT)/client_apps/Browser

# ---- Misc Java variables

# Use the standard JAVAC_11, and JAVACFLAGS.  Still need to define classpath.

CLASSPATH = $(STAGINGDIR):$(JAVAHOME_11)/lib/classes.zip:$(TOP)/lib/Java:$(JDBCHOME)/lib/ifxjdbc.jar

IMPORTPATH = $(TARGETROOT)/lib/Java
NETSCAPEPATH = $(IMPORTPATH)/netscape
NETSCAPESYMLINK = $(STAGINGDIR)/netscape


# ---- Java source and destination variables

GENERICS = SQLQuery.java Browser.java
STATICS  = OrganTree.java Selectable.java \
	   SelectedPanel.java SystemsPanel.java TreeNode.java TreePanel.java \
	   data_gatherer.java
JAVAS    = $(STATICS) $(GENERICS)
CLASSES  = $(foreach JAVA, $(JAVAS), $(patsubst %.java,%.class,$(JAVA)))

STAGINGJAVAS     = $(foreach JAVA, $(JAVAS), $(STAGINGDIR)/$(JAVA))
STAGINGCLASSES   = $(foreach CLASS, $(CLASSES), $(STAGINGDIR)/$(CLASS))

JAR = $(FINALTARGETDIR)/browser.jar


# ---- Define endemic targets so we can use make.default.rules.

ENDEMICTARGETS_POSTTARGETDIR = $(NETSCAPESYMLINK) $(FINALTARGETDIR)
ENDEMICTARGETS_POSTTARGETS = $(JAR)
ENDEMICTARGETS_CLEAN = $(STAGINGJAVAS) $(STAGINGCLASSES)


# ---------------  Production Rules  ------------------------------------

# Use default rules for most of the work

include $(TOP)/make.default.rules

$(FINALTARGETDIR) :
	$(TARGET_MKDIR) $@


# ----- Rules for compiling Java sources to class files

# When any class file needs to be remade, remake all of them.

$(STAGINGCLASSES) : $(STAGINGJAVAS)
	$(JAVAC_11) $(JAVACFLAGS) -classpath $(CLASSPATH) $(STAGINGJAVAS)


# ----- Rules for creating the JAR file.

$(NETSCAPESYMLINK) :
	ln -s $(NETSCAPEPATH) $(NETSCAPESYMLINK)

$(JAR) : $(STAGINGCLASSES) $(NETSCAPESYMLINK)
	cd $(STAGINGDIR); $(JAVAHOME_11)/bin/jar -cvf $(JAR) $(CLASSES) netscape
	chmod o+r $@
