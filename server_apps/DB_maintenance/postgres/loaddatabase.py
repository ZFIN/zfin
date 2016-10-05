#!/bin/python
import psycopg2
from psycopg2.extensions import AsIs

conn = psycopg2.connect("host='localhost' port='5432' dbname='swrdb' user='staylor'")
cur = conn.cursor()

f= open(r'/research/zunloads/databases/postgres_dumps/2016.08.26.1_replaced/filenames.txt','r');

for line in f:
    filename = line.rstrip('\n')
    tablef = open(r'/research/zunloads/databases/postgres_dumps/2016.08.26.1_replaced/'+filename+'.txt', 'r')
    cur.execute("truncate %(table)s", {"table": AsIs(filename)})
    cur.copy_from(tablef, filename, sep='|', null="")
    tablef.close()

conn.commit()
conn.close()
