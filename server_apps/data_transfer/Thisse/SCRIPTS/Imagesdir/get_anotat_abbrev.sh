#! /bin/tcsh

foreach f (CB*.txt) 
	echo $f:r
	cat $f |\
	 tr -d '0123456789,:'# |\
	 #sed -e s/black\|/\,/g -e //p
	 echo " "
end
