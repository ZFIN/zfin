

try {
  jQuery(document).ready(function() { jQuery(".default-input").focus(); })
}
catch(e) {
    if (e instanceof ReferenceError) {
	// just ignore it...
    } else {
	throw e; // let others bubble up
    }
}

document.write(" </div> ");
document.write(" <div id=\"footer\">  ");
document.write("   <div id=\"footerlinks\"> ");
document.write("     <a href=\"/\">Home</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/ZFIN/misc_html/tips.html\" title=\"Frequently asked questions\">Help and Tips</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/glossary.html\" title=\"Terms useful in understanding zebrafish development, anatomy, genetics and bioinformatics\">Glossary</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/news/committees.html\" title=\"Committees and working groups\">Committees</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"https://wiki.zfin.org/display/general/ZFIN+db+information\" title=\"Citing ZFIN resources in publications\">Citing ZFIN</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/contact_us.html\" title=\"ZFIN contact information\">Contact</a> <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\"> ");
document.write("     <a href=\"https://wiki.zfin.org/display/general/ZFIN+db+information\" title=\"About ZFIN\">About</a> ");
document.write("     <a href=\"https://wiki.zfin.org/display/jobs/ZFIN+Jobs\" title=\"Jobs at ZFIN\">ZFIN Jobs</a> ");
document.write("   </div> ");
document.write("   <div id=\"footercredits\" style=\"text-align: center \"> ");
document.write("       <span id=\"footer-generous-support\">Development of the Zebrafish Database is generously supported by the National Human Genome Research Institute (HG004838, and HG004834) of the National Institutes of Health. <br></span> ");
document.write("       <a href=\"/warranty.html\">Disclaimer, limitations, copyright &copy;  </a> <a href=\"http://www.uoregon.edu\"> ");
document.write("       University of Oregon</a>, 1994-2015,<a href=\"http://www.ci.eugene.or.us\"> Eugene</a>, Oregon. <br> ");
document.write("       <small>ZFIN logo design by Kari Pape, <A HREF=\"http://www.uoregon.edu\">University of Oregon</a></small> ");
document.write("   </div> ");
document.write(" </div>  ");


var jQueryScriptOutputted = false;
function initJQuery() {




    //if the jQuery object isn't available
    if (typeof(jQuery) == 'undefined') {

        console.log("jQuery wasn't loaded, loading it now");

        if (! jQueryScriptOutputted) {
            //only output the script once..
            jQueryScriptOutputted = true;

            //output the script (load it from google api)
            document.write("<script type=\"text/javascript\" src=\"/javascript/jquery-1.11.1.min.js\"></script>");
            document.write("<script type=\"text/javascript\" src=\"/javascript/typeahead.bundle.js\"></script>");
            document.write("<script type=\"text/javascript\" src=\"/javascript/autocompletify.js\"></script>");
            document.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/typeahead.css\">");
        }
        setTimeout("initJQuery()", 50);
    } else {

        jQuery(function() {
            $('#header-search-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY');
            $('#header-search-query-input').bind("typeahead:selected", function() {
                $('#header-query-form').submit();
            });
        });
    }


}

initJQuery();


if(isLoggedIn()) {
    document.getElementById('hdr-login-link').style.display = 'none';
    document.getElementById('hdr-logout-link').style.display = 'inline';
}



