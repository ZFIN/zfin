#! /home/users/tomc/bin/rebol -csq
rebol[
        Author: "Tom Conlin"
        Date: 2001-Jul-19
        File: %cgiemail.cgi
]
;;; see: rebol.com for free "rebol/core" interperter

print "Content-Type: text/html^/"

maintainer-email: tomc@cs.uoregon.edu
set-net [zfishweb@cs.uoregon.edu mailhost.cs.uoregon.edu none none none]

lock-file: %/tmp/stkctr.lock
;lock-file: %../stkctr.lock

output-file:   %<!--|FTP_ROOT|-->/pub/stockcenter/stockcenter.txt
;output-file:    %/cs/ftp/pub/tomc/stockcenter.txt

;;; TO-DO read a local config file that contains domains to accept
;;; template files from

;;; url-decode and parse the string into a block of name-value pairs
cgi: copy ""
either system/options/cgi/request-method = "POST"[
        ;;; read the block of name = value pairs
        ;;; see how long the string we need to read is
        len: load system/options/cgi/content-length

        ;;; make an empty string that is long enough
        ;;; read the string from stdin
	buffer: make string! (:len + 8)
        while [not zero? read-io system/ports/input buffer :len]
                [append  cgi buffer clear buffer]
        ]
        [   either system/options/cgi/request-method = "GET"
            [cgi: copy system/options/cgi/query-string]
            [print "implement command line testing already"]
        ]
        cgi: decode-cgi cgi
	keyval: copy []
        foreach x cgi [append/only keyval to-string :x ]
	
	if any[ (not find keyval "template") (not find keyval "success") 
	][
		print {<html>
        <head>
                <title>Failed</title>
        </head>
        <body>
                <h1> Required data missing</h1>
                <b>your form submission consisted of</b><p>
		<pre>
}
                foreach [k v] keyval [ print [k " -> " v ]]
                print "</pre></body></html>"
                quit
		         
	]

	;;; check-that the template is from somwhere we have agreed to read from 
        if not find (select keyval "template") "http://<!--|DOMAIN_NAME|-->/"[ print "we are coming to get you now" q]
        template: read to-url (select keyval "template")

        placeholders: copy []
        ;alfa: charset {#"A"=#"Z" #"a"-#"z"}

        mark: copy ""
        parse template [
                any [ (mark: copy  "")
                        to "[" copy mark thru "]"
                        (append placeholders mark)
                ]                
                to end
        ]
        sort/compare placeholders func[a b][(length? a ) > (length? b )]

        ;;; replace the place holders in the template with the values from the cgi
        foreach [k v] keyval [
                k: rejoin[ "[" :k "]" ]
                if all[ (equal? v "") (equal? copy/part k 10  "[required_") ][
                        v: rejoin[ {<font color="red">**} :k {</font>} ]
                ]
                replace/all template k v
        ]

        ;;; balk if a required field is missing
        if find template {<font color="red">**[required_}[
                template: copy next find template "^/^/"
                print 
{<html>
	<head>
		<title>Failed</title>
	</head>
	<body>
		<h1> Required fields missing</h1>
		<b>Please go back and fill in all required fields</b><p>
}
                print replace/all template "^/" "<br>^/"
                print "</body></html>"
                quit
        ]
        ;;; remove leftover place holders
        ;;; such as those attached to unchecked checkboxes
        foreach ph placeholders[ replace/all template ph ""]

;];endif


;;; strip the header info out of the template
;;; build a block of addressed to send to
addy: copy/part next find template ":"  find template "^/"
template: copy next find template "^/"
frm: load copy/part next find template ":"  find template "^/"
template: copy next find template "^/"
sbj: copy/part next find template ":"  find template "^/"
template: copy next find template "^/"
;;; clean off the top of the template
while [(pick template 1) = "^/" ][remove template]


foreach ad parse addy "," [ 
    if not empty? ad: trim ad [
        ;;; build a mail header
        header: make system/standard/email [
           To:     ad
           From:   frm
           Subject: sbj
       ]
        if error? err: try[send/header to-email ad template header][                    
            send/header tomc@cs.uoregon.edu probe disarm err 
            make system/standard/email [
               To:    tomc@cs.uoregon.edu
               Subject: "cgiemail error" 
               comment: ad
            ]          
        ];; not error
    ];; ad not empty        
]          

;;; append a copy of the email to a flatfile

;print error? try[delete lock-file]

;;; make sure only one cgi writes to the file at a time

;;; need to make sure it does not wait forever
then: now/time
while [all[(not error? try[read lock-file]) (now/time - then) < 00:00:30]][wait 2]


write  lock-file ""
write/append  output-file rejoin ["# "now/date "--" now/time "^/"]
write/append  output-file template
delete lock-file

;;; output the happy ending version
;print read to-url (select keyval "success")
print [{<html>
        <head>
                <title>redirect to success</title>
		<meta http-equiv="refresh" content="0;URL=}select keyval "success"{">
		
        </head>
	<body>
		redirect to <a href=}select keyval "success"{">}select keyval "success"{</a>
	</body>
</html>
}
]


;port: open[scheme: 'tcp  host: system/options/cgi/remote-addr port-id: system/options/cgi/server-port]
;insert port "HTTP/1.1 307 Temporary Redirect^/"
;insert port rejoin ["Location: " select keyval "success" "^/^/"]
;close port
;probe  system/options/cgi
