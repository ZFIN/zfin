#! /opt/zfin/bin/rebol -sqw

rebol[]

comment { usage

rebol -sq parse-gff3-unl.r drerio_vega.gff3 >! drerio_vega.unl
}

gff-col: [
    seqname
    source
    feature
    start
    end
    score
    strand
    frame
    ;;  attribute ;;; see below
]

;gff-rule: copy []
;;; make a parse rule for the first 8 tab separated columns
;foreach col gff-col [
;    insert tail gff-rule reduce/only [copy col to tab tab] [copy to tab]
;]
;;;probe gff-rule print "^/^/"


gff-rule: [
    copy seqname to tab tab
    copy source to tab tab
    copy feature to tab tab
    copy start to tab tab
    copy end to tab tab
    copy score to tab tab
    copy strand to tab tab
    copy frame to tab tab
]


;;;  the current set is
;;; ID      Name    Parent  biotype
;;; ID      Name    biotype
;;;         Name    Parent

;;; a default object to hold the attributes,
;;; including the optional ones as empty strings
;;; note that order DOES matter.

attribute: make object![
    ID:  copy ""
    Name: copy ""
    Parent: copy ""
    biotype: copy ""
] ;
a: make attribute[]

attribute-rule: [
    ( a/ID: copy "" a/Name: copy "" a/Parent: copy "" a/biotype: copy "" )
    some [
		;;;here: ( print copy/part :here 40) ; debugging 
        ["ID=" copy token to ";" ";" (a/ID: token)] |
        ["Name=" copy token to ";" ";" (a/Name: token)] |
        ["Parent=" copy token to ";" ";" (a/Parent: token)]|
        ["biotype=" copy token to ";" ";" (a/biotype: token)]
    ]
]

row: make string! 256

gff3-rule: [
    any newline
    any ["##" thru newline any newline]
    (row: clear head row)
    gff-rule       ;;; populate
    attribute-rule ;;; populate a
    (foreach col gff-col [insert tail row rejoin compose[(col)"|"]]
     foreach col next first attribute [
        insert tail row rejoin compose[(a/:col)"|"]
     ]
     print row
    )
    opt [thru newline] ;;; the last line may be missing a NL
]

parse/all read to file! trim system/script/args [some gff3-rule]

;;;halt
