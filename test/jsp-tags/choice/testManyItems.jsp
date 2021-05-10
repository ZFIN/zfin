<%@ page import="org.zfin.anatomy.AnatomyStatistics" %>
<%@ page import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core" %>
<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%
    AnatomyStatistics stat = new AnatomyStatistics();
    stat.setNumberOfObjects(102);
    AnatomySearchBean bean = new AnatomySearchBean();
    bean.setAnatomyStatistics(stat);

    pageContext.setAttribute("bean", bean, PageContext.REQUEST_SCOPE);

%>

<tagunit:assertEquals name="Many Items: 102">
    <tagunit:expectedResult>
        Genes
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:choice integerEntity="${bean.anatomyStatistics.numberOfObjects}" choicePattern="0#Genes| 1#Gene| 2#Genes"
                     scope="Request"/>
    </tagunit:actualResult>
</tagunit:assertEquals>


