#!/bin/python
import psycopg2
from psycopg2.extensions import AsIs

conn = psycopg2.connect("host='localhost' port='5432' dbname='swrdb' user='staylor'")
cur = conn.cursor()

f= open(r'/research/zunloads/databases/postgres_dumps/2016.08.26.1_replaced/filenames.txt','r');

for line in f:
    print "processing file: " + line
    filename = line.rstrip('\n')
    tablef = open(r'/research/zunloads/databases/postgres_dumps/2016.08.26.1_replaced/'+filename+'.txt', 'r')
    print "truncating table " + filename
    cur.execute("truncate %(table)s", {"table": AsIs(filename)})
    print "loading table " + filename
    cur.copy_from(tablef, filename, sep='|', null="")
    tablef.close()

conn.commit()
conn.close()
