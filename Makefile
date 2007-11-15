#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project
#
# This is the top level makefile for the ZFIN_WWW CVS project of ZFIN.
# The ZFIN_WWW project contains all files that are tied to a specific
# web site instance.  Files that are tied to a specific Informix server,
# machine, or that are truly general, are stored in other CVS projects.
#
# Invoking this makefile will remake the entire web site.  This 
# makefile uses a generic or source directory, and a specific or target
# directory.  This makefile is at the top of the generic/source directory.
# Files in the generic directory are, well, generic.  They have had all 
# references to specific databases, directories, Informix servers, and 
# machines replaced with equivalent generic tags.
#
# The specific/target directory approximately parallels the generic/source
# directory, but it contains specific versions of the files in the generic 
# tree, where the tags have been replaced with references to actual databases, 
# directories, Informix servers, and machines.
#
# The specific/target directory tree is populated by the makefiles in the 
# generic/source directory tree.  The makefiles use a TRANSLATE TABLE FILE 
# to know what specific values to replace the generic tags with.
#
#
# VARIABLES
# ---------
# Each makefile uses these environmental variables to assist it:
#
#  $INFORMIXDIR, $INFORMIXSERVER, $ONCONFIG, $INFORMIXSQLHOSTS, $PATH,
#    $LD_LIBRARY_PATH These are all assumed to point at the current
#                  	Informix server.
#  $DBNAME            Name of the database in $INFORMIXSERVER to work
#                       with.  This must agree with the database name
# 			value in the translate table file.
#  $TARGETROOT        Identifies the root directory of the directory
#                       tree where final output files go.  This is the 
#			specific version of the tree.  The makefiles 
#			reside in the generic version.  THIS MUST BE AN
#                       ABSOLUTE PATH.
#  $TARGETFTPROOT     Full path to the FTP directory for this site.  Note
#			that in production this points to a real FTP
#			directory that can be reached via anonymous FTP,
#			but that on test it only points to a directory
#			where files can be put.  THIS MUST BE AN ABSOLUTE
#			PATH.
#  $TAGETCGIBIN       Name of the cgi-bin directory to use.  This name
#			is RELATIVE to $TARGETROOT.  It typically has the
#			form cgi-bin_VirtualServerName.
#			Example: setenv $TARGETCGIBIN cgi-bin_albino
#  $TRANSLATETABLE    The file containing the translate table to use
#                       when converting generic files into their specific
#                       counterpart.  See the translate.table.template
#			file in this directory for an explanation of the
#			format and meaning of this file.  THIS MUST BE AN
#                       ABSOLUTE PATH.
#
# The makefiles also make extensive use of a common set of makefile 
# variables that are defined in the make.include file in this directory. 
# The make.include file is included in every makefile in this directory
# tree.  See make.include for a description of the variables it defines.
#
# Furthermore, each makefile also defines the values of a common set of 
# variables.  Not all variables are defined in all makefile, and sometimes
# the function of these variables will be split over many variables.
#
#  TOP		      Relative path from this directory to the root of 
#			the generic directory.  This is basically an 
#			indicator of how deep in the tree the current 
#  			directory is.  Must be defined before make.include
#			is included.
#			Example: TOP = ../../..
#  SUBDIRS            Subdirectories of the current directory that also
#			contain makefiles.
#  TARGETDIR	      Identifies the directory within $TARGETROOT where
#			the final output files produced by the makefile
#			will go.  Has the form $(TARGETROOT)/subdirectory.
#			The subdirectory part of that usually mirrors the
#			name of the subdirectory the makefile is in.
#			Example: TARGETDIR = $(TARGETROOT)/home
#
#  GENERICS 	      List of generic files that the makefile will translate
# 			into specific files
#			Example: GENERICS = classify_pubs.apg do_direct.apg ...
#  STATICS	      List of files that don't need to be translated from
#			a generic form into a specific form.  These files 
#			don't contain anything that needs translation.
#			Example STATICS = favicon.ico
#
#  SPECIFICTARGETS     List of specific versions of generic files.  Depending
#                  	upon the nature of the generic file, this may or 
#			may not be the final target files.  For app pages
#			and HTML files these are usually the final targets,
#			but for Java and C files they are intermediate files.
#			See discussion of Java and C below.
#			Example: 
#			  SPECIFICTARGETS = \
# 			    $(foreach SPEC, $(GENERICS), $(TARGETDIR)/$(SPEC))
#  STATICTARGETS      List of targets that are based on static files.
#			Example: 
#			  STATICTARGETS = \
#			    $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
#  ENDEMICTARGETS_PRE
#  ENDEMICTARGETS_POSTTARGETDIR
#  ENDEMICTARGETS_POSTTARGETS
#  ENDEMICTARGETS_POST
#                     These 4 variables are used with targets that require
#			special handling in the local makefile.  These variables
#			allow those makefiles to have special processing for
#			these targets and still use the default rules for 
#			other files.  Which of these variables a target goes
#			into determines when that target will be made in 
#			relation to the default targets.  See the default
#			rules makefiles for more.
#
#  TARGETS	      List of all targets produced by the makefile.  In other
#			words, this is the list of final output files produced
#			by the makefile.
#			Example: TARGETS = $(SPECIFICTARGETS) $(STATICTARGETS)
#
#
# DIRECTORIES WITH GENERIC C OR JAVA FILES (STAGING DIRECTORIES)
# --------------------------------------------------------------
# The STATICS/GENERICS/SPECIFICS variable naming conventions given above 
# work well for directories where none of the files need to go through a 
# compiler to produce the final target.  In such directories, the makefile
# typically calls MAKESPECIFIC, giving it the generic file in the generic
# directory, and telling it to put the specific output file in the target
# directory.  There are only 2 files (generic, specific) in the process and
# it is clear where each goes.
# 
# The Problem: 
# This model breaks down with C or Java files that contain generic tags.
# The first step in this situation is to make a specific version of the 
# generic source file.  However, the specific version of the source file
# is not the final target.  It still needs to be compiled (and possibly 
# linked) before it can be used.  We can't put the specific source file
# in the same directory as the generic source file because then it would
# overwrite the generic version.  If we gave the specific version of the file
# a different name the we could keep it in the same directory.  However, the
# temptation to edit the specific version would be great, and this gets 
# problematic and confusing with Java files where the filename is supposed
# to correspond with the class either name.  
# 
# The Solution:
# Each directory that contains generic C or Java files has a subdirectory 
# named Staging hung off of it.  The parent directory and the Staging 
# directory both have Makefiles in them.  The makefile in the parent 
# directory populates the Staging directory with specific versions of 
# all generic sources, and also copies all static files into the Staging
# directory. The parent makefile then calls the Staging makefile to 
# actually produce the final targets.  The Staging makefile is set up 
# so that it inherits several variables from the parent makefile.  The
# Staging makefile can also only be invoked through the parent makefile
# With this setup the parent makefile knows about static, generic, and
# specific files, but the Staging makefile just sees one type of file.
#
#
# TARGETS
# -------
# Finally, there is also a set of targets that are common to most or all 
# makefiles.  Invoking make with any of these targets will invoke the target
# recursively for all subdirectories below it.
# 
#  all		      The default target.  Produces the output of this
# 			directory and all its subdirectories.
#  clean	      Remove intermediate, temporary, and working files
# 			in this directory and all subdirectories.
#  clobber	      Removes the target files produced by the makefiles,
#			in this directory and all subdirectories.
#			It basically removes all files from target directories,
#			but does not remove the target directories themselves.
#			Note, however, that this does not delete app pages
#			from the database.
#  sanitycheck        Performs a sanity check on files in the directory and
#			all its subdirectories.  What this means varies with 
#			the directory and type of files.  Sanity checks may 
#			include 
#			o check STATIC files for presence of generic tags
#			o check STATIC & GENERIC files for presence of specific
#			  values that should be replaced with generic tags
#			o check app page source files against what is actually
#			  loaded into the database.
#			These sanity checks are in addition to the ones in
#			make.include that are always performed by every 
#			makefile.
# 
# In addition, there are several targets that occur in this makefile and in
# only a few other makefiles below it.  These do not propagate throughout the
# whole tree but only to portions of it.
#
#  mirror	      This target is used to create a web page hierarchy that
# 			is then copied to the mirror sites.  This web page
#			hierarchy contains only the static web pages.
#			This target is present only in high level makefiles
#			where some, but not all, of the subdirectories are
#			going in to the mirror.
#  postloaddb	      Should be invoked after calling loaddb.pl to load a
#			database into an already existing development 
#			environment.  Here's why:  loaddb.pl loads everything,
#			or almost everything, into the DB.  This includes
#			 o webpages table
#			 o execweb table
#			 o all SQL functions
#			 o all external (e.g. zextend.c) functions
#                       After a load all of these things effectively point
#			back to the database that the data was originally 
#			unloaded from, usually production.
#			Making the postloaddb target causes all of these 
#			things to be reloaded from the local DB.
#  start	      Start processes.  Starts up any processes that need
#			to be running for the environment to work.  The 
#			database server and apache are not controlled by 
#			this.  As of 2003/03, this doesn't start anything.
#  stop		      Stop processes.  Stops any processes started by the 
#			start target.
#


# ---------------  Variable Definitions  --------------------------------

TOP = .
include $(TOP)/make.include

SUBDIRS = lib client_apps home server_apps cgi-bin j2ee 
POSTLOADDB_SUBDIRS = home server_apps lib


# If we are building a mirror then disable calling gmake with no target.

ifeq ($(INFORMIXSERVER),bogus.informix.server.for.mirrors)
InvokingMirrorWithoutTarget:
	ERROR: Reinvoke gmake with a mirror target.
endif




# use the default set of rules

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------


# Make the directories needed for the mirror

mirror : 
	$(MAKE) -C server_apps $@
	$(MAKE) -C home $@

postloaddb :
	$(foreach PLDIR,$(POSTLOADDB_SUBDIRS), $(MAKE) -C $(PLDIR) $@; )

start stop :
	$(MAKE) -C server_apps $@
