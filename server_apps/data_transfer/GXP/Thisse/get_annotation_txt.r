REBOL[
    File: "get_annotation_txt.r"
]

foreach adir read %.[
    either dir? adir
    [
     foreach file read adir[ 
	either dir? file
	[]
	[  f: to-string file
		either (skip tail f -4) == ".txt" 
		[
			words: copy ""
			txtfile: join adir file
			write/append/lines %annotation.txt f
			parse/all read txtfile [
				any [ copy w to "|"(append words join (trim w) [", "] ) [thru ":::" | end]]
			]
			clear find/last words ","
			write/append/lines %annotation.txt words
			write/append/lines %annotation.txt ""
			if find words ":" [
				print join f " has bad ':'"
			]
			if find words "null" [
				print join f " has null string"
			] 
		]
		[]
	 ]
      ]
    ]         
    [ ]
] 
