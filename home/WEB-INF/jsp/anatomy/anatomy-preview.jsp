<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<html>
<head>
    <link rel="stylesheet" type="text/css" href="/css/zfin.css">

    <%-- override some of zfin.css since this will come up in an iframe --%>

    <style type="text/css">

        body {
            background: white;
            margin: 1px;
        }

        a.external {
            background: transparent url(/images/external.png) no-repeat scroll right center;
            padding-right: 13px;
        }

        a.xpatanat-close_link {
            color: #333333;
            font-family: sans-serif;
            font-weight: bold;
            text-decoration: none;
            font-size: Large;
        }

        body {
            font-family: arial, sans-serif;
        }
    </style>
</head>
<body>
<p></p>

<div style="float: right">
    <input type="button" alt="Add To Search" value="Add To Search"
           onclick="window.parent.useTerm('${formBean.anatomyItem.nameEscaped}'); ">
    &nbsp;
    &nbsp;
    <a title="Close window" class="xpatanat-close_link" onclick="window.parent.hideTerm(); " href="javascript:">x</a>

</div>

<zfin2:anatomy-view-header-info formBean="${formBean}" showSearchBox="false" linkStageTerms="false"/>

<p/>

<div class="summary">
    <span class="summaryTitle">Relationships</span> (<a href="/zf_info/ontology_relationship_info.html">about</a>)
    <table class="summary horizontal-solidblock">
        <c:forEach var="rt" items="${formBean.relations}">
            <tr>
                <th>
                        <%-- keep the relationship types from wrapping --%>
                        ${fn:replace(rt.type," ","&nbsp;")}:
                </th>
                <td>
                    <c:forEach var="item" items="${rt.items}">
                        <%--<zfin:link entity="${item}"/>--%>
                        <a href="/action/anatomy/anatomy-preview/${item.zdbID}">${item.termName}</a>
                        &nbsp;
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </TABLE>
</div>

<p/>
<a href="/action/anatomy/anatomy-view/${formBean.anatomyItem.zdbID}" target="_blank">Show Anatomy Details</a>
</body>
</html>
