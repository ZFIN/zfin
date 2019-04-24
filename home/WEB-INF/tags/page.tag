<!doctype html>

<%@ tag pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ attribute name="title" %>

<html lang="en">
<!-- Server: @INSTANCE@ -->
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>${title}</title>

    <link rel="stylesheet" href="/css/searchresults.css">
    <link rel="stylesheet" href="/css/summary.css">
    <link rel="stylesheet" href="/css/Lookup.css">
    <link rel="stylesheet" href="/css/datapage.css">
    <link rel="stylesheet" href="/css/popup.css">
    <link rel="stylesheet" href="/css/tipsy.css">
    <link rel="stylesheet" href="/css/jquery.modal.css">
    <link rel="stylesheet" href="/css/typeahead.css">
    <link rel="stylesheet" href="/css/zfin.css">
    <link rel="stylesheet" href="/css/header.css">
    <link rel="stylesheet" href="/css/footer.css">

    <script src="/javascript/dist/vendor-common.bundle.js"></script>
    <script src="/javascript/dist/zfin-common.bundle.js"></script>

    <script>
        <c:choose>
        <c:when test="${ZfinPropertiesEnum.GOOGLE_ANALYTICS_ID != '0'}">
        !function(z,b,r,f,i,s,h){z.GoogleAnalyticsObject=i,z[i]=z[i]||function(){(z[i].q=z[i].q||[]).push(arguments)},z[i].l=+new Date,s=b.createElement(r),h=b.getElementsByTagName(r)[0],s.src=f,h.parentNode.insertBefore(s,h)}(this,document,"script","//www.google-analytics.com/analytics.js","ga");
        ga('create', '@GOOGLE_ANALYTICS_ID@', {'cookieDomain': 'zfin.org'});
        ga('send', 'pageview');
        </c:when>
        <c:otherwise>
        window.ga = window.ga || function () {
        };
        </c:otherwise>
        </c:choose>

        $(function () {
            processPopupLinks('body');
            $(".default-input").focus();
            $('#header-search-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY');
            $('#header-search-query-input').bind("typeahead:select", function () {
                $('#header-query-form').submit();
            });
        });

        if (hdrGetCookie("tabCookie") === "Motto") {
            hdrSetCookie("tabCookie", "Research", "", "/");
        }
    </script>
