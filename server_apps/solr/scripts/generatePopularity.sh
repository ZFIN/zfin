#!/bin/sh
./getIdListFromApacheLog.sh | sort | uniq -c | ./getScoresFromIdList.py > popularity.txt

