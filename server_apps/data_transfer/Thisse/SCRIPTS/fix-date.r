rebol[]
comment{
	reads in a file in .csv format and finds the last field in each row
	this field is expected to have date in (Eric's) Mac FMPro format; 
	that is ...","M.D.YYYY"
	the goal is to change it to 'YYYY-MM-DD' format that informix is able to load
	the script is carful not to re-change date formats if run twice

	example: ">> write %bar.csv fix-date %foo.csv"
}

fix-date: func [name [file!] /local rows r  out d t][
	out: copy ""
	dirty: false
	rows: read/lines name
;	print to-string name
	count: 0
	foreach r rows[
;	   print [count: count + 1 find/last r {,} ]
		t: skip find/last r {","} 3	
		d: copy t
		if (find d {/}) [replace/all d {/} {.}]
		try[clear find d {"}]
		if error? try[load d] [
			d: parse d "." 
			clear t
 			reverse/part d 2
			reverse/part d 3 
			either (length? d/2) > 1 [insert d/2 "-"] [insert d/2 "-0"]
			either (length? d/3) > 1 [insert d/3 "-"] [insert d/3 "-0"]
			d: rejoin d 
			append r d
			append r {"}
			dirty: true
		]
		append r {^/}
		append out r
	]
	either dirty [out][false]
]
