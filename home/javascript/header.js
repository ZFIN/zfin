
/* Server: @INSTANCE@ */



window.__insp = window.__insp || [];
__insp.push(['wid', @INSPECTLET_ID@]);
(function() {
    function __ldinsp(){var insp = document.createElement('script'); insp.type = 'text/javascript'; insp.async = true; insp.id = "inspsync"; insp.src = ('https:' == document.location.protocol ? 'https' : 'http') + '://cdn.inspectlet.com/inspectlet.js'; var x = document.getElementsByTagName('script')[0]; x.parentNode.insertBefore(insp, x); }
if (window.attachEvent){
    window.attachEvent('onload', __ldinsp);
    }else{
    window.addEventListener('load', __ldinsp, false);
    }
})();

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

function isLoggedIn() {
    var loginCookie = getCookie('zfin_login');
    if (loginCookie === undefined || loginCookie == null)
         return false;

    if (loginCookie.lastIndexOf('GUEST') == 0)
        return false;

    return true;
}


 document.write('<LINK rel=stylesheet type="text/css" href="/css/zfin.css">');
 document.write('<LINK rel=stylesheet type="text/css" href="/css/header.css">'); 
 document.write('<LINK rel=stylesheet type="text/css" href="/css/footer.css">'); 
 document.write('<LINK rel=stylesheet type="text/css" href="/css/spiffycorners.css">');
 document.write('<link rel=stylesheet type="text/css" href="/css/spiffycorners.css">');
 document.write('<link rel=stylesheet type="text/css" href="/css/Lookup.css">');
 document.write('<link rel=stylesheet type="text/css" href="/css/datapage.css">');
 document.write('<link rel=stylesheet type="text/css" href="/css/popup.css">');
 document.write('<link rel=stylesheet type="text/css" href="/css/tipsy.css">');

<!-- Start GOOGLE Analytics -->
if ('@GOOGLE_ANALYTICS_ID@' != '0') {
    !function(z,b,r,f,i,s,h){z.GoogleAnalyticsObject=i,z[i]=z[i]||function(){(z[i].q=z[i].q||[]).push(arguments)},z[i].l=+new Date,s=b.createElement(r),h=b.getElementsByTagName(r)[0],s.src=f,h.parentNode.insertBefore(s,h)}(this,document,"script","//www.google-analytics.com/analytics.js","ga");

    ga('create', '@GOOGLE_ANALYTICS_ID@', 'auto');
    ga('send', 'pageview');
}
else{
    window.ga = window.ga || function () {};
}
<!-- End GOOGLE Analytics -->

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





function addScript(url, onloadFunction) {
  eltScript = document.createElement("script");
  eltScript.setAttribute("type", "text/javascript");
  eltScript.setAttribute("src", url);
  eltScript.onload = onloadFunction;
  document.getElementsByTagName('head')[0].appendChild(eltScript);
}









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
  document.getElementById("hdr-zfinlinks").style.display = "none";
  document.getElementById("hdr-zirclinks").style.display = "none";
  document.getElementById("hdr-generallinks").style.display = "none";
  document.getElementById("hdr-motto").style.display = "block";
}


/* end tab functions */


document.write(" <body id=\"body\" onload=\"hdrSetTabs();\">");
document.write("   <a href=\"/\">");
document.write("    <img id=\"logo-img\" src=\"/images/zfinlogo.png\"> ");
document.write("    <img id=\"logo-text\" src=\"/images/zfintxt.png\">");
document.write("   </a>");
document.write("   <noscript>");
document.write("    <div id=\"noscriptWarningBox\">");
document.write("      Many ZFIN features require that javascript be enabled.");
document.write("    </div>");
document.write("   </noscript>");
document.write("   <div id=\"quicksearchBox\">");
document.write("<form method=\"GET\" action=\"/search\" name=\"faceted-search\" accept-charset=\"utf-8\">");
document.write("    <label for=\"search-query-input\">");
document.write("    Search");
document.write("    </label>");
document.write("<img src=\"/images/new1.gif\" />");
document.write("    <input class=\"search-form-input input\" style=\"width: 300px;\" name=\"q\" id=\"header-search-query-input\" autocomplete=\"off\" type=\"text\"/>");
document.write("</form>");
document.write("   </div>");
document.write("   <div id=\"feedBox\">");
document.write("   <span id=\"site-link\">");
document.write("   <a href=\"/downloads\">Downloads</a> &nbsp; <a id=\"hdr-login-link\" href=\"/action/login\">Login</a> <a id=\"hdr-logout-link\" style=\"display:none\" href=\"/action/logout\">Logout</a>   ");


