<%@ page import="org.zfin.properties.ZfinProperties" %>
<style type="text/css">
    div.summary li {
        padding-top: .1em;
        padding-bottom: .1em;
    }
</style>

<div class="popup-header">
    Gene Expression Note
</div>
<div class="popup-body">
    ZFIN displays three kinds of gene expression data:

    <ol>
        <li> annotated images that have been directly submitted to ZFIN by
            researchers.
        <li> annotated figures from the current literature. Images and captions are displayed when copyright permissions
            are available.
        <li> an index of gene expression data from older publications
    </ol>

    <p>ZFIN began to include published figures in 2004. We are currently
        able to add figures from older publications only on an ad hoc basis.
        A more complete incorporation of figures from the older literature is
        a long-term goal.</p>

    <p>
        Reporter gene constructs can employ a variety of different engineered foreign genes. If, for example, a search
        for GFP expression yields no results, try RFP, YFP etc. For a full list, see
        <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerselect.apg&marker_type=EFG&query_results=t"
           target=new>all current EFG's</a>

    <p>If you have questions or suggestions or would like to submit
        expression pattern images to ZFIN, please <a href="mailto:zfinadmn@zfin.org">contact us</a>. </p>
</div>
