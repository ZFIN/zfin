#! /private/bin/rebol -sqw
rebol [
	date: 22-Apr-2008
	author: "Tom Conlin"
	usage: {gen-jobs-rss.r  [/path/to/destination/dir]}
]

;;; the page the rss feed is for
feed-link: http://<!--|DOMAIN_NAME|-->/zf_info/news/jobs.html

;;; url the logo is found
feed-logo: http://<!--|DOMAIN_NAME|-->/images/zfinlogo.png

;;; who owns the lawers
feed-copyright: "University of Oregon"

;;; keywords for feed search engines
feed-categories: ["zebrafish" "research" "job"]

;;; import some functions and parse rules
;;; common to our rss feed generators
do %rss-util.r

;;; parse rules for this particular page
;;; creates XML stanza for a RSS item from this particular page
comment {
<!-- BEGIN TEMPLATE
<LI><p><a href="#NAME">Position Title</a>. (Institution) . Posted DD Month 2008</p></LI>
  ---- END TEMPLATE -->
}
rss-item: [
    thru <LI> <p> {<a href="#}
    copy item-anchor to {">} {">}
    copy item-title  to </a>     thru "("
    copy item-description to ")" thru "Posted"
    copy item-date to </p> </p> </LI>
    (make-item item-anchor item-title item-date item-description)
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; the main loop for this particular page

parse read feed-link [
    title-rule
    description-rule
    thru <UL>
    thru {END TEMPLATE -->}
    some rss-item
]

;;; from rss-util.r
make-footer

;;; from rss-util.r
write feed-file rss-xml
