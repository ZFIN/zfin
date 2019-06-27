#!/usr/bin/python

import psycopg2
import os
import glob
from shutil import copy
import re

hostname = 'localhost'
database = os.environ.get('DBNAME')


def do_query(conn):
    cur = conn.cursor()

    cur.execute("SELECT distinct pf_file_name, pf_pub_zdb_id"
                "     FROM publication_file"
                "     ORDER BY pf_file_name"
                )

    for pf_file_name, pf_pub_zdb_id in cur.fetchall():

        m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)$", pf_pub_zdb_id)
        if m:
            year = m.group(2)
            if year.startswith('9'):
                year = "19" + year
            else:
                year = "20" + year

        yearDir = os.environ['LOADUP_FULL_PATH'] + "/" + year + "/"
        fullPathPDFDir = os.environ['LOADUP_FULL_PATH'] + "/" + year + "/" + pf_pub_zdb_id

        if os.path.isdir(fullPathPDFDir) and not os.path.exists(fullPathPDFDir + "/" + pf_file_name):
            movepdfs(fullPathPDFDir, pf_pub_zdb_id, year)

        else:
            print "cant find file dir: " + fullPathPDFDir
            if os.path.isdir(yearDir):
                os.mkdir(fullPathPDFDir)
                movepdfs(fullPathPDFDir, pf_pub_zdb_id, year)

            else:
                os.mkdir(yearDir)
                os.mkdir(fullPathPDFDir)
                movepdfs(fullPathPDFDir, pf_pub_zdb_id, year)


def movepdfs(fullPathPDFDir, pf_pub_zdb_id, yearDirectory):
    pattern = '/research/zcentral/loadUp/PDFLoadUp/' + yearDirectory + '/' + pf_pub_zdb_id + '*'

    for pubFile in glob.glob(pattern):
        print(pubFile)
        copy(pubFile, fullPathPDFDir)


myConnection = psycopg2.connect(host=hostname, dbname=database)
do_query(myConnection)
myConnection.close()
