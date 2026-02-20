<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h3><a href="/${publication.zdbID}">${publication.shortAuthorList}</a></h3>
        <div class="alert alert-info mt-3">
            Figures for this publication are not available on this page due to the large number of records.
            You can view the <a href="/${publication.zdbID}">publication page</a> for other details.
            If you need information regarding these figures, please
            <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information">contact ZFIN staff</a>.
        </div>
    </div>
</z:page>
