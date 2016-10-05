<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="hasRole('root')">


<h3>Curation</h3>
<ul>
    <li>
        View
        <a href="/action/publication/dashboard"><em>my open pubs</em></a>,
        <a href="action/publication/curating-bin"><em>curation bins</em></a>,
        <a href="action/publication/indexing-bin"><em>indexing bin</em></a>
    </li>
    <li>
        Add
        <a href="/action/marker/gene-add?type=GENE"><em>gene</em></a>,
        <a href="/action/marker/gene-add?type=GENEP"><em>pseudogene</em></a>,
        <a href="/action/marker/gene-add?type=EFG"><em>foreign gene</em></a>,
        <a href="/action/marker/clone-add"><em>clone</em></a>,
        <br>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="/action/antibody/add"><em>antibody</em></a>,
        <a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=MRPHLNO"><em>morpholino</em></a>,
        <a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=TALEN"><em>TALEN</em></a>,
        <a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=CRISPR"><em>CRISPR</em></a>,
        <br>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="/action/marker/transcript-add"><em>transcript</em></a>,
        <a href="/action/marker/region-add"><em>engineered region</em></a>
    </li>
    <li>
        Surf <a href="/action/reno/run-list"><em>ReNo pipeline</em></a>
    </li>

    <li>
        Regen
        <a href="javascript:document.maps.submit();"><em>Maps</em></a>

        <form name="fishsearch" action='/cgi-bin/runSPL.cgi'
              method="POST" >

            <input type=hidden
                   name="run"
                   value="regen_fishsearch">
        </form>

        <form name="maps" action='/cgi-bin/runSPL.cgi' method="GET" >

            <input type=hidden
                   name="run"
                   value="regen_maps">


        </form>


        <form name="ortholog" action='/cgi-bin/runSPL.cgi'
              method="POST" >
            <input type=hidden
                   name="run"
                   value="regen_oevdisp">
        </form>
    </li>
    <li>
        <script type="text/javascript">
            function handleOID(){
                if(document.quickpub.OIDpart.value.indexOf('ZDB-PUB-') != '0'){
                    document.quickpub.OID.value = 'ZDB-PUB-' + document.quickpub.OIDpart.value;
                }
                else{
                    document.quickpub.OID.value = document.quickpub.OIDpart.value;
                }
            }
        </script>

        <form name="quickpub" action="/cgi-bin/webdriver" method="get" onsubmit="handleOID() ; ">
            <input type="hidden" name="MIval" value="aa-curation.apg">
            <input type="hidden" name="OID">
            <label for="OIDpart"> Curate ZDB-PUB-</label><input name="OIDpart" id="OIDpart" size="15">
        </form>
    </li>
</ul>

<h3>Administration</h3>
<ul>
    <li> Add
        <a href="/action/profile/person/create"><em>person</em></a>,
        <a href="/action/profile/lab/create"><em>lab</em></a>,
        <a href="/action/profile/company/create"><em>company</em></a>,
        <br>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="/action/publication/new"><em>pub</em></a>,
        <a href="/action/feature/alleleDesig-add-form"><em>line designation</em></a>
    </li>
    <li>
        Find <a href="/search?category=Journal"><em>Existing Journal</em></a>
    <li>
        Generate <a href="/action/profile/distribution-list"><em>dist. list</em></a>, <a href="/action/profile/distribution-list?subset=usa"><em>USA dist. list</em></a>, <a href="/action/profile/distribution-list?subset=pi"><em>PI dist. list</em></a>
    </li>
</ul>


<h3> Development </h3>
<ul>
    <li><a href="/action/devtool/home"><em>Developer Tools</em></a> &bull; <a href="/gmake-deploy-version.html"><em>Deployed Version</em></a></li>
    &bull; <a href="/jobs"><em>Jenkins Jobs</em></a> &bull; <a href="/solr/"><em>Solr</em></a></li>
</ul>

</authz:authorize>




