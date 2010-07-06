#! /bin/sh

HEADER_STRING='<script language="JavaScript" src="\/javascript\/header.js"><\/script>'

ADD_STRING=' <table bgcolor="#ffcccc" width="50%" align="center"> <tr><td class="small"> This material is from the 4th edition of The Zebrafish Book.  The 5th edition is available <a href="http:\/\/zebrafish.org\/zirc\/orders\/buyBookQ.php?item=Book\&id=book\&detail=The%20Zebrafish%20Book">in print<\/a> and within the <a href="https:\/\/wiki.zfin.org\/display\/prot\/ZFIN+Protocol+Wiki">ZFIN Protocol Wiki<\/a>.  <\/td><\/tr> <table> '

find . -name \*.html | while read file
do
sed -e s/"$HEADER_STRING"/"&""$ADD_STRING"/g $file  > $file.$$
mv -f $file.$$  $file 
done 


