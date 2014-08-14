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
            <c:choose>
                <c:when test="${image.videoStill}">
                    <video controls ${autoplay} loop height="500" poster="/imageLoadUp/medium/${image.imageFilename}">
                        <c:forEach var="video" items="${image.videos}">
                            <source src="/videoLoadUp/${video.videoFilename}"/>
                        </c:forEach>
                        This browser does not support embedded videos.
                    </video>
                </c:when>
                <c:otherwise>
                    <zfin:link entity="${image}">
                        <c:choose>
                            <c:when test="${!empty image.imageWithAnnotationsFilename}">
                                <c:set var="filename" value="${image.imageWithAnnotationsFilename}"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="filename" value="${image.imageFilename}"/>
                            </c:otherwise>
                        </c:choose>
                        <img class="figure-image medium" src="/imageLoadUp/medium/${filename}"/>
                    </zfin:link>
                </c:otherwise>
            </c:choose>
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
              <c:when test="${ (fn:length(figure.images) > 1)}">
                <c:if test="{!empty figure.caption}">
                    Description:
                </c:if>
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
           <c:when test="${!figure.publication.canShowImages || (empty figure.caption && figure.publication.type != 'Unpublished')}">
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