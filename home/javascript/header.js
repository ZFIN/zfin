

 document.write('<LINK rel=stylesheet type="text/css" href="/css/zfin.css">');
 document.write('<LINK rel=stylesheet type="text/css" href="/css/header.css">'); 




function addScript(url) {
  eltScript = document.createElement("script");
  eltScript.setAttribute("type", "text/javascript");
  eltScript.setAttribute("src", url);
  document.getElementsByTagName('head')[0].appendChild(eltScript);
}


addScript("/javascript/prototype.js");


/** mostly this kind of thing is bad, but thers a 
    tiny bit of css we want for IE only  **/

if (navigator.appName == 'Microsoft Internet Explorer') {
  document.write("<style type='text/css'>"); 
//  document.write("body { width:expression(document.body.clientWidth < 600? '600px': 'auto' );}  ");
//  document.write("#hdr-banner {   width:expression(document.body.clientWidth < 580? '580px': 'auto' );  } "); 


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





/* tab functions */



function hdrSetTabs() {

	/** this requires conversion from the apg version of the header, 
        show_motto is always false here, because the home page never
        calls the js version of the header **/
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



function deselectTabs(tab_id) {

  $('researchspiffy').className = "spiffy";
  $('researchspiffy1').className = "spiffy1";
  $('researchspiffy2').className = "spiffy2";
  $('researchspiffy3').className = "spiffy3";
  $('researchspiffy4').className = "spiffy4";
  $('researchspiffy5').className = "spiffy5";
  $('researchTabContent').className = "tabContent";

  $('generalspiffy').className = "spiffy";
  $('generalspiffy1').className = "spiffy1";
  $('generalspiffy2').className = "spiffy2";
  $('generalspiffy3').className = "spiffy3";
  $('generalspiffy4').className = "spiffy4";
  $('generalspiffy5').className = "spiffy5";
  $('generalTabContent').className = "tabContent";


  $('productspiffy').className = "spiffy";
  $('productspiffy1').className = "spiffy1";
  $('productspiffy2').className = "spiffy2";
  $('productspiffy3').className = "spiffy3";
  $('productspiffy4').className = "spiffy4";
  $('productspiffy5').className = "spiffy5";
  $('productTabContent').className = "tabContent";


}


function showZFINLinks() {
  hdrSetCookie("tabCookie","Research","","/");

  deselectTabs();

  $('researchspiffy').className = "selectedspiffy";
  $('researchspiffy1').className = "selectedspiffy1";
  $('researchspiffy2').className = "selectedspiffy2";
  $('researchspiffy3').className = "selectedspiffy3";
  $('researchspiffy4').className = "selectedspiffy4";
  $('researchspiffy5').className = "selectedspiffy5";
  $('researchTabContent').className = "selectedTabContent";



  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-zfinlinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();


}

function showGeneralLinks() {
  hdrSetCookie("tabCookie","General","","/");

  deselectTabs();

  $('generalspiffy').className = "selectedspiffy";
  $('generalspiffy1').className = "selectedspiffy1";
  $('generalspiffy2').className = "selectedspiffy2";
  $('generalspiffy3').className = "selectedspiffy3";
  $('generalspiffy4').className = "selectedspiffy4";
  $('generalspiffy5').className = "selectedspiffy5";
  $('generalTabContent').className = "selectedTabContent";



  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();
}




function showZIRCLinks() {
  
  hdrSetCookie("tabCookie","Products","","/");

  deselectTabs();

  $('productspiffy').className = "selectedspiffy";
  $('productspiffy1').className = "selectedspiffy1";
  $('productspiffy2').className = "selectedspiffy2";
  $('productspiffy3').className = "selectedspiffy3";
  $('productspiffy4').className = "selectedspiffy4";
  $('productspiffy5').className = "selectedspiffy5";
  $('productTabContent').className = "selectedTabContent";


  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "block";

  document.getElementsByTagName("head")[0].blur();
}

function showMotto() {
  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "block";
}


/* end tab functions */


document.write(" <body id=\"body\" onload=\"hdrSetTabs();\">");
document.write("   <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-ZDB_home.apg\">");
document.write("    <img id=\"logo-img\" src=\"/images/zfinlogo.png\"> ");
document.write("    <img id=\"logo-text\" src=\"/images/zfintxt.png\">");
document.write("   </a>");
document.write("   <noscript>");
document.write("    <div id=\"noscriptWarningBox\">");
document.write("      Many ZFIN features require that javascript be enabled.");
document.write("    </div>");
document.write("   </noscript>");
document.write("   <div id=\"quicksearchBox\">");
document.write("     <form method=\"GET\" action=\"/action/quicksearch\" name=\"quicksearch\">");
document.write("       <label for=\"qsearch\" id=\"quicksearchLabel\">");
document.write("         <a href=\"/zf_info/syntax_help.html\">");
document.write("           Site Search:");
document.write("         </a>");
document.write("       </label>");
document.write("       <input type=\"text\" size=\"25\" name=\"query\" id=\"qsearch\">");
document.write("     </form>");
document.write("   </div>");
document.write(" <div id=\"hdr-banner\">");
document.write("   <div id=\"hdr-tabs\">    ");
document.write("      <div id=\"researchtab\">");
document.write("      <b class=\"spiffy\" id=\"researchspiffy\"><b class=\"spiffy1\"  id=\"researchspiffy1\"><b></b></b><b class=\"spiffy2\"  id=\"researchspiffy2\"><b></b></b><b class=\"spiffy3\"  id=\"researchspiffy3\"></b><b class=\"spiffy4\" id=\"researchspiffy4\"></b><b class=\"spiffy5\" id=\"researchspiffy5\"></b></b>");
document.write("      <div class=\"tabContent\" id=\"researchTabContent\" >");
document.write("        Research");
document.write("      </div>");
document.write("      </div>");

document.write("      <div id=\"generaltab\"> ");
 document.write("     <b class=\"spiffy\" id=\"generalspiffy\"><b class=\"spiffy1\" id=\"generalspiffy1\"><b></b></b><b class=\"spiffy2\" id=\"generalspiffy2\"><b></b></b><b class=\"spiffy3\" id=\"generalspiffy3\"></b><b class=\"spiffy4\" id=\"generalspiffy4\"></b><b class=\"spiffy5\" id=\"generalspiffy5\"></b></b>");
document.write("      <div class=\"tabContent\" id=\"generalTabContent\">");
document.write("        General Information");
document.write("      </div>");
document.write("      </div>");
document.write("      <div id=\"zirctab\">");
document.write("      <b class=\"spiffy\" id=\"productspiffy\"><b class=\"spiffy1\" id=\"productspiffy1\"><b></b></b><b class=\"spiffy2\" id=\"productspiffy2\"><b></b></b><b class=\"spiffy3\" id=\"productspiffy3\"></b><b class=\"spiffy4\" id=\"productspiffy4\"></b><b class=\"spiffy5\" id=\"productspiffy5\"></b></b>");
 document.write("     <div class=\"tabContent\"  id=\"productTabContent\">");
 document.write("      ZIRC");
 document.write("     </div>");
document.write("      </div>");

document.write("      <script type=\"text/javascript\">");
document.write("      if (document.addEventListener) {  ");
document.write("        document.getElementById(\"researchtab\").addEventListener(\"click\",showZFINLinks,true);");
document.write("        document.getElementById(\"generaltab\").addEventListener(\"click\",showGeneralLinks,true);");
document.write("        document.getElementById(\"zirctab\").addEventListener(\"click\",showZIRCLinks,true);");

document.write("      } else {");
document.write("        document.getElementById(\"researchtab\").onclick = showZFINLinks;");
document.write("        document.getElementById(\"generaltab\").onclick = showGeneralLinks;");
document.write("        document.getElementById(\"zirctab\").onclick = showZIRCLinks;");

document.write("      }");
document.write("      </script>");


document.write("   </div>");
document.write("   <div id=\"hdr-navlinks\">");
document.write("     <div id=\"hdr-zirclinks\" class=\"hdr-linkbar\"> ");
document.write("       <a href=\"/zirc/\">ZIRC Home</a>");
 document.write("      <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <strong> Request: </strong> &nbsp; <a href=\"/zirc/fish/lineAll.php\">Fish</a>,");
document.write("       &nbsp; <a href=\"/zirc/est/estAll.php\">ESTs/cDNAs</a>,");
document.write("       &nbsp; <a href=\"/zirc/abs/absAll.php\">Antibodies</a>,");
 document.write("      &nbsp; <a href=\"/zirc/orders/buyBookQ.php?item=Book&amp;id=book&amp;detail=The%20Zebrafish%20Book\"><span style=\"font-style:italic;\">ZF Book</span></a>,");
document.write("       &nbsp; <a href=\"/zirc/orders/buyParaQ.php?item=Paramecia&amp;id=para&amp;detail=Paramecia%20Starter%20Culture\" >Paramecia</a>&nbsp;");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
 document.write("      <a href=\"/zirc/documents/fees.php\">Prices</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/zirc/documents/payment.php\">Payment Help</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/zirc/documents/health.php\">Health Services</a>");
document.write("     </div>");
document.write("     <div id=\"hdr-zfinlinks\" class=\"hdr-linkbar\"> ");
document.write("       <a href=\"/\">Home</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-newmrkrselect.apg\">Genes / Markers / Clones</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-xpatselect.apg\">Expression</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/action/antibody/search\">Antibodies</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("        <a href=\"/action/blast/blast\" title=\"Search for sequence alignment against ZFIN datasets and Zebrafish datasets\">BLAST</a> ");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-fishselect.apg&line_type=mutant\">Mutants / Morphants / Tg</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
 document.write("      <a href=\"/action/anatomy/search\">Anatomy</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/<!--|CGI_BIN_DIR_NAME|-->/mapper_select.cgi\" title=\"View genetic, radiation hybrid or consolidated maps\">Maps</a>");
 document.write("      <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-pubselect2.apg&select_from=PUBLICATION\">Publications</a>");
document.write("     </div>");
document.write("     <div id=\"hdr-generallinks\" class=\"hdr-linkbar\">");
document.write("     <a href=\"/\"></a> "); 
document.write("     <a href=\"/\">Home</a> "); 
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-quickfindpers.apg&select_from=PERSON&frame_size=75\">People</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-labselect.apg&select_from=LAB\" title=\"Search for laboratories by name, address or research interests\">Laboratories</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-companyselect.apg&select_from=COMPANY&frame_size=230\" title=\"Search for companies supplying zebrafish reagents\">Companies</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/news/jobs.html\">Jobs</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/news/mtgs.html\">Meetings</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/zfbook/zfbk.html\" title=\"Browse the Zebrafish Book\">View <em>ZF Book</em></A>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
 document.write("    <a href=\"/zf_info/news/newsgroup.html\" title=\"a moderated, online discussion group for anyone interested in zebrafish research\">ZF Newsgroup</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/contact_us.html\">Contact ZFIN</A>");
document.write("    </div>");
document.write("    <div id=\"hdr-motto\" class=\"hdr-linkbar\">   ");  
document.write("      The Zebrafish Model Organism Database");
document.write("    </div>");
document.write("  </div> <!-- navlinks -->");
document.write("</div>  ");
document.write("<div class=\"allcontent staticcontent\">  ");
