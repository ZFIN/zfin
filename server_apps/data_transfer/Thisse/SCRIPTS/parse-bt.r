rebol[
	title: "parse bt" 
	date: [2001-Aug-2 2001-Nov-27]
	Author: "Tom Conlin"
]


do %../Scripts/csv2pipe.r
do %../Scripts/fix-date.r

	
	;;;;;;;;;;;;;; 
	;;; PROBES ;;;
	;;;;;;;;;;;;;; 
   comment: {
		probes_txt
		-----------

create temp table probes_tmp (
1	label varchar (80) not null,
2	isgene varchar(15),
3	genename varchar(100),
4	top_blast varchar(200),
5	blast_result lvarchar, 
6	gb5p varchar (50),
7	gb3p varchar (50),
8	or_lg integer,
9	lg_loc decimal(8,2),
0	metric varchar(5),
1	library varchar(80),
2	vector varchar(80),
3	insert_kb float,
4	cloning_site varchar(20),
5	digest varchar(20),
6	polymerase varchar(80),
7	medline_id varchar(80),
8	text_citation lvarchar, 
9	comments lvarchar,
0	expression lvarchar, 
1	modified DATETIME YEAR TO DAY
)

	}
;	buffer: fix-date %probes.csv  if buffer [write %probes.csv buffer] 
  
	comment{
		Arrrg! have to pull the blast result out into a file and load it as a clob
	}
	row:  copy []
	line: copy ""
	buf:  copy ""
	field:    copy ""
	buffer: read/lines %probes.csv
	write %probes.unl ""
	foreach line buffer [
		remove line
		clear back tail line
		replace/all line "|"  "<PIPE73>"
		replace/all line "hpf"  "h" 	
		replace/all line "^k"  "<br>" 
		replace/all line "primed<br>"  "primed "
		replace/all line "<br><br>Query: " "\^/\^/Query: "
		replace/all line {","}  "|"
		replace/all line "| "   "|" 
	   replace/all line " |"   "|"
 
		row: parse/all line {|}
		replace row/9 "," "."     ; change decimal point in lg_location to dot
		buf:  copy row/5
		insert (skip row 4)  rejoin [row/1 ".blast"]	
		remove (skip row 5)
		;probe row
		;print ["row 5" row/5 ]
		;foreach field row [append line (join field ["|"])] ;what the h_ll! puts the blast back in?
 	   ;hack 
		line: rejoin [row/1 "|" row/2 "|" row/3 "|" row/4 "|" row/5 "|" row/6 "|" row/7 "|" row/8 "|" row/9 "|" row/10 "|" row/11 "|" row/12 "|" row/13 "|" row/14 "|" row/15 "|" row/16 "|" row/17 "|" row/18 "|" row/19 "|" row/20 "|"]  	   replace/all line "<PIPE73>" "\|"
		write/append %probes.unl rejoin [line newline]
  
		replace/all buf "<PIPE73>" "|"	
		write to-file rejoin [row/1 ".blast"]	 rejoin ["<pre>" buf "</pre>"]
		
	]
	
	; change bernards stages to zfin stage ranges
	map-stages: func[ buffer [string!] /local cut paste changes][
		changes: [
			"|<none>|" "|unknown|unknown|"                              
			"|B|"      "|Blastula:Sphere|Blastula:30%-epiboly|"         
			"|G|"      "|Gastrula:50%-epiboly|Gastrula:Bud|"            
			"|ES|"     "|Segmentation:1-somite|Segmentation:5-somite|"  
			"|MS|"     "|Segmentation:14-somite|Segmentation:14-somite|"
			"|24 h|"   "|Segmentation:20-somite|Pharyngula:Prim-5|"     
			"|36 h|"   "|Pharyngula:Prim-15|Pharyngula:Prim-25|"        
			"|48 h|"   "|Pharyngula:High-pec|Hatching:Long-pec|"        		
		]
		foreach [cut paste] changes [replace/all buffer cut paste]
	   buffer
	]


	;;;;;;;;;;;;;;;;;;
	;;; EXPRESSION ;;;
	;;;;;;;;;;;;;;;;;;
	comment{
		expression_txt
		--------------
		cDNA name
		Stage
		Description
		modified
	}
