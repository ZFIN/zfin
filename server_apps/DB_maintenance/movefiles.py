#!/usr/bin/python

import re
import os
import shutil
files = os.listdir("<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->")

output = {}
path = '<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->'
origPath = '<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->'

for f in files:
        print f;
        m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)(\.pdf)$", f)
        if m: 
            year = m.group(2)
            month = m.group(3)
            day = m.group(4)
       
            if year.startswith('9'):
                year = "19"+year
            else:
                year = "20"+year
  
            if not os.path.isdir(year):
                os.mkdir(os.path.join(path,year))
            shutil.copy((os.path.join(origPath,f)),year)
