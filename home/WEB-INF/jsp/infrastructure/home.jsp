<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<style>
    #quicksearchBox { display: none; }
</style>

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
                        <div class="btn-group">
                            <button type="submit" class="btn btn-primary btn-zfin btn-search">Go</button>
                        </div>
                        <a href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
                            <i class="fas fa-question-circle fa-lg"></i>
                        </a>

                    </form>
                    <script>
                        jQuery(document).ready(function() {
                            jQuery('#search-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY', {directLink: true});
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