;	buffer: fix-date %expression.csv
	if buffer [write %expression.csv buffer]
	buffer: csv2pipe %expression.csv
	;;; trim extranous spaces
	replace/all buffer "| "   "|" 
	replace/all buffer " |"   "|" 

	buffer: map-stages buffer

	replace/all buffer "^k"  "<br>"

	write %expression.unl buffer

        ;;;;;;;;;;;;;;;;
	;;; KEYWORDS ;;;
	;;;;;;;;;;;;;;;;
	comment {
		keywords_txt
		------------
		cDNA name
		Stage
		Keyword
		modified
	}
;	buffer: fix-date %keywords.csv
	if buffer [write %keywords.csv buffer]
	buffer: csv2pipe %keywords.csv
	;;; trim extranous spaces
	replace/all buffer "| "   "|" 
	replace/all buffer " |"   "|" 

	;global changes to bernards keywords can occur here
	changes: [
		"|floorplate|" "|floor plate|"
		"|deep cells|" "|DEL cells|"
	]
	foreach [cut paste] changes [replace/all buffer cut paste]

	
	buffer: map-stages buffer   
	write %keywords.unl buffer


        ;;;;;;;;;;;;;;;
	;;; AUTHORS ;;;
	;;;;;;;;;;;;;;;
	comment {
		authors_csv
		------------
		EST
		Author
		Modified		
	}
	buffer: fix-date %authors.csv
	if buffer [write %authors.csv buffer]
	buffer: csv2pipe %authors.csv
	;;; trim extranous spaces
	changes: [
		"| "   "|" 
		" |"   "|" 
		"Obrecht-Pflumio"	"Pflumio"  
		"Fuerthauer"		"Fürthauer"
	]

	foreach [cut paste] changes [replace/all buffer cut paste]
	
	write %authors.unl buffer

	
	;;;;;;;;;;;;;
	;;; IMAGE ;;;
	;;;;;;;;;;;;;
	comment{
		images_txt
		----------
		Image filename
		cDNA name
		Stage
		Orientation (view)
		Orientation (direction)
		Specimen
		Primary publication - Medline ID
		Primary publication - text citation (optional)
		modified
	}
;	buffer: fix-date %images.csv
	if buffer [write %images.csv buffer]
	buffer: csv2pipe %images.csv
	;;; trim extranous spaces
	replace/all buffer "| "   "|" 
	replace/all buffer " |"   "|" 

   buffer: map-stages buffer
	
	

	comment{
	in zfin the acceptable dir/views are ...
		fimgdir_name         fm_direction     
      -------------        ------------     
		anterior to right	   anterior to right          
		dorsal to right		dorsal to right 
		anterior to left	   anterior to left 
		anterior to top		anterior to top
		dorsal to left		       
		not specified			<no direction selected>   
		dorsal to top		   dorsal to top          
		oblique					oblique
									 

		fimgview_name        fm_view            
		-------------		   ------             
		animal pole			   animal pole        
		dorsal					dorsal             
		frontal				   frontal            
		parasagittal			parasagittal       
		side view				side view (lateral)            
		ventral				   ventral             
		not specified			<none> ??????????          
		sagittal				   sagittal
		transverse			   transverse         
		vegetal pole			vegetal pole  
     

		fimgprep_name        speciman
      -------------
		live
		LM-section				LM-section
		not specified		   <no specimen selected>
		EM-section
		whole-mount			   whole-mount


	}
   ;replace/all buffer "|<no view selected>|" "|not specified|"

	
	replace/all buffer "|<no direction selected>|" "|not specified|"		
	replace/all buffer "|<no specimen selected>|"  "|not specified|"
	replace/all buffer "|side view (lateral)|"     "|side view|"
	replace/all buffer "|section|"                 "|LM-section|"
	replace/all buffer ".jpg|"                     "|"
   ;;; trim extranous spaces (again)
	replace/all buffer "| "   "|" 
	replace/all buffer " |"   "|" 
	
	write %images.unl buffer