/*

if(getCookie('zfin_login').lastIndexOf('GUEST') == 0)
    document.write("  <a href=\"/action/login\">Login</a>");
else
    document.write("   <a href=\"/action/logout\">Logout</a>");
*/


document.write("                       </span> &nbsp;");
document.write("      <a href=\"https://@WIKI_HOST@/createrssfeed.action?types=blogpost&spaces=news&title=Zebrafish+News&labelString%3D&excludedSpaceKeys%3D&sort=modified&maxResults=10&timeSpan=120&showContent=true\"><img id=\"social-icon\" src=\"/images/feed-icon-28x28.png\"></a>");
document.write("      <a href=\"https://twitter.com/ZFINmod\" id=\"social-icon\" class=\"twitter-follow-button\" data-show-screen-name=\"false\" data-show-count=\"false\" data-lang=\"en\">Follow</a> ");
document.write("      <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=\"//platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script>");
document.write("   </div> ");

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
document.write("     <div id=\"hdr-zirclinks\" style=\"display: none;\" class=\"hdr-linkbar\"> ");
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
document.write("       <a href=\"/zirc/health/index.php\">Health Services</a>");
document.write("     </div>");
document.write("     <div id=\"hdr-zfinlinks\" class=\"hdr-linkbar\"> ");
document.write("       <a href=\"/\">Home</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/@WEBDRIVER_PATH_FROM_ROOT@?MIval=aa-newmrkrselect.apg\" title=\"Search by name, accession number, chromosome, vector or sequence type\">Genes / Markers / Clones</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("        <a href=\"/action/blast/blast\" title=\"Search for sequence alignment against ZFIN datasets and zebrafish datasets\">BLAST</a> ");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("        <a href=\"/@GBROWSE_PATH_FROM_ROOT@\">GBrowse</a> ");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/@WEBDRIVER_PATH_FROM_ROOT@?MIval=aa-xpatselect.apg\" title=\"Search by gene, developmental stage, anatomy and other attributes\">Expression</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/action/antibody/search\" title=\"Search for antibodies by gene, labeled anatomy and other attributes\">Antibodies</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");

document.write("       <a href=\"/action/fish/search\">Mutants / Knockdowns / Tg</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/action/construct/search\">Constructs</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("      <a href=\"/action/ontology/search\" title=\"Search anatomy and gene ontology\">Anatomy / GO</a>");
document.write("      <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/@WEBDRIVER_PATH_FROM_ROOT@?MIval=aa-pubselect2.apg&select_from=PUBLICATION\" title=\"Search for zebrafish research publications by author, title or citation\">Publications</a>");
document.write("       <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("       <a href=\"/@CGI_BIN_DIR_NAME@/mapper_select.cgi\" title=\"View genetic, radiation hybrid or consolidated maps\">Maps</a>");
document.write("     </div>");
document.write("     <div id=\"hdr-generallinks\" style=\"display: none;\" class=\"hdr-linkbar\">");
document.write("     <a href=\"/\"></a> ");


document.write("     <a href=\"/\">Home</a> "); 
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write(" <a href=\"https://@WIKI_HOST@/\" title=\"ZFIN-hosted community wiki: browse, contribute and export\">Wiki</A>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write(" <a href=\"https://@WIKI_HOST@/display/jobs\" title=\"Zebrafish-related job announcements\">Jobs</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write(" <a href=\"https://@WIKI_HOST@/display/meetings\" title=\"Zebrafish-related meeting announcements\">Meetings</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
 document.write("    <a href=\"/zf_info/news/newsgroup.html\" title=\"Moderated, online discussion group for anyone interested in zebrafish research\">Newsgroup</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/action/profile/person/search\" title=\"Search for zebrafish researchers by name or address\">People</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/action/profile/lab/search\" title=\"Search for laboratories by name, address or research interests\">Labs</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/action/profile/company/search\" title=\"Search for companies supplying zebrafish reagents\">Companies</a>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/news/education.html\" title=\"Educational websites for students and educators\">Education</A>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/zfbook/zfbk.html\" title=\"Browse The Zebrafish Book\"><em>ZF Book</em></A>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/contact_us.html\" title=\"ZFIN contact information\">Contact</A>");
document.write("     <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\" alt=\"-\"> ");
document.write("     <a href=\"/zf_info/dbase/db.html\" title=\"About ZFIN and citing ZFIN resources in publications\">About</A>");
document.write("    </div>");
document.write("    <div id=\"hdr-motto\" style=\"display:none;\"  class=\"hdr-linkbar\">   ");  
document.write("      The Zebrafish Model Organism Database");
document.write("    </div>");
document.write("  </div> <!-- navlinks -->");
document.write("</div>  ");
document.write("<div class=\"allcontent\">  ");

