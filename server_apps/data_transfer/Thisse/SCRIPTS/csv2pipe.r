Rebol[ 
	File: %csv2pipe.r
	Author: "Tom Conlin"
	Date: 2001-July-23
	purpose: {change from a comma delimited file to a pipe terminated file}
]

csv2pipe: func [f [file!] /local buffer ][
	buffer: read  f
	remove buffer
	replace/all buffer {","}  "|"
	replace/all buffer {"^/"} "|^/"
	replace/all buffer "|^""  "|" 
	while [(last buffer) == #"^/"][clear back tail buffer]
	if (last buffer) == #"^"" [clear back tail buffer] 
	append buffer "|^/"
	buffer
]
