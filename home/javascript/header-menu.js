


if (navigator.appName == 'Microsoft Internet Explorer') {
    document.write("<style type='text/css'>");

    /** if (IE version > 7) **/
    if (!window.XMLHttpRequest) {
        document.write("#hdr-tabs { margin-left: 95px; } ");
    }
    document.write("</style>");
}

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