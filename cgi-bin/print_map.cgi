#!/bin/sh
cd <!--|ROOT_PATH|-->/home/client_apps/Map/

#setenv DISPLAY="<!--|DOMAIN_NAME|-->:1"
#unset DISPLAY
DISPLAY=<!--|DOMAIN_NAME|-->:1.0
export DISPLAY
java \
    -mx100m \
    -Dcgi.content_type=$CONTENT_TYPE \
    -Dcgi.content_length=$CONTENT_LENGTH \
    -Dcgi.request_method=$REQUEST_METHOD \
    -Dcgi.query_string=$QUERY_STRING \
    -Dcgi.server_name=$SERVER_NAME \
    -Dcgi.server_port=$SERVER_PORT \
    -Dcgi.script_name=$SCRIPT_NAME \
    -Dcgi.path_info=$PATH_INFO \
    -classpath .:<!--|ROOT_PATH|-->/home/client_apps/Map/mapplet-1.0.jar:<!--|ROOT_PATH|-->/lib/Java  \
    mapimage 
