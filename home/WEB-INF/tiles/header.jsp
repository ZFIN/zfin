


<script type="text/javascript" src="/javascript/prototype.js"></script>

<script type="text/javascript">

/** mostly this kind of thing is bad, but thers a
    tiny bit of css we want for IE only  **/

if (navigator.appName == 'Microsoft Internet Explorer') {
  document.write("<style type='text/css'>");
  //document.write("body { width:expression(document.body.clientWidth < 600? '600px': 'auto' );}  ");
  //document.write("#hdr-banner {   width:expression(document.body.clientWidth < 580? '580px': 'auto' );  } ");

  /** if (IE version > 7) **/
  if (!window.XMLHttpRequest) {
    document.write("#hdr-tabs { margin-left: 95px; } ");
  }
  document.write("</style>");

}

function hdrSetCookie(name,value,expires,path,domain,secure) {
   document.cookie = name + "=" +escape(value) +
          ( (expires) ? ";expires=" + expires.toGMTString() : "") +
          ( (path) ? ";path=" + path : "") +
          ( (domain) ? ";domain=" + domain : "") +
          ( (secure) ? ";secure" : "");
   var cookieVal = document.cookie;
  }

function hdrGetCookie(name) {
   var allcookies = document.cookie;
   if (allcookies == "") return false;
   var start = allcookies.indexOf(name + '=');
   if (start == -1) return false;
   start += name.length + 1;
   var end = allcookies.indexOf(';', start);
   if (end == -1) end = allcookies.length;
   return allcookies.substring(start, end);
	}

function hdrDeleteCookie(name, path, domain) {
  var today = new Date();
  var expired = new Date(today.getTime() - 28 * 24 * 60 * 60 * 1000); // less 28 days
   if (hdrGetCookie(name)) {
      document.cookie = name + "=" +
              ((path) ? "; path=" + path : "") +
              ((domain) ? "; domain=" + domain : "") +
              "; expires=Thu, 01-Jan-70 00:00:01 GMT";
   }
}



function deselectTabs(tab_id) {

  document.getElementById('researchspiffy').className = "spiffy";
  document.getElementById('researchspiffy1').className = "spiffy1";
  document.getElementById('researchspiffy2').className = "spiffy2";
  document.getElementById('researchspiffy3').className = "spiffy3";
  document.getElementById('researchspiffy4').className = "spiffy4";
  document.getElementById('researchspiffy5').className = "spiffy5";
  document.getElementById('researchTabContent').className = "tabContent";

  document.getElementById('generalspiffy').className = "spiffy";
  document.getElementById('generalspiffy1').className = "spiffy1";
  document.getElementById('generalspiffy2').className = "spiffy2";
  document.getElementById('generalspiffy3').className = "spiffy3";
  document.getElementById('generalspiffy4').className = "spiffy4";
  document.getElementById('generalspiffy5').className = "spiffy5";
  document.getElementById('generalTabContent').className = "tabContent";


  document.getElementById('productspiffy').className = "spiffy";
  document.getElementById('productspiffy1').className = "spiffy1";
  document.getElementById('productspiffy2').className = "spiffy2";
  document.getElementById('productspiffy3').className = "spiffy3";
  document.getElementById('productspiffy4').className = "spiffy4";
  document.getElementById('productspiffy5').className = "spiffy5";
  document.getElementById('productTabContent').className = "tabContent";


}


function showZFINLinks() {
  hdrSetCookie("tabCookie","Research","","/");

  deselectTabs();

  document.getElementById('researchspiffy').className = "selectedspiffy";
  document.getElementById('researchspiffy1').className = "selectedspiffy1";
  document.getElementById('researchspiffy2').className = "selectedspiffy2";
  document.getElementById('researchspiffy3').className = "selectedspiffy3";
  document.getElementById('researchspiffy4').className = "selectedspiffy4";
  document.getElementById('researchspiffy5').className = "selectedspiffy5";
  document.getElementById('researchTabContent').className = "selectedTabContent";



  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-zfinlinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();


}

