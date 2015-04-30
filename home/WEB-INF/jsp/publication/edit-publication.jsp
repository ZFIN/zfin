<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<style>
    .ui-menu-item {
        font-size: small
    }

    /* necessary because bootstrap overrides our body margin on top */
    body {
        margin-top: 81px;
    }

    /* don't need the left-right magin fixes from all-content */
    div.allcontent {
        margin: 0px;
    }

    #hdr-navlinks {
        line-height: 15px;
        /*        position: relative;
                top: 5px;*/
    }

    .tabContent, .selectedTabContent { height: 22px !important; }

    #feedBox #rss-icon {
        position: absolute;
        top: 1px;
        right: 64px;
    }

    html { font-size: 100%; }
    body { font-size: 16px; }
</style>

<div class="container-fluid">
    <div class="row">
        <div class="col-sm-10">
            <h2>Editing ${publicationForm.zdbID}</h2>
        </div>

        <div class="col-sm-2">
            <button class="btn btn-danger"><i class="fa fa-trash"></i> Delete</button>
        </div>
    </div>

    <zfin2:publicationForm publicationForm="${publicationForm}"/>
</div>
