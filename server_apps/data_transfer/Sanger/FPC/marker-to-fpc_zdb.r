rebol[
    
]

bacend-head: ["bz" | "zkp" | "zk" | "zc"]
bacend-tail: ["sp6" | "t7" | "y1" | "ya" | "yb" | "yc" | "za" | "zb" | "zc" | "1" | "2" | "y" | "z" ]
alpha: charset [#"a" - #"z"]

buf: read/lines to-file f: trim system/script/args
row: copy []
out: make string! 100 * length? buf
blk: make block! 10

foreach line buf [
    row: parse line "|"
    blk: make block! 10
    foreach k parse/all row/2 ";" [
        foreach i parse/all row/3 ";" [
            if all [not equal? "ZDB-" copy/part i 4 j: find/last i "_"][clear j]
            if all [j: find/last i "." 
                    not parse lowercase z: copy i [bacend-head integer! some alpha integer! "." bacend-tail end]
                   ]
            [clear j]
            if error? try[to-integer? i: trim i][
              insert tail blk rejoin [i "|" k "|^/"]
            ]
        ]
    ]
    blk: unique blk
    foreach i blk [insert tail out i]	
]
print out


