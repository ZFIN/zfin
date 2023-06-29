<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.featureflag.FeatureFlagEnum" %>

<z:section title="Additional Resources">
    <b>Data Mining</b>
    <ul class="list-inline">
        <li class="list-inline-item">
            <zfin2:externalLink href="https://www.alliancegenome.org/bluegenes/alliancemine">AllianceMine</zfin2:externalLink>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="https://ensembl.org/biomart/martview">BioMart</zfin2:externalLink>
        </li>
    </ul>

    <b>Browse Genome</b>
    <ul class="list-inline">
        <li class="list-inline-item">
            <c:choose>
                <c:when test="${zfn:isFlagEnabled(FeatureFlagEnum.JBROWSE)}">
                    <a href="/jbrowse/?data=data/GRCz11">ZFIN</a>
                </c:when>
                <c:otherwise>
                    <a href="/@GBROWSE_PATH_FROM_ROOT@">ZFIN</a>
                </c:otherwise>
            </c:choose>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="http://www.ensembl.org/Danio_rerio/">Ensembl</zfin2:externalLink>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="http://www.ncbi.nlm.nih.gov/projects/genome/assembly/grc/zebrafish/">GRC</zfin2:externalLink>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&amp;clade=vertebrate&amp;org=Zebrafish&amp;db=0">UCSC</zfin2:externalLink>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="https://www.ncbi.nlm.nih.gov/genome/gdv/?org=danio-rerio">NCBI</zfin2:externalLink>
        </li>
    </ul>

    <b>Order cDNAs and ESTs</b>
    <ul class="list-inline">
        <li class="list-inline-item">
            <zfin2:externalLink href="http://zgc.nci.nih.gov/">ZGC</zfin2:externalLink>
        </li>
        <li class="list-inline-item">
            <zfin2:externalLink href="http://zebrafish.org/est/estAll.php">ZIRC</zfin2:externalLink>
        </li>
    </ul>
</z:section>