<h3><a href="http://zebrafish.org/" title="Zebrafish International Resource Center (ZIRC) Home">Zebrafish International Resource Center</a></h3>
<ul>
    <li>
        Request:
        <a href="http://zebrafish.org/zirc/fish/lineAll.php">Fish Lines</a>,
        <a href="http://zebrafish.org/zirc/est/estAll.php">ESTs/cDNAs</a>,
        <a href="http://zebrafish.org/zirc/abs/absAll.php">Monoclonal Antibodies</a>,
        <a href="http://zebrafish.org/zirc/orders/buyBookQ.php?item=Book&id=book&detail=The%20Zebrafish%20Book">The Zebrafish Book</a>,
        <a href="http://zebrafish.org/zirc/orders/buyParaQ.php?item=Paramecia&id=para&detail=Paramecia%20Starter%20Culture">Paramecia</a>
    </li>
    <li class="divider-above">
        <a href="http://zebrafish.org/submissions/submitTerms.php">Submit Fish Lines</a>
    <li class="divider-above">
        <a href="http://zebrafish.org/zirc/health/index.php" class="novisited">Health Services</a>
    </li>
</ul>



<h3>Genomics</h3>
<ul>
    <li>Data mining:
        <a href="http://www.zebrafishmine.org">ZebrafishMine</a>,
        <a href="http://www.ensembl.org/biomart">BioMart</a>
    </li>
    <li>Browse genome:
        <a href="@GBROWSE_PATH_FROM_ROOT@">ZFIN</a>,
        <a href="http://www.ensembl.org/Danio_rerio/">Ensembl</a>,
        <a href="http://vega.sanger.ac.uk/Danio_rerio/">Vega</a>,<br>
        <a href="http://www.ncbi.nlm.nih.gov/projects/genome/assembly/grc/zebrafish/">GRC</a>,
        <a href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&clade=vertebrate&org=Zebrafish&db=0">UCSC</a>,
        <a href="http://www.ncbi.nlm.nih.gov/projects/mapview/map_search.cgi?taxid=7955">NCBI</a>, <a href="http://genome.igib.res.in/">FishMap</a>
    </li>
    <li>
        View <a href="/cgi-bin/mapper_select.cgi"
                title="View genetic, radiation hybrid or consolidated maps">
        Genetic Maps</a>
    </li>

    <li>BLAST:
        <a href="/action/blast/blast">ZFIN</a>,
        <a href="http://www.ensembl.org/Danio_rerio/blastview">Ensembl</a>,
        <a href="http://vega.sanger.ac.uk/Multi/blastview?species=Danio_rerio">Vega</a>,
        <a href="http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastSearch&BLAST_SPEC=OGP__7955__9557">NCBI</a>,
        <a href="http://danio.mgh.harvard.edu/blast/blast.html">MGH</a>
    </li>

    <li>Find cDNAs and ESTs at
        <a href="http://zgc.nci.nih.gov/">ZGC</a>
    </li>

    <li class="divider-above">
        <a href="https://wiki.zfin.org/display/general/Genomic+Resources+for+Zebrafish">More Zebrafish Genome Resources</a>
    </li>

    <li>
        <a href="https://@WIKI_HOST@/display/general/Other+Databases">Other Fish Genomes and Model Organism Databases</a>
    </li>

</ul>


<h3><strong>Zebrafish Programs</strong></h3>
<ul>
    <li>
        <a href="http://www.nih.gov/science/models/zebrafish/">Trans-NIH Zebrafish Initiative</a>,<br>
        <a href="http://www.zf-health.org">ZF-HEALTH</a>,
        <a href="https://wiki.zfin.org/display/general/Zebrafish+Programs#husbandry">Husbandry Resources</a>,
        <a href="https://wiki.zfin.org/display/general/Zebrafish+Programs">more...</a>
    </li>
</ul>




<h3>
    News
</h3>
<ul>
    <script type="text/javascript">
        jQuery.get( '/action/wiki/summary/news?length=2', {}, function(data){
            document.getElementById('news').innerHTML = data ;
        });
    </script>

    <div id="news"></div>

    <li>
        <a href="/zf_info/news/Newsletters.html">ZFIN Newsletters</a>,
        <!--<a href="/zf_info/news/siteNews.html">News Archive</a>-->
        <a href="@SECURE_HTTP@@WIKI_HOST@/display/news">News Archive</a>
    </li>

    <br>

</ul>

<img id="zdbhome-fishimg" src="/images/betterfish.jpg">


