rebol[]
comment{
files: read %.
foreach file files[ ;eliminate trailing returns in anotation
	if all[ not dir? file (skip tail to-string file -4) == ".txt" ][			
		buffer: read file
		replace/all buffer "^/|" "|"
		write file buffer 
	]
]
}
foreach file read %.[ 
	either dir? file
	[]
	[  f: to-string file
		either (skip tail f -4) == ".txt" 
		[
			words: copy ""
			print f
			parse/all read file [
				any [ copy w to "|"(append words join (trim w) [", "] ) [thru ":::" | end]]
			]
			clear find/last words ","
			print words
			print ""
		]
		[]
	]
]
