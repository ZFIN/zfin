<%@ taglib prefix="zfin" uri="http://www.springframework.org/security/tags" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>


<script src="/javascript/header-menu.js" type="text/javascript"></script>
<script src="/javascript/your-input-welcome.js" type="text/javascript"></script>

<script>

    // causes to call onload() function to be executed upon loading the page
    function addLoadEvent(_function) {
        var _onload = window.onload;
        if (typeof window.onload != 'function') {
            if (window.onload) {
                window.onload = _function;
            } else {
                var _addEventListener = window.addEventListener || document.addEventListener;
                var _attachEvent = window.attachEvent || document.attachEvent;
                if (_addEventListener) {
                    _addEventListener('load', _function, true);
                    return true;
                } else if (_attachEvent) {
                    var _result = _attachEvent('onload', _function);
                    return _result;
                } else {
                    //todo: preloading fix for ie5.2 on mac os
                    return false;
                }
            }
        } else {
            window.onload = function() {
                _onload();
                _function();
            }
        }
    }

    function randomUniqueID(prefix) {
        var id = prefix + Math.floor( Math.random()*99999 );
        // if the id exists, try again
        if ( document.getElementById(id) != undefined ) {
            return randomUniqueID(prefix);
        } else {
            return id;
        }


    }

    function processPopupLinks(parent) {
        //for each popup link, create a div to store the popup contents
        var selector = parent + ' .popup-link ';

        jQuery(selector).each(function() {
            div_id = randomUniqueID("popup-");
            jQuery(this).attr("rel", "#" + div_id);
            div_html = "<div class=\"modal\" id=\"" + div_id + "\"><div class=\"popup-content\">Loading... <img src=\"/images/ajax-loader.gif\"/></div></div>";
            //append to the body so that we don't get unwanted css rules
            jQuery('body').append(div_html);
            if (jQuery(this).modal != undefined) {
                this.style.display = "inline";
            }

        });

        jQuery(selector).click(function (event) {
            event.preventDefault();
            var overlay = jQuery(jQuery(this).attr("rel"));
            jQuery.ajax({
                url: jQuery(this).attr('href'),
                success: function(data) {
                    jQuery(".popup-content", overlay).html(data);
                    overlay.modal({
                        fadeDuration: 100
                    });
                }
            });
        });
    }



    jQuery.noConflict();

    jQuery(document).ready(function() {
        processPopupLinks('body');
        initYIW();
    });

</script>

<body id="body" >
<%--
<a href="/">
    <img id="logo-img" src="/images/zfinlogo.png">
    <img id="logo-text" src="/images/zfintxt.png">
</a>
<noscript>
    <div id="noscriptWarningBox">
        Many ZFIN features require that javascript be enabled.
    </div>
</noscript>
<div id="quicksearchBox">
    <form method="GET" action="/action/quicksearch/query" name="quicksearch">
        <label for="qsearch" id="quicksearchLabel">
            <a href="/zf_info/syntax_help.html">
                Site Search:
            </a>
        </label>
        <input type="text" size="25" name="query" id="qsearch">
    </form>
</div>

<div id="header-extras">
   <span id="downloads-link">
      <a href="/downloads">Download Files</a>
   </span>
   <span id="login-link">
          <a href="/action/login">Login</a>
   </span>
</div>

        <div id="feedBox">
            <a href="feed:https://wiki.zfin.org/spaces/createrssfeed.action?types=blogpost&spaces=news&sort=created&title=Zebrafish+News&publicFeed=true&rssType=rss2&maxResults=-1&timeSpan=120"><img id="rss-icon" src="/images/feed-icon-28x28.png"></a>
            <iframe allowtransparency="true" frameborder="0" scrolling="no"
                    src="//platform.twitter.com/widgets/follow_button.html?screen_name=ZFINmod&show_count=false&show_screen_name=false"
                    style="width:60px; height:20px;"></iframe>
        </div>


