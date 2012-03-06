#! /private/bin/rebol -sqw

rebol[
	author: "Tom Conlin"
	date: May-16-2008
	usage: {
	topo-sort.r <partial_order.txt>
	
	where the <partial_order.txt> file has a pair of terms per line
	the first term, a prerequsite for the second term
	}
]

; do %~/bin/topo-sort.r
; graph: parse read %build.po none
; tsg: topo-sort graph
; cc: conn-comp copy graph
; foreach [s d] g [print [a b]]


context[
graph: copy [] ;;; partial order of edges

set 'conn-comp func [graph [block!] /local result][
	result: topo-sort reverse topo-sort reverse topo-sort graph

];~conn-comp
;;;
decendent: func [parent [string!] po [series!] store [block!] pad [string! char!]][
	forskip po 2[
		if parent = first po[
			insert tail store rejoin [pad second po newline]
			decendent second po head po store join pad first pad
		]
	]
];~decendent

;;;
set 'root-tree func [graph [block!] /local comopnent s t root][
	comopnent: make block! length? graph
	s: make block! .5 * length? graph
	graph: topo-sort graph
	forskip graph 2 [insert tail s first graph]
	s: unique s ; all start nodes
	t: next graph: head graph
	root: unique intersect s difference/skip graph t 2

	foreach r root [
		clear comopnent insert comopnent rejoin ["" r newline]
		decendent r graph comopnent "^-"
		print comopnent
		print "--------------------------------------------"
	]
]; ~root-trace


;;;
set 'topo-sort func [graph [block!] /local result s t root][
	result: make block! length? graph
	s: make block! .5 * length? graph
	forskip graph 2 [insert tail s first graph]
	s: unique s ; all start nodes
	t: next graph: head graph
	root: unique intersect s difference/skip graph t 2
	;print "starting root(s)" foreach r root[print r] print ""
	either empty? root[print "cycle-no root" quit/return 1][];probe root]
	until [
		while [not tail? graph][
			either root/1 = first graph
				[loop 2 [insert tail result first graph remove graph]]
				[graph: skip graph 2]
		]
		remove root
		t: next graph: head graph
		insert tail root unique intersect s difference/skip graph t 2
		;print "new root(s)" foreach r unique intersect s difference/skip graph t 2[print r] print ""
		empty? root
	]
	either empty? graph
		[result]
		[print "cycle" probe graph quit/return 1]
];~topo-sort

]; ~context

root-tree parse read join system/options/path system/options/args none

;halt


