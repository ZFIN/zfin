#!/usr/bin/python

import psycopg2
import os
import glob
from shutil import copy
import re

hostname = 'localhost'
database = 'swrdb'


def do_query(conn):
    cur = conn.cursor()

    cur.execute("SELECT distinct img_zdb_id, fig_source_zdb_id "
                "     FROM figure, image WHERE fig_zdb_id = img_fig_zdb_id")

    for img_id, fig_source_id in cur.fetchall():

        m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)$", fig_source_id)
        if m:
            year = m.group(2)
            month = m.group(3)
            day = m.group(4)

            if year.startswith('9'):
                year = "19" + year
            else:
                year = "20" + year

        fullPathPDFDir = os.environ['LOADUP_FULL_PATH']+fig_source_id
        if os.path.isdir(fullPathPDFDir):
            print fullPathPDFDir
            pattern = "/research/zcentral/loadUp/imageLoadUp/" + img_id + "*"

            for imgFile in glob.glob(pattern):
                 print imgFile
                 copy(imgFile, fullPathPDFDir)

            pattern = "/research/zcentral/loadUp/imageLoadUp/medium" + img_id + "*"

            for imgFile in glob.glob(pattern):
                 print imgFile
                 copy(imgFile, fullPathPDFDir)

        else:
            print "cant file dir: " + fullPathPDFDir
            os.mkdir(fullPathPDFDir)
            pattern = '/research/zcentral/loadUp/imageLoadUp/' + img_id + '*'

            for imgFile in glob.glob(pattern):
                 print imgFile
                 copy(imgFile, fullPathPDFDir)

            pattern = "/research/zcentral/loadUp/imageLoadUp/medium" + img_id + "*"

            for imgFile in glob.glob(pattern):
                 print imgFile
                 copy(imgFile, fullPathPDFDir)

myConnection = psycopg2.connect(host=hostname, dbname=database)
do_query(myConnection)
myConnection.close()




# pdf_dir = '<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->'

# for f in os.listdir(pdf_dir):
#     m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)(\.pdf)$", f)
#     if m:
#         year = m.group(2)
#         month = m.group(3)
#         day = m.group(4)
#
#         if year.startswith('9'):
#             year = "19" + year
#         else:
#             year = "20" + year
#
#         src_path = os.path.join(pdf_dir, f)
#         year_dir = os.path.join(pdf_dir, year)
#         dst_path = os.path.join(year_dir, f)
#
#         if os.path.isfile(dst_path):
#             continue
#
#         if not os.path.isdir(year_dir):
#             os.mkdir(year_dir)
#
#         print src_path
#         shutil.copy(src_path, year_dir)
