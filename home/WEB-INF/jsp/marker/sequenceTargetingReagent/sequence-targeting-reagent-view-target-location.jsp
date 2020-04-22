<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<c:if test="${!empty formBean.gbrowseImages}">

    <c:forEach items="${formBean.gbrowseImages}" var="image" end="1" varStatus="loop">
        <div class="__react-root"
             id="GbrowseImage__${loop.index}"
             data-image-url="${image.imageUrl}"
             data-link-url="${image.linkUrl}"
             data-build="${image.build}">
        </div>
    </c:forEach>

    <c:if test="${fn:length(formBean.gbrowseImages) > 2}">
        <div>
            <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View
                all ${fn:length(formBean.gbrowseImages)} target locations</a>
        </div>
    </c:if>

</c:if>
