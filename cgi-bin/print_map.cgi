#!/bin/sh
cd /research/zfin/chromix/www/home/ZFIN_software/mapplet/release

#setenv DISPLAY="chromix.cs.uoregon.edu:1"
#unset DISPLAY
DISPLAY=chromix.cs.uoregon.edu:1.0
export DISPLAY
java \
    -mx50m \
    -Dcgi.content_type=$CONTENT_TYPE \
    -Dcgi.content_length=$CONTENT_LENGTH \
    -Dcgi.request_method=$REQUEST_METHOD \
    -Dcgi.query_string=$QUERY_STRING \
    -Dcgi.server_name=$SERVER_NAME \
    -Dcgi.server_port=$SERVER_PORT \
    -Dcgi.script_name=$SCRIPT_NAME \
    -Dcgi.path_info=$PATH_INFO \
    -classpath .:/research/zfin/chromix/www/home/ZFIN_software/mapplet/release/mapplet.jar  \
    mapimage 
