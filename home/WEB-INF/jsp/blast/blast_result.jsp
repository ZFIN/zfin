<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<h3>BLAST Results</h3>

<zfin2:blastResultTicketInfo formBean="${formBean}" />

<c:choose>
    <c:when test="${empty formBean.blastResultBean.hits}">
        <span style="color: gray; font-size: larger;">NO HITS</span>
    </c:when>
    <c:otherwise>

        <br>

        <zfin2:blastResultHeader blastResults="${formBean.blastResultBean}"/>

        <hr>

        <zfin2:blastResultAlignment blastResults="${formBean.blastResultBean}" width="500" numTicksPlusOne="4" accessionWidth="170"/>

        <zfin2:blastResultOverview blastResults="${formBean.blastResultBean}"/>

        <hr>

        <zfin2:blastResultData blastResults="${formBean.blastResultBean}" />

        <hr>

    </c:otherwise>
</c:choose>
<br>
<zfin2:blastResultDetails blastResult="${formBean.blastResultBean}"/>

