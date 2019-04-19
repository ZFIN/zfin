
function hdrSetTabs() {

    tabCookie = hdrGetCookie("tabCookie");

    if (tabCookie === "Motto") {
        showMotto();
    }
    if (!tabCookie || tabCookie === "Research") {
        showZFINLinks();
    }
    if (tabCookie === "Products") {
        showZIRCLinks();
    }
    if (tabCookie === "General") {
        showGeneralLinks();
    }
}

function hdrSetCookie(name,value,expires,path,domain,secure) {
    document.cookie = name + "=" +escape(value) +
            ( (expires) ? ";expires=" + expires.toGMTString() : "") +
            ( (path) ? ";path=" + path : "") +
            ( (domain) ? ";domain=" + domain : "") +
            ( (secure) ? ";secure" : "");
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

function deselectTabs() {
    $('.header-tab').removeClass('selected');
}

function showZFINLinks() {
    hdrSetCookie("tabCookie","Research","","/");

    deselectTabs();
    $('.header-tab.research').addClass('selected');

    $("#hdr-zirclinks").hide();
    $("#hdr-generallinks").hide();
    $("#hdr-motto").hide();
    $("#hdr-zfinlinks").show();
}

function showGeneralLinks() {
    hdrSetCookie("tabCookie","General","","/");

    deselectTabs();
    $('.header-tab.general').addClass('selected');

    $("#hdr-zfinlinks").hide();
    $("#hdr-zirclinks").hide();
    $("#hdr-motto").hide();
    $("#hdr-generallinks").show();
}

function showZIRCLinks() {
    hdrSetCookie("tabCookie","Products","","/");

    deselectTabs();
    $('.header-tab.zirc').addClass('selected');

    $("#hdr-zfinlinks").hide();
    $("#hdr-generallinks").hide();
    $("#hdr-motto").hide();
    $("#hdr-zirclinks").show();
}

function showMotto() {
    $("#hdr-zfinlinks").hide();
    $("#hdr-zirclinks").hide();
    $("#hdr-generallinks").hide();
    $("#hdr-motto").show();
}

$(function() {
    $(".header-tab.research").click(showZFINLinks);
    $(".header-tab.general").click(showGeneralLinks);
    $(".header-tab.zirc").click(showZIRCLinks);
    hdrSetTabs();

    var login = $('#hdr-login-link');
    var logout = $('#hdr-logout-link');
    jQuery.ajax({
        url: "/action/login-status",
        success: function (data) {
            if (data) {
                if (data.root) {
                    $('#hdr-gmc-search').attr('href', '/action/marker/search');
                }
                login.hide();
                logout.show();
            } else {
                login.show();
                logout.hide();
            }
        },
        error: function () {
            console.log("could not validate login status");
        }
    });
});