#! /private/bin/rebol -sqw

;;mandatory header - nothing above it is interperted
rebol[]

;;make a place to accmulate the data before writing it out at the end

buffer: make string! 1000000

;;create a prototype object to contain default go record
;;in the rebol prgrm (the parse file prgrm is a subflavor of rebol)
;;have to have the 'copy' keyword, as referencing '' more than 
;;once could lead to overwriting of value.

go-obj: context [term: copy ""
		 id: copy "" 
		 name: copy "" 
                 namespace: copy ""
		 def: copy "" 
                 is_a: copy ""  
		 exact_synonym: copy ""
                 alt_id: copy "" 
		 xref_analog: copy "" 
		 relationship: copy "" 
                 comment: copy "" 
		 is_obsolete: copy ""
		 xref_unknown: copy ""
		 subset: copy ""
		 synonym: copy ""
		 related_synonym: copy "" 
		 narrow_synonym: copy ""
		 broad_synonym: copy ""
		 use_term: copy ""
		  ] 

parse file: read ftp://ftp.geneontology.org/go/ontology/gene_ontology.obo
;parse file: read %tester.txt
 [;;a block of rules to parse the file

	;;ignore any like that begins with the following: 

	opt ["format-version:" thru newline]
	opt ["date:" thru newline]	
	opt ["saved-by:" thru newline]
	opt ["auto-generated-by:" thru newline]
	opt ["default-namespace:" thru newline] 
	opt ["remark:" thru newline]
	opt ["subsetdef:" thru newline]

	;;each some is a new rule, parser goes through rules 1 at a time
	;;at each newline, trying to match input with rule.  If input
	;;does not match any rule listed, then the parser fails.

	newline
 
       some[ ;;; record
	;;make a blank object with format from above
	;;if you want, add values to existing variables, in[] below, now.
	(goobj: make go-obj[]) 
;;	here: (print copy/part :here find :here newline)
                some[ ;;; row
      
			;;fill up the currently default '' values from
			;;new record.
			;;grab info past token all the way to the next newline

                        [["[TERM]" copy token to newline 

			 newline

			(goobj/term: token)] |
			
			["id: " copy token to newline 

			 newline

			(goobj/id: token)] |

			 ["name: " copy token to newline 

			 newline

			(goobj/name: token)] |

			 ["namespace: " copy token to newline 

			newline

			(append goobj/namespace token)] |

			 ["def: " copy token to newline 
		
			 newline
	
			(replace/all token "|" "\|" )

			 (append goobj/def token)] |

			 ["is_a: "copy token to newline 

			newline
		
			  (append goobj/is_a token)] | 


			["exact_synonym: " copy token to newline 

			newline
		
			  (append goobj/exact_synonym token)] |

			 ["alt_id: " copy token to newline 

			 newline
		
			  (goobj/alt_id: token)] |
			 
			["xref_analog: " copy token to newline 

			 newline
		
			  (append goobj/xref_analog token)] |		
			
			["relationship: " copy token to newline 
			
			 newline
		
			  (append goobj/relationship token)] |

			["comment: " copy token to newline 
	
			 newline

			  (append goobj/comment token)]|

			["is_obsolete: " copy token to newline 

			 newline
		
			  (goobj/is_obsolete: token)]|

			["xref_unknown: " copy token to newline 

			 newline
		
			  (append goobj/xref_unknown token)]|
		
			["subset: " copy token to newline 
			
			newline
		
			  (append goobj/subset token)]|

			["synonym: " copy token to newline 
			
			 newline
		
			  (append goobj/synonym token)]|
				
			["related_synonym: " copy token to newline 

			newline
		
			  (append goobj/related_synonym token)]|

			["narrow_synonym: " copy token to newline 
		
			newline

			  (append goobj/narrow_synonym token)]|

			["broad_synonym: " copy token to newline 
	
			 newline
		
			  (append goobj/broad_synonym token)]|

			["use_term: " copy token to newline 

			 newline
		
			  (append goobj/use_term token)]]

	]   ;;; end row rule



 ;;here: (print copy/part :here find :here newline)
                ;;put the token followed by a | in the buffer, | indicates
		;;column seperation in IDS 9.* load keywords.
		;;add all the newly parsed text to an output buffer
	
			(append buffer join trim goobj/id "|"
			append buffer join trim goobj/name "|"
			append buffer join trim goobj/namespace "|"
			append buffer join trim goobj/def "|"	
			append buffer join trim goobj/is_a "|"
			append buffer join trim goobj/exact_synonym "|"
			append buffer join trim goobj/alt_id "|"
			append buffer join trim goobj/xref_analog "|"
			append buffer join trim goobj/relationship "|"
			append buffer join trim goobj/comment "|"	
			append buffer join trim goobj/is_obsolete "|"	
			append buffer join trim goobj/xref_unknown "|"
			append buffer join trim goobj/subset "|"
			append buffer join trim goobj/synonym "|"
			append buffer join trim goobj/related_synonym "|"
			append buffer join trim goobj/narrow_synonym "|"
			append buffer join trim goobj/broad_synonym "|"
			append buffer join trim goobj/use_term "|") 

	;;add a newline to buffer to move append to next line

		 	(append buffer newline)

 	       ;;print out to screen--good for debugging


	newline
     ];;; end record rule	
	
	end
]

;;write the accmulated rows out to a file
;;file will be written over at each execution of this script.
write %godefs_parsed.unl buffer