#!/private/apps/rebol/rebol --cgi
rebol[]
header:
{POST http://www.ncbi.nlm.nih.gov/blast/blast.cgi HTTP/1.0
Referer: http://zfin.org/
Proxy-Connection: Keep-Alive
User-Agent: Rebol-script
Host: www.ncbi.nlm.nih.gov
Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png,*/*
Accept-Encoding: gzip
Accept-Language: en
Accept-Charset: iso-8859-1,*,utf-8
Content-type: application/x-www-form-urlencoded
Content-length: }
query: rejoin
[{ADV_LAB=++&PROGRAM=blastn&DATALIB=nr&INPUT_TYPE=Accession+or+GI&SEQUENCE=}
system/options/cgi/query-string
{&DOUBLE_WINDOW=IS_SET&OVERVIEW=on&ALIGNMENT_VIEW=0&DESCRIPTIONS=100&ALIGNMENTS=50&PATH=&B-ADVNCD.x=64&B-ADVNCD.y}
]
ncbi: open tcp://www.ncbi.nlm.nih.gov:80
insert ncbi rejoin[ header (length? query) newline newline query ]
while  [none? reply: copy ncbi ][ wait 10]
buffer: copy/deep find reply "Content-Type:"
buffer: replace/all buffer {"new.gif} {"http://www.ncbi.nlm.nih.gov/BLAST/new.gif}
print replace/all buffer {"/blast/} {"http://www.ncbi.nlm.nih.gov/BLAST/}
close ncbi
