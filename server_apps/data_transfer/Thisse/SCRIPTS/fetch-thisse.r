Rebol[
file: %fetch-thisse.r

]
comment "grab files gene expression files from bernard thisse's ftp dir" 

thisse:  ftp://ftp-igbmc.u-strasbg.fr/pub/thisse/sending%20CB1_CB2/

dirlist: read thisse
probe dirlist
print ""
subdirlist: copy ""

foreach file dirlist [
	
	probe file
   print ""
	either (dir? file)
	[	
		info?  to-url rejoin[thisse file]

		subdirlist: read to-url rejoin[thisse file]
		probe  subdirlist
		print ""
		foreach subfile subdirlist [
			print ["s-file" subfile]
			write/binary (to-file file subfile) read/binary to-url rejoin [thisse file subfile]
		]
	]
	[
		print ["file" file]
		write/binary (to-file file) read/binary to-url rejoin [thisse file]
	]
]
