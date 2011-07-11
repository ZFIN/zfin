#!/bin/sh
dbaccess -a $DBNAME removeTraceViewBlast.sql ;
dbaccess -a $DBNAME add_display_group.sql ;
dbaccess -a $DBNAME fix_marker_relationship_type.sql ;
dbaccess -a $DBNAME updateGenox.sql ;
