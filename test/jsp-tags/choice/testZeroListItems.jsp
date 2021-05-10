<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ page import="org.zfin.gwt.root.dto.TermDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core"  %>
<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>

<%
    List<TermDTO> items = new ArrayList<TermDTO>();
    AnatomySearchBean bean = new AnatomySearchBean();
    bean.setTerms(items);

    AnatomySearchBean beanNull = new AnatomySearchBean();
    beanNull.setTerms(null);

    pageContext.setAttribute("bean", bean, PageContext.REQUEST_SCOPE);
    pageContext.setAttribute("beanNull", beanNull, PageContext.PAGE_SCOPE);

%>

<tagunit:assertEquals name="No Item provided, empty list">
<tagunit:expectedResult>
Genes
</tagunit:expectedResult>
<tagunit:actualResult>
<zfin:choice collectionEntity="${bean.terms}" choicePattern="0#Genes| 1#Gene| 2#Genes" scope="Request" />
</tagunit:actualResult>
</tagunit:assertEquals>


<tagunit:assertEquals name="No item provided, null">
<tagunit:expectedResult>
Genes
</tagunit:expectedResult>
</tagunit:assertEquals>