function showGeneralLinks() {
  hdrSetCookie("tabCookie","General","","/");

  deselectTabs();

  document.getElementById('generalspiffy').className = "selectedspiffy";
  document.getElementById('generalspiffy1').className = "selectedspiffy1";
  document.getElementById('generalspiffy2').className = "selectedspiffy2";
  document.getElementById('generalspiffy3').className = "selectedspiffy3";
  document.getElementById('generalspiffy4').className = "selectedspiffy4";
  document.getElementById('generalspiffy5').className = "selectedspiffy5";
  document.getElementById('generalTabContent').className = "selectedTabContent";



  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();
}




function showZIRCLinks() {

  hdrSetCookie("tabCookie","Products","","/");

  deselectTabs();

  document.getElementById('productspiffy').className = "selectedspiffy";
  document.getElementById('productspiffy1').className = "selectedspiffy1";
  document.getElementById('productspiffy2').className = "selectedspiffy2";
  document.getElementById('productspiffy3').className = "selectedspiffy3";
  document.getElementById('productspiffy4').className = "selectedspiffy4";
  document.getElementById('productspiffy5').className = "selectedspiffy5";
  document.getElementById('productTabContent').className = "selectedTabContent";


  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();
}

function showMotto() {
  /* for some reason, one of these is coming up null in IE */

  if (document.getElementById("hdr-zfinlinks") != null) {
    document.getElementById("hdr-zfinlinks").style.display = "none";
  } else { alert('hdr-zfinlinks is null'); }

  if (document.getElementById("hdr-zirclinks") != null) {
    document.getElementById("hdr-zirclinks").style.display = "none";
  } else { alert('hdr-zirclinks is null'); }

  if (document.getElementById("hdr-generallinks") != null) {
    document.getElementById("hdr-generallinks").style.display = "none";
  } else { alert('hdr-generallinks is null'); }

  if (document.getElementById("hdr-motto") != null) {
    document.getElementById("hdr-motto").style.display = "block";
  } else { alert('hdr-motto is null'); }
}




function hdrSetTabs() {


   var hdr_showmotto = false;

   if (hdr_showmotto) {
     showMotto();
   } else {

     tabCookie = hdrGetCookie("tabCookie");

     if (!tabCookie) {
       hdrSetCookie("tabCookie","Research","","/");
       tabCookie = hdrGetCookie("tabCookie");
     }
     if (tabCookie == "Research") {
       showZFINLinks();
     }
     if (tabCookie == "Products") {
       showZIRCLinks();
     }
     if (tabCookie == "General") {
       showGeneralLinks();
     }
   }
}



</script>



<body id="body" onload="hdrSetTabs();">

  <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-ZDB_home.apg">
   <img id="logo-img" src="/images/zfinlogo.png">
   <img id="logo-text" src="/images/zfintxt.png">
  </a>

  <noscript>
   <div id="noscriptWarningBox">
     Many ZFIN features require that javascript be enabled.
   </div>
  </noscript>


  <div id="quicksearchBox">
    <form method="GET" action="/SearchApp/category_search.jsp" name="quicksearch">
      <label for="qsearch" id="quicksearchLabel">
        <a href="/SearchApp/syntax_help.jsp">
          Site Search:
        </a>
      </label>
      <input type="text" size="25" name="query" id="qsearch">
    </form>
  </div>

