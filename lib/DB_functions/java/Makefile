#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW Database functions library
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/DB_functions/java

CLASSES = BlastDBSequences.class

JAVA_UDR_SQL = BlastDBSequences.sql

JAVA_ROUTINES = BlastDBSequences.java

TARGETS= ${CLASSES} ${JARS} ${CPSQL}

JARS = BlastDBSequences.jar

CPJARS = $(foreach JAR, $(JARS), $(TARGETDIR)/$(JAR)

CPSQL = $(foreach SQL, $(JAVA_UDR_SQL), $(TARGETDIR)/$(SQL))

# ---------------  Production Rules  ------------------------------------

.SUFFIXES: .java .class .jar

.java.class: 
	 /private/apps/java1.4/bin/javac -g $<

.class.jar:
	/private/apps/java1.4/bin/jar -cf `echo $< | sed -e "s/\.class/\.jar/"` $<
	$(TARGET_CP) *.jar $(TARGETDIR)
	$(MAKESPECIFIC) $(JAVA_UDR_SQL) $(TRANSLATETABLE) $(TARGETDIR)/$(JAVA_UDR_SQL)
	$(DBACCESS) $(DBNAME) $(TARGETDIR)/$(JAVA_UDR_SQL)

all: $(TARGETS) $(CPSQL)
	rm -f *.class

include $(TOP)/make.default.rules

# ---------------  Special Rules  ---------------------------------------

postloaddb : clean clobber all
