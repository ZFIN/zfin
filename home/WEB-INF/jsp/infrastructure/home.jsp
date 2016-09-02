<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">
<link rel="stylesheet" type="text/css" href="/css/zdbhome.css">


<script>
    hdrSetCookie("tabCookie","Motto","","/");
</script>

<div id="zdbhome-container">

    <table id="zdbhome-container-inner">
        <tr>
            <td colspan="2" id="zdbhome-search">
                <div class="center">
                    <form method="GET" action="/search" name="search" accept-charset="utf-8" id="query-form">
                        <label for="search-query-input">Search ZFIN</label>
                        <input class="search-form-input input form-control"
                               placeholder="bmp2a, hindbrain development disrupted, pax morpholino"
                               name="q" id="search-query-input" autocomplete="off" type="text"/>
                        <button type="submit" class="btn btn-primary btn-zfin btn-search">Go</button>
                        <a href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
                            <i class="fa fa-question-circle fa-lg"></i>
                        </a>
                    </form>
                    <script>
                        jQuery(document).ready(function() {
                            jQuery('#search-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY');
                        });
                        $('#search-query-input').bind("typeahead:select", function() {
                            $('#query-form').submit();
                        });
                    </script>
                </div>
            </td>
        </tr>
        <tr>
            <td id="zdbhome-primary" nowrap>
                <zfin2:homePrimarySection/>
            </td>
            <td id="zdbhome-sidebar">
                <zfin2:homeSecondarySection/>
            </td>
        </tr>
    </table>
</div>




<%--


<div class="zdbhome-container">
    <div class="container-fluid zdbhome-container-inner">
        <div class="row">
            <div class="col-sm-12 center">
                <zfin2:homeSearchSection/>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-6">
                <zfin2:homePrimarySection/>
            </div>
            <div class="col-xs-12 col-sm-12 col-md-6">
                <zfin2:homeSecondarySection/>
            </div>
        </div>


    </div>
</div>--%>
