<%--
 This tag, in conjunction with some css in zfin.css, will draw a pretty rounded div around
 whatever comes in as the body of the tag.

 This an unfortunate example to use as the first of these jsp 2.0 tags, because there's
 hardly anything jsp related happening.  For better examples, check out:
     http://www.oracle.com/technology/pub/articles/cioroianu_tagfiles.html

 --%>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div>
    <b class="spiffy"><b class="spiffy1"><b></b></b><b class="spiffy2"><b></b></b><b class="spiffy3"></b><b
            class="spiffy4"></b><b class="spiffy5"></b></b>

    <div class="spiffy_content">
        <jsp:doBody/>
        <br class="spiffy-bottom-fix">
    </div>
    <b class="spiffy"><b class="spiffy5"></b><b class="spiffy4"></b><b class="spiffy3"></b><b class="spiffy2"><b></b></b><b
        class="spiffy1"><b></b></b></b>

</div>
