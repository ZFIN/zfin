<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   mergeURL="${formBean.mergeURL}"/>

<zfin2:antibodyMarkerHeader antibodyBean="${formBean}"/>

<zfin2:externalNotes notes="${formBean.externalNotes}"/>

<div id="antibody-labeling">
    <zfin2:antibodyLabeling formBean="${formBean}"/>
</div>

<div class="summary">
    <a href="/action/antibody/antibody-publication-list?antibodyID=${formBean.marker.zdbID}&orderBy=author">CITATIONS</a>
    (${formBean.numPubs})
</div>

<script>
    jQuery(function () {
        jQuery("#antibody-labeling").tableCollapse({label: "labeled structures"});
    });
</script>
