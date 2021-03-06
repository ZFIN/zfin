<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<html>
<head>
    <%-- override some of zfin.css since this will come up in an iframe --%>
    <style type="text/css">

        body {
            background: white;
            margin: 1px;
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

<p></p>

<div style="float: right">
    <input type="button" alt="Add To Search" value="Add To Search"
           onclick="window.parent.useTerm('${formBean.anatomyItem.nameEscaped}'); ">
    &nbsp;
    &nbsp;
    <a title="Close window" class="xpatanat-close_link" onclick="window.parent.hideTerm(); " href="javascript:">x</a>
</div>

<table width="100%" class="primary-entity-attributes">
    <tr>
        <th width="80" valign=top>
            <span class="name-label">Name:</span>
        <td>
            <span class="name-value">${formBean.anatomyItem.termName}</span>
            <br>
            <a href="/action/anatomy/term-detail?anatomyItem.zdbID=${formBean.anatomyItem.zdbID}" class="external"
               target="_blank">Anatomy Details</a>
            <c:if test="${formBean.anatomyItem.obsolete}"><span style="color:red">(obsolete)</span> </c:if>
        </td>
    </tr>
    <c:if test="${not empty formBean.anatomyItem.synonyms}">
        <tr valign="top">
            <th>
                Synonyms:
            </th>
            <td>
                    ${formBean.formattedSynonymList}
            </td>
        </tr>
    </c:if>

    <c:if test="${formBean.anatomyItem.formattedDefinition != null  }">
        <tr>
            <th>
                Definition:
            </th>
            <td>
                    ${formBean.anatomyItem.formattedDefinition}
            </td>
        </tr>
    </c:if>
    <tr>
        <th>Appears&nbsp;at:</th>
        <td><zfin:name entity="${formBean.anatomyItem.start}" longVersion="true"/></td>
    </tr>
    <tr>
        <th>Evident&nbsp;until:</th>
        <td><zfin:name entity="${formBean.anatomyItem.end}" longVersion="true"/></td>
    </tr>
</table>

<div class="summary">
    <span class="summaryTitle">
        Relationships
        <a class='popup-link info-popup-link' href='/action/ontology/note/ontology-relationship'></a>
    </span>
    <table class="summary horizontal-solidblock">
        <c:forEach var="rt" items="${formBean.relations}">
            <tr>
                <th>
                        ${rt.type}:
                </th>
                <td>
                    <c:forEach var="item" items="${rt.items}">
                        <%--<zfin:link entity="${item}"/>--%>
                        <a href="/action/anatomy/term-info?anatomyItem.zdbID=${item.zdbID}">${item.termName}</a>
                        &nbsp;
                    </c:forEach>
                </td>
            </tr>
        </c:forEach>
    </table>
</div>

</html>
