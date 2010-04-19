<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ tag pageEncoding="UTF-8" %>

<%@ attribute name="gbrowseImage" type="org.zfin.gbrowse.presentation.GBrowseImage" required="true"%>
<%@ attribute name="width" required="false"%>


 <c:if test="${empty width}">
     <c:set var="width" value="${gbrowseImage.defaultWidth}"/>
 </c:if>


   <c:if test="${empty gbrowseImage.linkText}">
    <div style="margin: .5em; border: 1px solid black ; background: white">

        <a href="${gbrowseImage.linkURL}">
        <img
             style="padding-bottom:10px; border: 0 "
             src="${gbrowseImage.imageURL}&width=${width}">
        </a>
    </div>
   </c:if>
   <c:if test="${!empty gbrowseImage.linkText}">
       <div style="width: ${width}px; margin: .5em auto ; padding: .5em; border: 1px solid black ; background: white">
         <a href="${gbrowseImage.linkURL}">${gbrowseImage.linkText}</a>
         <c:if test="${!empty gbrowseImage.note}"><div style="font-size:.7em; color: #888; padding-top: .7em">${gbrowseImage.note}</div></c:if>
         
       </div>
   </c:if>
