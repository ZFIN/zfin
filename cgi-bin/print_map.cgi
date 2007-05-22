#!/bin/sh
cd <!--|ROOT_PATH|-->/server_apps/mapimage/

#setenv DISPLAY="localhost:1"
#unset DISPLAY
DISPLAY=localhost:1.0
export DISPLAY
#set QUERY_STRING = `echo $QUERY_STRING | tr '%0D' '%0A'`
/private/apps/java/bin/java \
    -mx200m \
    -Dcgi.content_type=$CONTENT_TYPE \
    -Dcgi.content_length=$CONTENT_LENGTH \
    -Dcgi.request_method=$REQUEST_METHOD \
    -Dcgi.query_string=$QUERY_STRING \
    -Dcgi.server_name=$SERVER_NAME \
    -Dcgi.server_port=$SERVER_PORT \
    -Dcgi.script_name=$SCRIPT_NAME \
    -Dcgi.path_info=$PATH_INFO \
    -classpath .:<!--|ROOT_PATH|-->/server_apps/mapimage/mapplet-1.0.jar:<!--|ROOT_PATH|-->/lib/Java:/private/apps/jdbc3/lib/ifxjdbc.jar  \
    mapimage 

