#!/bin/bash

[ -a 'logs' ] || mkdir logs ; 

THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
