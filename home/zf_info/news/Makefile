#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Zebrafish News directory.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/news

# Subdirectories come and go as meetings come and go.


# List permanent files first -- files that will stay around no matter what
# announcements come and go.  Follow that list with files that will disappear
# when the meetings or jobs they go with will disappear.

VARIABLE_GENERICS =  curator.html

GENERICS = siteNews_2005.html usersurvey2005.html  $(VARIABLE_GENERICS)

STATICS =  Newsletter_Summer04.pdf Madison_2004_poster.jpg RegistrationForm.pdf newsgroup.html \
 funding_opps.html ISDB2005.pdf Newsletter_Winter05.pdf Newsletters.html Newsletter_Summer05.pdf \
 Newsletter_Winter06.pdf Newsletter_Summer2006.pdf ANZZFWksp2007RegForm.doc ZFIN_news_Spring_07.pdf \
 ZNEWS_Spring_08.pdf ZF_Madisonv3.pdf siteNews_2006.html ZFIN_jobs.html \
 jobs.html mtgs.html siteNews.html


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules

# path to the rss generators
GENERATOR = $(TARGETROOT)/server_apps/WebSiteTools


# RSS feed pages


jobs_test : jobs.html
	echo "Scan for funny chars 'Error 1' if none"
	egrep -n "[^ -~	]" jobs.html
jobs_tidy : jobs.html
	tidy -eq -ascii jobs.html
jobs_rss : jobs.html
	$(GENERATOR)/gen-jobs-rss.r $(TARGETDIR)

meetings_test : mtgs.html
	echo "Scan for funny chars 'Error 1' if none"
	egrep -n "[^ -~	]" mtgs.html
meetings_tidy : mtgs.html
	tidy -eq -ascii  mtgs.html
meetings_rss : mtgs.html
	$(GENERATOR)/gen-meetings-rss.r $(TARGETDIR)


news_test : siteNews.html
	echo "Scan for funny chars 'Error 1' if none"
	egrep -n "[^ -~	]" siteNews.html
news_tidy : siteNews.html
	tidy -eq -ascii  siteNews.html
news_rss : siteNews.html
	$(GENERATOR)/gen-news-rss.r $(TARGETDIR)
