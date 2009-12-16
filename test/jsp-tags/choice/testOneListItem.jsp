<%@ page import="org.zfin.anatomy.AnatomyItem" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core"  %>
<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%
    List<AnatomyItem> items = new ArrayList<AnatomyItem>();
    items.add(new AnatomyItem());
    AnatomySearchBean bean = new AnatomySearchBean();
    bean.setAnatomyItems(items);

    pageContext.setAttribute("bean", bean, PageContext.REQUEST_SCOPE);

%>

<tagunit:assertEquals name="One Item in List">
<tagunit:expectedResult>
Gene
</tagunit:expectedResult>
<tagunit:actualResult>
<zfin:choice collectionEntity="${bean.anatomyItems}" choicePattern="0#Genes| 1#Gene| 2#Genes" scope="Request" />
</tagunit:actualResult>
</tagunit:assertEquals>


