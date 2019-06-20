#!/usr/bin/python

import psycopg2
import os
import glob
from shutil import copy
import re
import codecs

hostname = 'localhost'
database = os.environ.get('DBNAME')
if os.path.exists("updateImagePaths.txt"):
    print "removing file"
    os.remove("updateImagePaths.txt")
file = codecs.open("updateImagePaths.txt", "w")


def do_query(conn):

    cur = conn.cursor()

    cur.execute("SELECT distinct img_zdb_id, fig_source_zdb_id, img_image "
                "     FROM figure, image WHERE fig_zdb_id = img_fig_zdb_id"
                "     ORDER BY fig_source_zdb_id"
                )

    for img_id, fig_source_id, img_name in cur.fetchall():

        m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)$", fig_source_id)
        if m:
            year = m.group(2)
            if year.startswith('9'):
                year = "19" + year
            else:
                year = "20" + year

        yearDir = os.environ['LOADUP_FULL_PATH']+"/pubs/"+year+"/"
        fullPathPDFDir = os.environ['LOADUP_FULL_PATH']+"/pubs/"+year+"/"+fig_source_id

        if os.path.isdir(fullPathPDFDir) and not os.path.exists(fullPathPDFDir+"/"+img_id):
            moveImages(fullPathPDFDir, img_id)
            

        else:
            print "cant find file dir: " + fullPathPDFDir
            if os.path.isdir(yearDir):
                os.mkdir(fullPathPDFDir)
                moveImages(fullPathPDFDir, img_id)
                

            else:
                os.mkdir(yearDir)
                os.mkdir(fullPathPDFDir)
                moveImages(fullPathPDFDir, img_id)
                


def moveImages(fullPathPDFDir, img_id):

    pattern = '/research/zcentral/loadUp/imageLoadUp/' + img_id + '*'

    for imgFile in glob.glob(pattern):
        print(imgFile)
        copy(imgFile, fullPathPDFDir)

    pattern = '/research/zcentral/loadUp/imageLoadUp/medium/' + img_id + '*'

    for imgFile in glob.glob(pattern):        
        imgName = imgFile.replace(".", "_medium.")
        fileNameSplit = imgName.split('/')
        imgFileName = fileNameSplit[-1]
        copy(imgFile, fullPathPDFDir+"/"+imgFileName)
        print fullPathPDFDir+"/"+imgFileName
        file.write(img_id + "|" + fullPathPDFDir + "/" + imgName + "\n")

myConnection = psycopg2.connect(host=hostname, dbname=database)
do_query(myConnection)
myConnection.close()

