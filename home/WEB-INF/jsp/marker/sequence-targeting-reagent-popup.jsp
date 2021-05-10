<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<zfin2:sequenceTargetingReagentInfo marker="${sequenceTargetingReagent}"
                                    markerBean="${formBean}"
                                    previousNames="${formBean.previousNames}"
                                    suppressAnalysisTools="true"/>


