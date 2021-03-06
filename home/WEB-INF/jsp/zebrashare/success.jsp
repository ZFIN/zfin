<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h1>Thanks For Your Submission</h1>
        <p>
            We have received your submission and will get to work on it soon. We will contact you if we have any questions
            or once we have finished curating it. You can view your publication here:<br>
            <zfin:link entity="${publication}">${publication.zdbID}</zfin:link>
        </p>
        <p>
            If you notice any errors please <zfin2:mailTo>contact us</zfin2:mailTo>.
        </p>
    </div>
</z:page>