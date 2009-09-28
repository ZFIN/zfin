<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%--<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>--%>
<%--<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>--%>
<!--<link rel="stylesheet" type="text/css" href="/css/zfin.css">-->

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>${formBean.marker.abbreviation}</title>
</head>

<body>

<style>
    /*** gene name & abbrev ***/
    body {
        font-family: arial, sans-serif;
    }

    .genedom {
        font-style: italic;
    }
    a.external{
        background: transparent url(/images/external.png) no-repeat scroll right center ;
        padding-right: 13px;
    }
    a.close_link {
        color:#333333;
        font-family:sans-serif;
        font-weight:bold;
        text-decoration: none;
        font-size: Large;
    }

</style>


<c:if test="${showClose==true}">
    <div style="text-align: right;">
        <a title="Close window" class="close_link" onclick="window.parent.hideTerm(); " href="javascript:;">x</a>
    </div>
</c:if>
<br>

<zfin2:geneHead gene="${formBean.marker}"/>

</body>

</html>
