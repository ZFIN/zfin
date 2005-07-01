<%@page language="java"%>

<%-- retrieve matched record --%>

<jsp:useBean id="matchedRecord" type="org.zfin.mergerservlet.MatchedRecord"
             scope="request" />

<div style="padding-left: 2em; border-width: 4px; border-color: red; border-style: groove">

<%= matchedRecord.show() %>

<br>matched.jsp<br>


</div>
