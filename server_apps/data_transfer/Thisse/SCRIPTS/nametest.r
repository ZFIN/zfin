rebol[]

imgtab: read/lines %images.unl
stems: copy []

foreach line imgtab[
	row: parse line "|"
	append stems row/1
]

sufixs: [".jpg" "--t.jpg" "--C.jpg" ".txt"]

foreach n stems[
	foreach s sufixs [
		name: copy "" 
		either error? try[read to-file name: rejoin [n s]]
		[print ["X" name]]
		[print ["0" name]]
	]
]

