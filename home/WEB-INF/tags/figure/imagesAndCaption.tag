  <%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true"  %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>


<c:set var="autoplay" value=""/>
<c:if test="${autoplayVideo}">
    <c:set var="autoplay" value="autoplay"/>
</c:if>



<table class="figure-image-and-caption">
  <tr><td>

      <c:if test="${figure.publication.canShowImages && empty figure.images}">
          <img class="figure-image placeholder" src="/images/imagenotavailable.gif"/>
      </c:if>


      <%--has permission to show, single image, can be video--%>
    <c:if test="${figure.publication.canShowImages && !empty figure.images && fn:length(figure.images) == 1}">
        <c:forEach var="image" items="${figure.images}">
            <zfin:link entity="${image}">
                <img class="figure-image medium" src="/imageLoadUp/medium/${image.imageFilename}"/>
            </zfin:link>
        </c:forEach>
    </c:if>

     <%--has permission to show, multiple images--%>
    <c:if test="${figure.publication.canShowImages && !empty figure.images && fn:length(figure.images) > 1}">
        <c:forEach var="image" items="${figure.images}">
            <zfin:link entity="${image}"/>
        </c:forEach>
    </c:if>



    <p class="fig">
      <%-- show Description: for the multi-image pubs, the figure label is shown for all others,
           logic comes from fxfigureview.apg --%>
      <strong>
          <c:choose>
              <c:when test="${fn:length(figure.images) > 1}">
                Description:
              </c:when>
              <c:when test="${figure.type == 'TOD'}">
                <%-- don't show anything as a label for Text Only --%>
              </c:when>
              <c:otherwise>
                ${figure.label}
              </c:otherwise>
          </c:choose>
      </strong>
       <c:if test="${!figure.publication.canShowImages}">
           <img class="placeholder" src="/images/onlyfrompublisher.jpg">
       </c:if>
       <c:choose>
           <c:when test="${!figure.publication.canShowImages || empty figure.caption}">

               ZFIN is incorporating published figure images and captions as part of an ongoing project.
               Figures from some publications have not yet been curated, or are not available for display because of copyright restrictions.
           </c:when>
           <c:otherwise>${figure.caption}</c:otherwise>
       </c:choose>

    </p>



    <%-- on all figure view, we want to also show some data tables, so they'll be passed in as the
         'body' of this tag --%>
    <div style="padding-top: 1em; clear: both">
    <jsp:doBody/>
    </div>
  </td></tr>
</table>