find . -name \*.java | xargs svn ps svn:eol-style native

svn ps -R svn:eol-style native source

svn ps -R svn:eol-style native source test/org/zfin  home/WEB-INF/jsp home/WEB-INF/tags home/WEB-INF/tiles home/WEB-INF/*.xml home/WEB-INF/conf home/WEB-INF/spring home/WEB-INF/tld home/WEB-INF/validation home/ZFIN/APP_PAGES  commons/env
