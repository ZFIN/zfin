#! /home/users/tomc/bin/rebol -sqw
rebol[
	Author: "Tom Conlin"
	Date: 2001-Oct-24
	comment{ change db flatfiles from tab-sep to comma-sep format}
]

buffer: copy ""

either none? system/script/args
	[set-modes system/ports/input [lines: false binary: false]
   	if error? err: try[buffer: copy system/ports/input ][
        	print ["ERROR "  err halt]]
	]
	[if error? err: try[buffer: read to-file system/script/args ][
                  print ["ERROR "  err halt]]
	]

;if find buffer {,}[print "Warning comma(s) found in input"]
;if find buffer {"}[print "Warning quote(s) found in input"]
 
insert buffer {"}

replace/all buffer "^-" {","}

replace/all buffer "^/" {"^/"}

either  (back tail buffer) == {"} 
	[clear back back tail buffer]
	[append buffer {"}]

insert system/ports/output buffer
