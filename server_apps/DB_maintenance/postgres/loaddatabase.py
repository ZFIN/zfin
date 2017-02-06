#!/bin/python
from psycopg2.extensions import AsIs
import psycopg2, sys, getopt, os


def main(argv):

    loadDirectory = ''
    fileLister="filenames.txt"

    try:
        opts, args = getopt.getopt(argv, "d:h",[])
    except getopt.GetoptError:
        print 'loaddatabase.py -d <dumpToLoad>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == "-h":
            print 'loaddatabase.py -d <dumpToLoad>'
            sys.exit()
        elif opt in ("-d"):
            loadDirectory = arg
            print 'loadDirectory file is "', loadDirectory

    dbname = os.environ['DBNAME']
    print dbname
    user = os.environ['USER']
    print user
    hostname = "localhost"
    port = "5432"

    cs = "dbname=%s user=%s host=%s port=%s" % (dbname,user,hostname,port)
    conn = psycopg2.connect(cs)
    cur = conn.cursor()
    
    filePath=os.path.join(loadDirectory,fileLister)
    print filePath
    f= open(os.path.join(loadDirectory,fileLister),'r');
    
    for line in f:
        print "processing file: " + line
        tablename = line.rstrip('\n')
        tablenameFile = tablename+".txt"
        tablef = open(os.path.join(loadDirectory,tablenameFile), 'r')
        print "truncating table " + tablename
        cur.execute("truncate %(table)s", {"table": AsIs(tablename)})
        print "loading table " + tablename
        cur.copy_from(tablef, tablename, sep='|', null="")
        tablef.close()

    conn.commit()
    conn.close()

if __name__ == "__main__":
    main(sys.argv[1:])
