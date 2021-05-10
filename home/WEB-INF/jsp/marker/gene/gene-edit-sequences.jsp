<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


    <script type="text/javascript" language="javascript"
            src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>



    <script type="text/javascript">
        var MarkerProperties= {
            zdbID: "${gene.zdbID}"
        };
    </script>


<c:if test="${!fn:contains(gene.zdbID,'MIRNAG')}">
        <div id="newProteinSequence"></div>
</c:if>

        <div id="newStemLoopSequence"></div>






