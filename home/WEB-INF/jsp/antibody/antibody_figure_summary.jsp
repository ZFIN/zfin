<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodyBean" scope="request"/>

<tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:putAttribute name="subjectName" value="${formBean.antibody.name}"/>
    <tiles:putAttribute name="subjectID" value="${formBean.antibody.zdbID}"/>
</tiles:insertTemplate>

<div style="font-size:larger; font-weight:bold;" align="center">
    Antibody <zfin:link entity="${formBean.antibody}"/> labeling in
    <zfin2:postComposedTermLink superterm="${formBean.superTerm}" subterm="${formBean.subTerm}" />
    <c:if test="${formBean.startStage.zdbID !=null && formBean.endStage.zdbID != null}">
        <c:choose>
            <c:when test="${formBean.startStage.zdbID == formBean.endStage.zdbID}">
                at stage <zfin:link entity="${formBean.startStage}"/>
            </c:when>
            <c:otherwise>
                at stage range <zfin:link entity="${formBean.startStage}"/> to <zfin:link
                    entity="${formBean.endStage}"/>
            </c:otherwise>
        </c:choose>
    </c:if>
    <br/>
    <small>( ${formBean.antibodyStat.numberOfFiguresDisplay} from ${formBean.antibodyStat.numberOfPublicationsDisplay}
        )
    </small>

    <br/>
    <small>
        <c:choose>
            <c:when test="${formBean.onlyFiguresWithImg}">
                [ <a
                    href="javascript:document.location.replace('figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&superTerm.zdbID=${formBean.superTerm.zdbID}&subTerm.zdbID=${formBean.subTerm.zdbID}<c:if test="${formBean.startStage.zdbID != null}">&startStage.zdbID=${formBean.startStage.zdbID}&endStage.zdbID=${formBean.endStage.zdbID}</c:if>&onlyFiguresWithImg=false')">
                Show all figures</a> ]
            </c:when>
            <c:otherwise>
                [ <a
                    href="javascript:document.location.replace('figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&superTerm.zdbID=${formBean.superTerm.zdbID}&subTerm.zdbID=${formBean.subTerm.zdbID}<c:if test="${formBean.startStage.zdbID != null}">&startStage.zdbID=${formBean.startStage.zdbID}&endStage.zdbID=${formBean.endStage.zdbID}</c:if>&onlyFiguresWithImg=true')">
                Show only figures with images</a> ]
            </c:otherwise>
        </c:choose>
    </small>


</div>

<p/>

<table width=100% border=0 cellspacing=0>

    <tr>
        <th align="left" width="20%">Publication</th>
        <th align="left" width="5%">Data</th>
        <th align="left" width="5%"> &nbsp; </th>
        <th align="left">Anatomy</th>
    </tr>

    <% int alternateShadingIndex = 0; %>
    <c:forEach var="figureData" items="${formBean.antibodyStat.figureSummary}" varStatus="status">
        <c:if test="${status.index == 0 || formBean.antibodyStat.figureSummary[status.index].publication.zdbID ne formBean.antibodyStat.figureSummary[status.index-1].publication.zdbID}">
            <% alternateShadingIndex++; %>
        </c:if>
        <% if (alternateShadingIndex % 2 == 1) { %>
        <tr class="odd">
                    <% } else { %>
        <tr>
            <% } %>
            <td>
                <c:if test="${status.index == 0 || formBean.antibodyStat.figureSummary[status.index].publication.zdbID ne formBean.antibodyStat.figureSummary[status.index-1].publication.zdbID}">
                    <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${figureData.publication.zdbID}"
                       id="${figureData.figure.zdbID}">
                            ${figureData.publication.shortAuthorList}
                    </a>
                </c:if>
            </td>
            <td>
                <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${figureData.figure.zdbID}">${figureData.figure.label}</a>
            </td>
            <td>
                <c:if test="${figureData.thumbnail != null}">
                    <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${figureData.figure.zdbID}">
                        <img border="1" src="/imageLoadUp/${figureData.thumbnail}" height="50"
                             title='${figureData.imgCount} image<c:if test="${figureData.imgCount > 1}">s</c:if>'
                                />
                        <c:if test="${figureData.imgCount > 1}"><img border="0"
                                                                     src="/images/multibars.gif"/></c:if>
                    </a>
                </c:if>
            </td>
            <td>
                <zfin2:toggledHyperlinkList collection="${figureData.terms}" maxNumber="6"
                                            id="${figureData.figure.zdbID}"/>
            </td>
        </tr>
    </c:forEach>
</table>