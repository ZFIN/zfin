<%@ page import="org.zfin.anatomy.AnatomyItem" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core"  %>
<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>

<%
    List<AnatomyItem> items = new ArrayList<AnatomyItem>();
    AnatomySearchBean bean = new AnatomySearchBean();
    bean.setAnatomyItems(items);

    AnatomySearchBean beanNull = new AnatomySearchBean();
    beanNull.setAnatomyItems(null);

    pageContext.setAttribute("bean", bean, PageContext.REQUEST_SCOPE);
    pageContext.setAttribute("beanNull", beanNull, PageContext.PAGE_SCOPE);

%>

<tagunit:assertEquals name="No Item provided, empty list">
<tagunit:expectedResult>
Genes
</tagunit:expectedResult>
<tagunit:actualResult>
<zfin:choice collectionEntity="${bean.anatomyItems}" choicePattern="0#Genes| 1#Gene| 2#Genes" scope="Request" />
</tagunit:actualResult>
</tagunit:assertEquals>


<tagunit:assertEquals name="No item provided, null">
<tagunit:expectedResult>
Genes
</tagunit:expectedResult>
<tagunit:actualResult>
<zfin:choice name="beanNull" collection="anatomyItems" choicePattern="0#Genes| 1#Gene| 2#Genes" scope="Page" />
</tagunit:actualResult>
</tagunit:assertEquals>


