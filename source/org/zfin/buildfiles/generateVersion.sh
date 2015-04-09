#!/bin/sh
SVN_REVISION_STRING=`svn info | grep Revision`
SVN_REVISION=`expr "$SVN_REVISION_STRING" : 'Revision: \(.*\)'`;
SVN_URL_STRING=`svn info | grep URL`
SVN_URL=`expr "$SVN_URL_STRING" : '.*ZFIN_WWW\/\(.*\)'`;
EXPORT_FILE="$1" ; 
TITLE=`expr "$EXPORT_FILE" : '.*home\/\(.*\)'`;
echo "<html><head><title>Deploy Version $1</title></head><body>" > $EXPORT_FILE ;  
echo "<h1>$TITLE</h1>" >> $EXPORT_FILE ; 
echo "<ul>" >> $EXPORT_FILE ;

echo "<li>TIME: `date`" >> $EXPORT_FILE  ; 
echo "<li>REVISION: $SVN_REVISION" >> $EXPORT_FILE  ; 
echo "<li>BRANCH: $SVN_URL" >> $EXPORT_FILE  ; 
echo "<li>DOMAIN_NAME: $DOMAIN_NAME" >> $EXPORT_FILE  ;
echo "<li>INFORMIXSERVER: $INFORMIXSERVER" >> $EXPORT_FILE  ;

echo "</ul>" >> $EXPORT_FILE ;
echo "<a href="/java-deploy-version.html">[java-deploy-version.html]</a>" >> $EXPORT_FILE ;
echo "<a href="/gmake-deploy-version.html">[gmake-deploy-version.html]</a>" >> $EXPORT_FILE ;
echo "<a href="/action/dev-tools/svn-version">[in-java-deploy-version]</a>" >> $EXPORT_FILE ; 
echo "</body></html>" >> $EXPORT_FILE ;