</head>
<body id="body">
<a id="top"></a>
<header class="zfin-header">
    <a href="/">
        <img id="logo-img" src="/images/zfinlogo.png" alt="header logo image">
        <img id="logo-text" src="/images/zfintxt.png" alt="header logo text">
    </a>
    <noscript>
        <div id="noscriptWarningBox">
            Many ZFIN features require that javascript be enabled.
        </div>
    </noscript>
    <div id="quicksearchBox">
        <form id="header-query-form" method="GET" action="/search" name="faceted-search" accept-charset="utf-8">
            <label for="header-search-query-input">Search</label>
            <input class="search-form-input input" style="width: 300px;" name="q" id="header-search-query-input"
                   autocomplete="off" type="text"/><input type="submit" style="visibility: hidden; position: fixed;"/>
        </form>
    </div>
    <div id="feedBox">
        <span class="site-link"><a href="/downloads">Downloads</a></span>
        <span class="site-link">
            <a id="hdr-login-link" href="/action/login">Login</a>
            <a id="hdr-logout-link" style="display:none" href="/action/logout">Logout</a>
        </span>
        <a class="social-icon" href="https://@WIKI_HOST@/createrssfeed.action?types=blogpost&spaces=news&title=Zebrafish+News&labelString%3D&excludedSpaceKeys%3D&sort=modified&maxResults=10&timeSpan=120&showContent=true">
            <i class="fas fa-rss-square"></i>
        </a>
        <a class="social-icon" href="https://twitter.com/zfinmod">
            <i class="fab fa-twitter-square"></i>
        </a>
    </div>
    <div id="hdr-banner">
        <div id="hdr-tabs">
            <div class="header-tab research">Research</div>
            <div class="header-tab general">General Information</div>
            <div class="header-tab zirc">ZIRC</div>
        </div>
        <div id="hdr-navlinks">
            <div id="hdr-zirclinks" style="display: none;" class="hdr-linkbar">
                <a href="http://zebrafish.org/">ZIRC Home</a>
                <strong>Request: </strong> <a href="/zirc/fish/lineAll.php">Fish</a>
                <a href="/zirc/est/estAll.php">ESTs/cDNAs</a>
                <a href="/zirc/abs/absAll.php">Antibodies</a>
                <a href="/zirc/orders/buyBookQ.php?item=Book&amp;id=book&amp;detail=The%20Zebrafish%20Book"><span style="font-style:italic;">ZF Book</span></a>
                <a href="/zirc/orders/buyParaQ.php?item=Paramecia&amp;id=para&amp;detail=Paramecia%20Starter%20Culture">Paramecia</a>
                <a href="/zirc/documents/fees.php">Prices</a>
                <a href="/zirc/documents/payment.php">Payment Help</a>
                <a href="/zirc/health/index.php">Health Services</a>
            </div>
            <div id="hdr-zfinlinks" class="hdr-linkbar">
                <a href="/">Home</a>
                <a href="/action/marker/search" title="Search by name, accession number, chromosome, vector or sequence type" id="hdr-gmc-search">Genes / Markers / Clones</a>
                <a href="/action/blast/blast" title="Search for sequence alignment against ZFIN datasets and zebrafish datasets">BLAST</a>
                <a href="/@GBROWSE_PATH_FROM_ROOT@">GBrowse</a>
                <a href="/action/expression/search" title="Search by gene, developmental stage, anatomy and other attributes">Expression</a>
                <a href="/action/antibody/search" title="Search for antibodies by gene, labeled anatomy and other attributes">Antibodies</a>
                <a href="/action/fish/search">Mutants / Knockdowns / Tg</a>
                <a href="/search?q=&fq=category%3A%22Construct%22&category=Construct">Constructs</a>
                <a href="/action/ontology/search" title="Search anatomy and gene ontology">Anatomy / GO / Human Disease</a>
                <a href="/action/publication/search" title="Search for zebrafish research publications by author, title or citation">Publications</a>
            </div>
            <div id="hdr-generallinks" style="display: none;" class="hdr-linkbar">
                <a href="/">Home</a>
                <a href="https://@WIKI_HOST@/" title="ZFIN-hosted community wiki: browse, contribute and export">Wiki</A>
                <a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements" title="Zebrafish-related job announcements">Jobs</a>
                <a href="https://@WIKI_HOST@/display/meetings" title="Zebrafish-related meeting announcements">Meetings</a>
                <a href="https://@WIKI_HOST@/display/general/Zebrafish+Newsgroup+Information" title="Moderated, online discussion group for anyone interested in zebrafish research">Newsgroup</a>
                <a href="/action/profile/person/search" title="Search for zebrafish researchers by name or address">People</a>
                <a href="/action/profile/lab/search" title="Search for laboratories by name, address or research interests">Labs</a>
                <a href="/action/profile/company/search" title="Search for companies supplying zebrafish reagents">Companies</a>
                <a href="/zf_info/news/education.html" title="Educational websites for students and educators">Education</A>
                <a href="/zf_info/zfbook/zfbk.html" title="Browse The Zebrafish Book"><em>ZF Book</em></A>
                <a href="https://@WIKI_HOST@/display/general/ZFIN+Contact+Information" title="ZFIN contact information">Contact</A>
                <a href="https://wiki.zfin.org/display/general/ZFIN+db+information" title="About ZFIN and citing ZFIN resources in publications">About</A>
            </div>
            <div id="hdr-motto" style="display:none;" class="hdr-linkbar">
                The Zebrafish Information Network
            </div>
        </div>
    </div>
</header>
<div class="allcontent">
    <jsp:doBody/>
</div>
<div id="footer">
    <div id="footerlinks">
        <a href="/">Home</a>
        <a href="/ZFIN/misc_html/tips.html" title="Frequently asked questions">Help and Tips</a>
        <a href="/zf_info/glossary.html"
           title="Terms useful in understanding zebrafish development, anatomy, genetics and bioinformatics">Glossary</a>
        <a href="/zf_info/news/committees.html" title="Committees and working groups">Committees</a>
        <a href="https://wiki.zfin.org/display/general/ZFIN+db+information"
           title="Citing ZFIN resources in publications">Citing ZFIN</a>
        <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information" title="ZFIN contact information">Contact</a>
        <a href="https://wiki.zfin.org/display/general/ZFIN+db+information" title="About ZFIN">About</a>
        <a href="https://wiki.zfin.org/display/jobs/ZFIN+Jobs" title="Jobs at ZFIN">ZFIN Jobs</a>
    </div>
    <div id="footercredits" class="center">
        <span id="footer-generous-support">Development of the Zebrafish Database is generously supported by the National Human Genome Research Institute (HG004838, and HG004834) of the National Institutes of Health. <br></span>
        <a href="/zf_info/warranty.html">Disclaimer, limitations, copyright &copy; </a> <a href="http://www.uoregon.edu">
        University of Oregon</a>, 1994-2019,<a href="http://www.ci.eugene.or.us"> Eugene</a>, Oregon. <br>
        <small>ZFIN logo design by Kari Pape, <A HREF="http://www.uoregon.edu">University of Oregon</a></small>
    </div>
</div>
</body>
</html>
