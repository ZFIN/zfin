<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.featureflag.FeatureFlagEnum" %>

<z:section title="About ZFIN">
    <!-- Example Feature Flag Use -->
    <c:if test="${zfn:isFlagEnabled(FeatureFlagEnum.CURATOR_JOB_POSTING)}">
        <p class="lead">
            <a href="https://zfin.atlassian.net/wiki/spaces/jobs/pages/2996503058/ZFIN+Jobs">
                ZFIN is hiring a curator! </a>
        </p>
    </c:if>
    The Zebrafish Information Network (ZFIN) is the database of genetic and genomic data for the
    zebrafish (<i>Danio rerio</i>) as a model organism. ZFIN provides a wide array of expertly curated,
    organized and cross-referenced zebrafish research data.
    </p>
    <a href="https://wiki.zfin.org/display/general/ZFIN+Database+Information">Learn More</a>
</z:section>
