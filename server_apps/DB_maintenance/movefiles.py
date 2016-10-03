#!/usr/bin/python

import re
import os
import shutil

pdf_dir = '<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->'

for f in os.listdir(pdf_dir):
    m = re.match("^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)(\.pdf)$", f)
    if m:
        year = m.group(2)
        month = m.group(3)
        day = m.group(4)

        if year.startswith('9'):
            year = "19" + year
        else:
            year = "20" + year

        src_path = os.path.join(pdf_dir, f)
        year_dir = os.path.join(pdf_dir, year)
        dst_path = os.path.join(year_dir, f)

        if os.path.isfile(dst_path):
            continue

        if not os.path.isdir(year_dir):
            os.mkdir(year_dir)

        print src_path
        shutil.copy(src_path, year_dir)
