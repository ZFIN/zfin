<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="canonicalUrl" value="/action/gbrowse" />

<z:page bootstrap="true">
    <div class="container mt-2">
        <div class="row">
            <div class="col-2"></div>
            <div class="col-8">
                <h3>Genome Browser Moved</h3>
                <p>For the latest genome browser, please click the link below.</p>
                <p><a class="btn btn-primary" href="/jbrowse/">Continue to jBrowse</a></p>
                <p>This page was previously the home of our instance of the gBrowse genome browser.<br/>
                   We have since migrated to the newer <a href="https://jbrowse.org/jb2/" target="_blank">jBrowse</a>.</p>
                <p>For any questions or concerns, please feel free to <a id="contact-us-link" href="#input-welcome-button">contact us</a>.</p>
            </div>
            <div class="col-2"></div>
        </div>
    </div>
    <script>
        document.getElementById('contact-us-link').addEventListener('click', function () {
            document.getElementById('input-welcome-button').click();
        });
    </script>
</z:page>
