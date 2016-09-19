<%@ tag import="org.zfin.publication.Publication" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="image" type="org.zfin.expression.Image" rtexprvalue="true" required="true"  %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>



<c:set var="UNPUBLISHED" value="<%=Publication.Type.UNPUBLISHED %>"/>


<c:set var="autoplay" value=""/>
<c:if test="${autoplayVideo}">
    <c:set var="autoplay" value="autoplay"/>
</c:if>


<div style="text-align:center; max-width:100%">
    <TABLE border=0 cellpadding=20>
        <TR>
            <TD align=center bgcolor=#000000>
               



      <c:if test="${image.figure.publication.canShowImages && empty image}">
          <img class="figure-image placeholder" src="/images/imagenotavailable.gif"/>
      </c:if>




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

                            <c:choose>
                                <c:when test="${!empty image.imageWithAnnotationsFilename}">
                                    <c:set var="filename" value="${image.imageWithAnnotationsFilename}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="filename" value="${image.imageFilename}"/>
                                </c:otherwise>
                            </c:choose>
                    <%--//        <img align="middle" class="figure-gallery-modal-image"  src="/imageLoadUp/${filename}"/>--%>
                        <%--<img class="figure-gallery-modal-image" src="/imageLoadUp/${filename}"/>--%>
<img style="max-width:100%; max-height: 100%" src="/imageLoadUp/${filename}"/>



                    </c:otherwise>
                </c:choose>

            </TD>
        </TR>
    </TABLE>
</div>

     <%--has permission to show, multiple images, should show them as thumbnails --%>
    <%--<c:if test="${image.figure.publication.canShowImages}">

            <zfin:link entity="${image}"/>

    </c:if>--%>


  <p class="fig">

      <strong>
          <c:choose>
              <c:when test="${image.figure.type == 'TOD'}">

              </c:when>
              <c:otherwise>
                ${image.figure.label}
              </c:otherwise>
          </c:choose>
      </strong>
       <c:if test="${!image.figure.publication.canShowImages}">
           <img class="placeholder" src="/images/onlyfrompublisher.jpg">
       </c:if>
       <c:choose>
           <c:when test="${!image.figure.publication.canShowImages || (empty image.figure.caption && image.figure.publication.type != UNPUBLISHED)}">

               <c:choose>
                   <c:when test="${figure.comments == 'GELI'}">
                       This is a summary of gene expression assays reported in this publication.
                       Associated figures and anatomical structures have not yet been added to ZFIN.
                   </c:when>
                   <c:otherwise>
                       ZFIN is incorporating published figure images and captions as part of an ongoing project.
                       Figures from some publications have not yet been curated, or are not available for display because of copyright restrictions.
                   </c:otherwise>
               </c:choose>

           </c:when>
           <c:otherwise>${image.figure.caption}</c:otherwise>
       </c:choose>

    </p>


    <%-- on all figure view, we want to also show some data tables, so they'll be passed in as the
         'body' of this tag --%>
    <div style="padding-top: 1em; clear: both">
    <jsp:doBody/>
    </div>
  </td></tr>
