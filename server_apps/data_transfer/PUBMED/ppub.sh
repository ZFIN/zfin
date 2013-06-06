#!/bin/sh

cd <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/
updatepubstatus.pl && <!--|TARGETROOT|-->/cgi-bin/publications_today.cgi

