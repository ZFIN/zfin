rebol[]

buf: read/lines to-file f: trim system/script/args
row:  copy []
out: make string! 100 * length? buf
set: make block! 10

foreach line buf [
	row: parse line "|"
	set: make block! 10	
	foreach k parse/all row/2 ";" [
		foreach i parse/all row/3 ";" [
			if all [not equal? "ZDB-" copy/part i 4 j: find/last i "_"][clear j]
			if j: find/last i "."[ clear j]
			if error? try[to-integer? i: trim i][
				insert tail set rejoin [i "|" k "|^/"]
			]
		]
	]
	set: unique set
	foreach i set [insert tail out i]	
]
print out
