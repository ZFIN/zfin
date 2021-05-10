rebol[
    Title: "RSS feed generating utility"
    Date: [6-Oct-2007 20-Apr-2008]
    Name: "rss-util.r"
    Version: 0.2
    File: %rss-util.r
    Home: http://zfin.org
    Author: "Tom Conlin"
    Usage:  "see script stub at bottom"
    Purpose: "simplify making RSS feeds from an existing web pages"
]
;;; common utility functions to be included in feed generators

;;; a global buffer to put the RSS results in before writeing them out
rss-xml: make string! 48000

context [
;;; deconstruct the feed url so we can use it's parts
;;; expects global feed-link from the specific generator
feed-url: decode-url feed-link

;;; create a direct link to an anchor in the page the feed is from
;;; local fx
item-link: does[join trim feed-link ["#" item-anchor]]

;;; local fx
strip-tags: func ["pulls html tags out of a string"
    str[string!] /local rslt
][
    remove-each tag rslt: load/markup str [tag? tag]
    to string! rslt
]

;;; local fx
rss-escape: func ["Escapes chars &<>- in a string intended to be served via RSS"
    str [string!]
][
    foreach[c e]["&" "&#x26;"  "<" "&#x3C;"  ">" "&#x3E;"  "-" "&#x2D;"][
     replace/all str c e
    ]str
]

;;; local fx
rel-to-abs: func ["given a string replaces any relative hrefs with absolute hrefs"
      str [string!]
 ][
    parse str [
        any[thru {<a href="}
            here:
            [{http://}  |
             [(insert :here rejoin[
                {http://} feed-url/host either #"/" = first :here [""]["/"]
               ]
              )
              8 skip
             ]
            ]
        ]
    ]
    str
]

;;; side effect sets global feed-title
set 'make-title func["puts the string into the rss feed title"
    feed-title [string!]
][  ;;; <rss version="2.0">
    insert rss-xml rejoin[
{<?xml version="1.0" encoding="utf-8"?>
 <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
   <link> }feed-link{</link>
   <atom:link href="http://}feed-url/host{/}feed-url/path copy/part feed-url/target find feed-url/target "."{.rss" rel="self" type="application/rss+xml"/>
   <title>}feed-title: trim/all/with feed-title newline{</title>
    }]
    set 'feed-title :feed-title
]

;;;
;;; expect global rss-xml feed-link feed-title
set 'make-description func["puts the string into the rss feed description"
    description [string!]
][
    insert tail rss-xml rejoin [{
  <description>
  	}rss-escape strip-tags trim/all/with description newline{
  </description>
  <image>
      <url>}feed-logo{</url>
      <title>}feed-title{</title>
      <link>}feed-link{</link>
      <description>}feed-url/host{ Logo</description>
  </image>
  <lastBuildDate>}to-idate now{</lastBuildDate>
  <language>en-us</language>
  <copyright>}feed-copyright { } now/year {</copyright>
  }
    ]
    foreach cat feed-categories [
       insert tail rss-xml rejoin[{<category>}cat{</category>}]
    ]
    insert tail rss-xml newline
]

;;;
set 'make-item func[
   "puts the strings into an rss feed item"
    item-anchor[string!]
    item-title [string!]
    item-date  [string!]
    item-description [string!]
][
    insert tail rss-xml rejoin[{
  <item>
   <title>}rss-escape strip-tags item-title{</title>
   <link>}item-link{</link>
   <guid>}item-link{</guid>
   <pubDate>}to-idate to-date trim item-date{</pubDate>
   <description>}rss-escape rel-to-abs item-description{</description>
   }]
   foreach cat feed-categories [
       insert tail rss-xml rejoin[{<category>}cat{</category>}]
   ]
insert tail rss-xml rejoin[{
  </item>
}]
]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; parse rule to create a title common to feeds
set 'title-rule [
    thru <title> copy feed-title to </title>
    (make-title trim feed-title)
]

;;; parse rule to create a description common to feeds
set 'description-rule [
    thru {<h4} thru {>} copy feed-description to </h4>
    (make-description trim feed-description)
]

;;; add a footer to the xml file
set 'make-footer does[
    insert tail rss-xml { </channel>
</rss>
}]

;;; make a .rss file name from the page name in the feed-url
;;; and the path provided on the command line
set 'feed-file does [
	change/part find/last/tail feed-url/target "." "rss" tail feed-url/target
	to file! rejoin [
		either system/script/args[system/script/args]['.]
		"/"
		feed-url/target
	]
]
];;;~context



comment { -----------------------------------------
#! /path/to/rebol -sqw
rebol [
	usage: "a stub of a script to scrape a web page into an rss feed"
]
;;; variables for the particular  page and rss feed
feed-link: http://host/page.html

;;; where on the host the logo is found "/images/zfinlogo.png"
feed-logo: http://host/image/logo.png

;;; who owns the lawers
feed-copyright: "University of Oregon"

;;; keywords for feed search engines
feed-categories: [
	zebrafish research
]

;;; import some functions and rules common to feed generators
do %rss-util.r

;;; parse rules for this particular page
;;; standard rules may exist in rss-util.r
;;; re-write or ommit them here

;;; parse rule to create a description for this particular feed
;;; you will have to adjust rule to your page
description-rule: [
    copy feed-description to ...
    (make-description feed-description)
]

;;; creates XML stanza for a RSS item from this particular page
;;; you will have to adjust rule to your page
rss-item: [
    copy item-anchor      to ...
    copy item-title       to ...
    copy item-date        to ...
    copy item-description to ...
    (make-item item-anchor item-title item-date item-description)
]

;;; the main loop for this particular page
;;; you may have to adjust to your page & rules
parse read feed-link [
    title-rule
    description-rul
    some rss-item
]
;;;;;;;;;;;;;;;;;;;;;;;;;;
make-footer
write feed-file  rss-xml

}
