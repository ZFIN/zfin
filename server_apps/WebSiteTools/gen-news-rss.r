#! /private/bin/rebol -sqw
rebol [
	date: 22-Apr-2008
	author: "Tom Conlin"
	usage: {gen-news-rss.r  [/path/to/destination/dir]}
]

;;; the page the rss feed is for
feed-link: http://<!--|DOMAIN_NAME|-->/zf_info/news/siteNews.html

;;; url the logo is found
feed-logo: http://<!--|DOMAIN_NAME|-->/images/zfinlogo.png"

;;; who owns the lawers
feed-copyright: "University of Oregon"

;;; keywords for feed search engines
feed-categories: ["zebrafish" "research" "news"]

;;; import some functions and parse rules
;;; common to our rss feed generators
do %rss-util.r

;;; parse rules for this particular page
;;; creates XML stanza for a RSS item from this particular page
;;; standard rules may exist in rss-util.r

;;; parse rule to create a description for this particular feed
description-rule: [
    thru </table>
    copy feed-description to {<!-- BEGIN TEMPLATE}
    thru {---- END TEMPLATE -->}
    (make-description feed-description)
    ;here: (print copy/part :here 40)
]


comment {
<!-- BEGIN TEMPLATE
<h2><a name="NAME">Headline</a></h2><br>DD MMM 2008</br>
<div>
    ...
</div>
---- END TEMPLATE -->
}
rss-item: [
    thru <h2> {<a name="}
    copy item-anchor to {">} {">}
    copy item-title  to </a>  thru </h2>
    copy item-date   to <br> <br>
    (replace/all item-date "  " " ")
    thru <div>
    copy item-description to </div>
    (make-item item-anchor item-title item-date item-description)
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; the main loop for this particular page

parse read feed-link [
    title-rule
    description-rule
    to <h2>
    some rss-item
]
;;;;;;;;;;;;;;;;;;;;;;;;;;
make-footer
write feed-file  rss-xml
