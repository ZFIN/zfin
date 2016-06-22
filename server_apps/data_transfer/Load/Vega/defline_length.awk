#!/bin/awk -f 

$0 ~ ">" {print c; c=0;printf substr($0,2,300) "|"; } $0 !~ ">" {c+=length($0);} END { print c; }