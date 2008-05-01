#! /private/bin/rebol -sqw
rebol []

;;; the page the rss feed is for
feed-link: http://<!--|DOMAIN_NAME|-->/zf_info/news/mtgs.html

;;; url the logo is found
feed-logo: http://<!--|DOMAIN_NAME|-->/images/zfinlogo.png

;;; who owns the lawers
feed-copyright: "University of Oregon"

;;; keywords for feed search engines
feed-categories: ["zebrafish" "research" "meeting"]

;;; some functions and rules common to feed generators
do %rss-util.r

;;; parse rules for this particular page
;;; common rules  exist in rss-util.r

;;; creates XML stanza for a RSS item from this particular page
comment {
<!-- BEGIN ITEM TEMPLATE

<div title="DD-MMM-YYYY">
 <h1 align="center"><a name="NAME">MEETING/COURSE NAME</a></h1>
 <h3 align="center">MEETING DATE AND LOCATION<br></h3><br>
 <p>
	...
 </p>
 <br><center><p><a href="#Meetings">Back to top</a></center><br>
</div>
<HR>
---- END ITEM TEMPLATE -->
}

rss-item: [
    thru {<div title="} copy item-date to {">}
    thru {<a name="} copy item-anchor to {">} {">}
    copy item-title to </a> </a> </h1>
    copy item-description to {<center><p><a href="#Meetings">Back to top</a></center>}
    thru </div>
    (make-item item-anchor item-title item-date item-description )
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; the main loop for this particular page

parse read feed-link [
    title-rule
    description-rule
    to{---- END ITEM TEMPLATE -->}
    some rss-item
    to end
]
make-footer
write feed-file rss-xml
