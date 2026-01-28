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
            <a href="https://main.d2u241g26l748k.amplifyapp.com/?loc=13:33,153,905..33,200,688&tracks=zfin-gene12,refseq12&assembly=GRCz12tu">ZFIN</a>
        <li class="list-inline-item">
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
            <zfin2:externalLink href="http://zebrafish.org/est/estAll.php">ZIRC</zfin2:externalLink>
        </li>
    </ul>
</z:section>