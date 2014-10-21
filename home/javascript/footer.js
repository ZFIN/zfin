

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
document.write("     <a href=\"/ZFIN/misc_html/tips.html\" title=\"Frequently asked questions\">Help and Tips</A><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/glossary.html\" title=\"Terms useful in understanding zebrafish development, anatomy, genetics and bioinformatics\">Glossary</A><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/news/committees.html\" title=\"Committees and working groups\">Committees</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/dbase/db.html#citing\" title=\"Citing ZFIN resources in publications\">Citing ZFIN</a><img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\">  ");
document.write("     <a href=\"/zf_info/contact_us.html\" title=\"ZFIN contact information\">Contact</A> <img src=\"/images/research-dot.png\" class=\"hdr-linkbar-divider\"> ");
document.write("     <a href=\"/zf_info/dbase/db.html\" title=\"About ZFIN\">About</A> ");
document.write("     <a href=\"/ZFIN/misc_html/jobs.html\" title=\"Jobs at ZFIN\">Jobs</A> ");
document.write("   </div> ");
document.write("   <div id=\"footercredits\" style=\"text-align: center \"> ");
document.write("       <span id=\"footer-generous-support\">Development of the Zebrafish Database is generously supported by the National Human Genome Research Institute (HG004838, and HG004834) of the National Institutes of Health. <br></span> ");
document.write("       <a href=\"/warranty.html\">Disclaimer, limitations, copyright &copy;  </a> <a href=\"http://www.uoregon.edu\"> ");
document.write("       University of Oregon</a>, 1994-2014,<a href=\"http://www.ci.eugene.or.us\"> Eugene</a>, Oregon. <br> ");
document.write("       <small>ZFIN logo design by Kari Pape, <A HREF=\"http://www.uoregon.edu\">University of Oregon</a></small> ");
document.write("   </div> ");
document.write(" </div>  ");


<!-- start AWStats -->
document.write('<script src="/javascript/awstats_misc_tracker.js" type="text/javascript">') ;
document.write("</script>");
document.write('<script type="text/javascript/">');
document.write('<noscript><img src="/javascript/awstats_misc_tracker.js?nojs=y" height=0 width=0 border=0 style="display: none"></noscript>');
document.write("</script>");
<!-- End AWStats -->


var jQueryScriptOutputted = false;
function initJQuery() {




    //if the jQuery object isn't available
    if (typeof(jQuery) == 'undefined') {

        console.log("jQuery wasn't loaded, loading it now");

        if (! jQueryScriptOutputted) {
            //only output the script once..
            jQueryScriptOutputted = true;

            //output the script (load it from google api)
            document.write("<script type=\"text/javascript\" src=\"/javascript/jquery-1.8.3.min.js\"></script>");
            document.write("<script type=\"text/javascript\" src=\"/javascript/jquery-ui-1.10.4.custom.js\"></script>");
            document.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery-ui-1.10.4.custom.css\">");
        }
        setTimeout("initJQuery()", 50);
    } else {

        jQuery(function() {

            var autocompleteXhr = null;

            jQuery('#header-search-query-input').autocomplete({
                source: function(request, response) {
                    http://stackoverflow.com/questions/4551175/how-to-cancel-abort-jquery-ajax-request

                        if(autocompleteXhr && autocompleteXhr.readyState != 4) {
                            autocompleteXhr.abort();
                        }
                    autocompleteXhr = jQuery.ajax({
                        url: '/action/quicksearch/autocomplete',
                        dataType: "json",
                        data: {
                            q : request.term
                        },
                        success: function(data) {
                            response(data);
                        }
                    });
                },
                select: function(event, ui) {
                    var val = ui.item.value;
                    jQuery('#header-search-query-input').val(val);
                    var form = jQuery(this).parents('form:first');
                    jQuery(form).submit();

                },
                minLength: 1, delay: 50,
                open: function(event, ui){
                    jQuery("ul.ui-autocomplete li a").each(function(){
                        var htmlString = jQuery(this).html().replace(/&lt;/g, '<');
                        htmlString = htmlString.replace(/&gt;/g, '>');
                        jQuery(this).html(htmlString);
                    });
                }
            }, {});



        });
    }


}

initJQuery();


if(isLoggedIn()) {
    document.getElementById('hdr-login-link').style.display = 'none';
    document.getElementById('hdr-logout-link').style.display = 'inline';
}



