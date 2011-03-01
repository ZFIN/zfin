<%@ page import="org.zfin.anatomy.AnatomyItem" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core" %>
<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%

    AnatomyItem item = new AnatomyItem();
    item.setTermName("Brain");
    AnatomySearchBean bean = new AnatomySearchBean();
    bean.setAnatomyItem(item);
    pageContext.setAttribute("ao", bean, PageContext.PAGE_SCOPE);

    AnatomySearchBean ao = new AnatomySearchBean();
    ao.setHighlightText("Werner");
    pageContext.setAttribute("bean", ao, PageContext.REQUEST_SCOPE);

%>

<tagunit:assertEquals name="Create a Marker Link DB">
    <tagunit:expectedResult>
        Genes
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:hightlight name="ao" property="anatomyItem.name" hightlightName="bean"
                         hightlightProperty="highlightText"/>
    </tagunit:actualResult>
</tagunit:assertEquals>