<div id="hdr-banner">
    <div id="hdr-tabs">
        <div id="researchtab">
            <b class="spiffy" id="researchspiffy"><b class="spiffy1" id="researchspiffy1"><b></b></b><b class="spiffy2"
                                                                                                        id="researchspiffy2"><b></b></b><b
                    class="spiffy3" id="researchspiffy3"></b><b class="spiffy4" id="researchspiffy4"></b><b
                    class="spiffy5" id="researchspiffy5"></b></b>

            <div class="tabContent" id="researchTabContent">
                Research
            </div>
        </div>

        <div id="generaltab">
            <b class="spiffy" id="generalspiffy"><b class="spiffy1" id="generalspiffy1"><b></b></b><b class="spiffy2"
                                                                                                      id="generalspiffy2"><b></b></b><b
                    class="spiffy3" id="generalspiffy3"></b><b class="spiffy4" id="generalspiffy4"></b><b
                    class="spiffy5" id="generalspiffy5"></b></b>

            <div class="tabContent" id="generalTabContent">
                General Information
            </div>
        </div>
        <div id="zirctab">
            <b class="spiffy" id="productspiffy"><b class="spiffy1" id="productspiffy1"><b></b></b><b class="spiffy2"
                                                                                                      id="productspiffy2"><b></b></b><b
                    class="spiffy3" id="productspiffy3"></b><b class="spiffy4" id="productspiffy4"></b><b
                    class="spiffy5" id="productspiffy5"></b></b>

            <div class="tabContent" id="productTabContent">
                ZIRC
            </div>
        </div>

        <script type="text/javascript">
            if (document.addEventListener) {
                document.getElementById("researchtab").addEventListener("click", showZFINLinks, true);
                document.getElementById("generaltab").addEventListener("click", showGeneralLinks, true);
                document.getElementById("zirctab").addEventListener("click", showZIRCLinks, true);

            } else {
                document.getElementById("researchtab").onclick = showZFINLinks;
                document.getElementById("generaltab").onclick = showGeneralLinks;
                document.getElementById("zirctab").onclick = showZIRCLinks;

            }
        </script>


    </div>
    <div id="hdr-navlinks">
        <div id="hdr-zirclinks" style="display: none;" class="hdr-linkbar">
            <a href="http://zebrafish.org">ZIRC Home</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <strong> Request: </strong> &nbsp; <a href="http://zebrafish.org/zirc/fish/lineAll.php">Fish</a>,
            &nbsp; <a href="http://zebrafish.org/zirc/est/estAll.php">ESTs/cDNAs</a>,
            &nbsp; <a href="http://zebrafish.org/zirc/abs/absAll.php">Antibodies</a>,
            &nbsp; <a href="http://zebrafish.org/zirc/orders/buyBookQ.php?item=Book&id=book&detail=The%20Zebrafish%20Book"><span
                style="font-style:italic;">ZF Book</span></a>,
            &nbsp; <a
                href="http://zebrafish.org/zirc/orders/buyParaQ.php?item=Paramecia&id=para&detail=Paramecia%20Starter%20Culture">Paramecia</a>&nbsp;
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zirc/documents/fees.php">Prices</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zirc/documents/payment.php">Payment Help</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zirc/health/index.php">Health Services</a>
        </div>
        <div id="hdr-zfinlinks" class="hdr-linkbar">
            <a href="/">Home</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-newmrkrselect.apg"
               title="Search by name, accession number, chromosome, vector or sequence type">Genes / Markers / Clones</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/blast/blast"
               title="Search for sequence alignment against ZFIN datasets and zebrafish datasets">BLAST</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/@GBROWSE_PATH_FROM_ROOT@">GBrowse</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg"
               title="Search by gene, developmental stage, anatomy and other attributes">Expression</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/antibody/search"
               title="Search for antibodies by gene, labeled anatomy and other attributes">Antibodies</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">

            <a href="/action/fish/search">Mutants / Knockdowns / Tg</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/construct/search">Constructs</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/ontology/search" title="Search anatomy and gene ontology">Anatomy / GO</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/<%=ZfinProperties.getWebDriver()%>?MIval=aa-pubselect2.apg&select_from=PUBLICATION"
               title="Search for zebrafish research publications by author, title or citation">Publications</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/@CGI_BIN_DIR_NAME@/mapper_select.cgi" title="View genetic, radiation hybrid or consolidated maps">Maps</a>
        </div>
        <div id="hdr-generallinks" style="display: none;" class="hdr-linkbar">
            <a href="/"></a>


            <a href="/">Home</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="http://wiki.zfin.org"
               title="ZFIN-hosted community wiki: browse, contribute and export">Wiki</A>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="https://wiki.zfin.org/display/jobs" title="Zebrafish-related job announcements">Jobs</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="https://wiki.zfin.org/display/meetings" title="Zebrafish-related meeting announcements">Meetings</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zf_info/news/newsgroup.html"
               title="Moderated, online discussion group for anyone interested in zebrafish research">Newsgroup</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/profile/person/search"
               title="Search for zebrafish researchers by name or address">People</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/profile/lab/search"
               title="Search for laboratories by name, address or research interests">Labs</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/action/profile/company/search"
               title="Search for companies supplying zebrafish reagents">Companies</a>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zf_info/news/education.html" title="Educational websites for students and educators">Education</A>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zf_info/zfbook/zfbk.html" title="Browse The Zebrafish Book"><em>ZF Book</em></A>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zf_info/contact_us.html" title="ZFIN contact information">Contact</A>
            <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
            <a href="/zf_info/dbase/db.html" title="About ZFIN and citing ZFIN resources in publications">About</A>
        </div>
        <div id="hdr-motto" style="display:none;" class="hdr-linkbar">
            The Zebrafish Model Organism Database
        </div>
    </div>
    <!-- navlinks -->
</div>

<div class="allcontent">
--%>
