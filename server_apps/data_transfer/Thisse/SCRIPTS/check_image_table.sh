#! /bin/csh
	
echo "looking for rows in images.csv that that don't have .jpg and .txt files"
echo ""
foreach f ( `cat images.csv | cut -f1 -d,| tr -d \" `) 
	if ( "`ls Imagesdir/'$f'`" != "Imagesdir/$f") echo $f
	if ( "`ls Imagesdir/'$f:r.txt'`" != "Imagesdir/$f:r.txt") echo $f:r.txt
end

echo ""
echo ""

echo "looking for .jpg sent but not included in the images.csv table"

cat images.csv | cut -f1 -d\. | tr -d \" | sort >! imgtab.list
cat imagedim.unl | cut -f1 -d\| | sort >! imgdir.list
diff imgtab.list imgdir.list  |grep '^>' | cut -c 3-
echo ""
echo ""
