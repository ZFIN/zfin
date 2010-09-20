#! /private/bin/rebol -sqw
;;;w
rebol[Title: "make specific"
    Date: [2005-Aug-02 2009-Apr-24 2010-Mar-31]
    usage: "make-specific.r <generic-file> <translation-table-file> <specific-file>"
]
;;; COPY ONCE

;;; helper to clean up double slashes in absolute or relative paths
path: func [filename [string!]][
    replace/all filename "//" "/"
    to file! either find/match filename "/"
        [filename]
        [join system/options/path filename]
]

FAIL: -1 ;;; exit code to pass back to calling shell

;;; check that arguments could be rational
either any[ none? args: system/script/args    3 <> length? args: parse args none ]
[   print ["MAKESPECIFIC Usage: " system/script/header/usage]
    quit/return FAIL
][
    ;;; Bail if cannot read generic file
    if err: error? try[buffer: read/binary generic: path args/1][
        print ["MAKESPECIFIC ERROR cannot open generic file " generic]
        print ["MAKESPECIFIC Usage: " system/script/header/usage]
        probe disarm err
        quit/return FAIL
    ]
    ;;; Bail if cannot read translation table
    if err: error? try[translation-table: read/lines path trim args/2][
        print ["MAKESPECIFIC ERROR cannot open translation file " path trim args/2]
        print ["MAKESPECIFIC Usage: " system/script/header/usage]
        probe disarm err
        quit/return FAIL
    ]
    ;;; Bail if cannot write to the "specific" file
    specific: path trim args/3
    either not error? mode: try[get-modes specific 'owner-write][
        ;;; file may exist, attempt to make writable
        if not mode[try[set-modes specific[owner-write: true]]]
        if error? err: try[write/append specific "" true][
            print ["MAKESPECIFIC ERROR unable to write output file " specific ]
            probe disarm err
            quit/return FAIL
        ]
    ][  disarm mode
        if error? err: try[write/append specific "" true][
            print ["MAKESPECIFIC ERROR unable to create output file " specific ]
            probe disarm err
            quit/return FAIL
        ]
    ]
];;; ~ check for well formed arguments

;;; ought not mess with binary files.
;;; squawk if not just ascii chars 9-126 (tab-tilde)
seven-bit-clean: true
validchar: make bitset!
    #{00FEFFFFFFFFFFFFFFFFFFFFFFFFFF7F00000000000000000000000000000000}
if not parse read/binary generic[some validchar end][
    seven-bit-clean: false
    print rejoin ["MAKESPECIFIC ERROR binary file " path  args/1]
]

;;; expect the last two rows of .tt file to be start & end patterns
;;; this allows different tables to have different tag demarkers
;;; (bit of a bell&whistle as we have never used this capability)
st-len: length? start-tag: first parse first skip tail translation-table -2 none
et-len: length? end-tag:   first parse first skip tail translation-table -1 none
clear skip tail translation-table -2;

;;; expect one or more tab/space between translation pairs, puts them in a hash
tt: make hash!
token: copy ""
target: copy ""
;;; make explicit copies of the bitsets so as not to regenenerate each run
;;; could genrerate 'ws' (space&tab) and 'bs' (everything else) with
;;; ws: charset [#" "#"^-"] bs: complement ws
ws: make bitset!
    #{0002000001000000000000000000000000000000000000000000000000000000}
bs: make bitset!
    #{FFFDFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF}

;;; reduce the translation table file to a key-value hash
foreach line translation-table [
    if all[not equal? #"#" pick line 1    not equal? "" line: trim line][
        parse/all line [
            copy token some bs some ws (insert tail tt trim token)
            copy token to end          (insert tail tt trim token)
       ]
    ]
]
;;; Bail if the translation table is useless or structualy courupt (wonky)
wonky: false
forskip tt 2 [
    if any[wonky not find first tt start-tag     not find first tt end-tag][
        wonky: true tt: tail tt
    ]
] tt: head tt

if any [wonky zero? length? tt    odd? length? tt][
    print ["MAKESPECIFIC ERROR TRANSLATION TABLE IS WONKEY" ]
    print ["        TRANSLATION TABLE FORMAT:  " start-tag "<key>" end-tag
           " whitespace <value ...>newline"
          ]
    quit/return FAIL
]

translated: make string! 2 * length? buffer

;;; check that at least one tag demarker pair could exist in the generic file
either here: find buffer start-tag
    [ if not find here end-tag[
        print ["MAKESPECIFIC WARNING NO END tag in: " generic]
        translated:  buffer
      ]
    ][print ["MAKESPECIFIC WARNING NO START tag in: " generic]
      translated:  buffer
    ]


;;; Main loop
;;; run thru the file once instead of once per tag
;;; copy everything, but always only once.
;;; allows for the same tag to be both stop & start  i.e. a separater

if empty? translated[
    parse/all buffer[
        there:
        some[
            to start-tag here: (insert tail translated copy/part :there :here)
            copy token [st-len skip  thru end-tag] there:
            (insert tail translated
                either all[target: select tt token   even? index? find tt target]
                [target][token]
            )
        ] to end
        (insert tail translated copy :there )
    ]
]
;;; sanity check on untranslated tags
if find translated start-tag[
    print ["MAKESPECIFIC WARNING unmatched START tag in: " generic]
]
if find translated end-tag[
    print ["MAKESPECIFIC WARNING unmatched END tag in:" generic]
]

;;; write specific file out
either err: error? try[write specific ""][
    print ["MAKESPECIFIC ERROR unable to write output file " specific newline]
    probe  disarm err
    quit/return FAIL
][
    either seven-bit-clean[write specific translated][write/binary specific translated]
]

;;; set specific permissions to generic permissions
;;; call rejoin[ "getfacl " generic " | setfacl -f - " specific]
;;; if error? err: try[...]
;;;    ["MAKESPECIFIC WARNING cannot match permissions^/" probe disarm err]
set-modes specific get-modes generic [owner-read owner-write owner-execute
    group-read group-write group-execute world-read world-write world-execute
    set-user-id set-group-id
]
;halt
quit/return 0
