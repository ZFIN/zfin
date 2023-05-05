<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <script type="text/javascript" language="javascript"
            src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

    <script type="text/javascript">
        var MarkerProperties= {
            zdbID: "${formBean.marker.zdbID}"
        };
    </script>

    <authz:authorize access="hasRole('root')">

        <c:set var="deleteURL">none</c:set>
        <zfin2:dataManager
                    zdbID="${formBean.marker.zdbID}"
                    editURL=""
                    deleteURL="${deleteURL}"/>

        <div style="font-size: large; text-align: center;">
                <a href="/action/marker/sequence/view/${formBean.marker.zdbID}">[View Sequences]</a>
        </div>

        <zfin2:sequenceHead gene="${formBean.marker}"/>

        <div id="newProteinSequence"></div>

        <div id="newStemLoopSequence"></div>

        <div class="__react-root"
             id="SequenceInformationEdit"
             data-marker-id="${formBean.marker.zdbID}"
        ></div>

        <script src="${zfn:getAssetPath("react.js")}"></script>

    </authz:authorize>
</z:page>