<div id="hdr-banner">

  <div id="hdr-tabs">

     <div id="researchtab">
     <b class="spiffy" id="researchspiffy"><b class="spiffy1"  id="researchspiffy1"><b></b></b><b class="spiffy2"  id="researchspiffy2"><b></b></b><b class="spiffy3"  id="researchspiffy3"></b><b class="spiffy4" id="researchspiffy4"></b><b class="spiffy5" id="researchspiffy5"></b></b>
     <div class="tabContent" id="researchTabContent" >
       Research
     </div>
     </div>

     <div id="generaltab">
     <b class="spiffy" id="generalspiffy"><b class="spiffy1" id="generalspiffy1"><b></b></b><b class="spiffy2" id="generalspiffy2"><b></b></b><b class="spiffy3" id="generalspiffy3"></b><b class="spiffy4" id="generalspiffy4"></b><b class="spiffy5" id="generalspiffy5"></b></b>
     <div class="tabContent" id="generalTabContent">
       General Information
     </div>
     </div>

     <div id="zirctab">
     <b class="spiffy" id="productspiffy"><b class="spiffy1" id="productspiffy1"><b></b></b><b class="spiffy2" id="productspiffy2"><b></b></b><b class="spiffy3" id="productspiffy3"></b><b class="spiffy4" id="productspiffy4"></b><b class="spiffy5" id="productspiffy5"></b></b>
     <div class="tabContent"  id="productTabContent">
      ZIRC
     </div>
     </div>



     <script type="text/javascript">
     if (document.addEventListener) {
       document.getElementById("researchtab").addEventListener("click",showZFINLinks,true);
       document.getElementById("generaltab").addEventListener("click",showGeneralLinks,true);
       document.getElementById("zirctab").addEventListener("click",showZIRCLinks,true);

     } else {
       document.getElementById("researchtab").onclick = showZFINLinks;
       document.getElementById("generaltab").onclick = showGeneralLinks;
       document.getElementById("zirctab").onclick = showZIRCLinks;

     }
     </script>


  </div>






  <div id="hdr-navlinks">
    <div id="hdr-zirclinks" class="hdr-linkbar" style="display: none;">
      <a href="/zirc/">ZIRC Home</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <strong> Request: </strong> &nbsp; <a href="/zirc/fish/lineAll.php">Fish</a>,
      &nbsp; <a href="/zirc/est/estAll.php">ESTs/cDNAs</a>,
      &nbsp; <a href="/zirc/abs/absAll.php">Antibodies</a>,
      &nbsp; <a href="/zirc/orders/buyBookQ.php?item=Book&amp;id=book&amp;detail=The%20Zebrafish%20Book"><span style="font-style:italic;">ZF Book</span></a>,
      &nbsp; <a href="/zirc/orders/buyParaQ.php?item=Paramecia&amp;id=para&amp;detail=Paramecia%20Starter%20Culture" >Paramecia</a>&nbsp;
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/zirc/documents/fees.php">Fees</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/zirc/documents/payment.php">Payment Help</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/zirc/documents/health.php">Health Services</a>

    </div>
    <div id="hdr-zfinlinks" class="hdr-linkbar"
         style="display: block; ">
      <a href="/">Home</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-newmrkrselect.apg">Genes / Markers / Clones</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-xpatselect.apg">Expression</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/action/antibody/search">Antibodies</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
       <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-blast.apg" title="Search for sequence alignment against ZFIN datasets and Zebrafish datasets">BLAST</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-fishselect.apg&line_type=mutant">Mutants / Tg</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/action/anatomy/search">Anatomy</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/cgi-bin/mapper_select.cgi" title="View genetic, radiation hybrid or consolidated maps">Maps</a>
      <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
      <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-pubselect2.apg&select_from=PUBLICATION">Publications</a>
    </div>

    <div id="hdr-generallinks" class="hdr-linkbar" style="display: none;">
     <a href="/">Home</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-quickfindpers.apg&select_from=PERSON&frame_size=75">People</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-labselect.apg&select_from=LAB" title="Search for laboratories by name, address or research interests">Laboratories</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/@WEBDRIVER_LOC@/webdriver?MIval=aa-companyselect.apg&select_from=COMPANY&frame_size=230" title="Search for companies supplying zebrafish reagents">Companies</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/zf_info/news/jobs.html">Jobs</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/zf_info/news/mtgs.html">Meetings</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/zf_info/zfbook/zfbk.html" title="Browse the Zebrafish Book">View <em>ZF Book</em></A>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/zf_info/news/newsgroup.html" title="a moderated, online discussion group for anyone interested in zebrafish research">ZF Newsgroup</a>
     <img src="/images/research-dot.png" class="hdr-linkbar-divider" alt="-">
     <a href="/zf_info/contact_us.html">Contact ZFIN</A>
    </div>
    <div id="hdr-motto" class="hdr-linkbar"
         style="display: none;">
      The Zebrafish Model Organism Database
    </div>





  </div> <!-- navlinks -->
</div>
<div class="allcontent" >
