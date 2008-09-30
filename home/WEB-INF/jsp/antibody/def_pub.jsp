<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.antibody.presentation.AntibodyUpdateDetailBean" %>
<%--
    Def pub module.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<table style="background-color:#bbbbbb" align="right" width="15%">
    <tr>
        <td colspan="2">
            <hr/>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <b title="This page is publication centric. Enter a pub id and cklick 'Set' and the Pub ID will be used in all instances on this page where a publication is added. The publication
            entered here is called Global Reference, or short Def-Pub.">Default Publication (Def-Pub)</b>
            <hr>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <div id="pub-detail"/>
        </td>
    </tr>
    <tr>
        <td nowrap=nowrap valign="top">
            <b title="Global Reference used on this page" id="Def-Pub-field">Enter Pub ID:</b>
        </td>
        <td>
            <form:input path="<%= AntibodyUpdateDetailBean.AB_DEFPUB_ZDB_ID%>" size="25" id="Def-Pub"
                        onblur="updateSelectionBox(this.value);"
                        onkeydown="k = (navigator.appName == 'Netscape') ? event.which : window.event.keyCode;
		                        if (k == 13 ) { getPubDetail(this.value);}"/>
            <span id="def-pub-error-message">
                <form:errors path="<%= AntibodyUpdateDetailBean.AB_DEFPUB_ZDB_ID%>"
                             cssClass="error indented-error"/>
            </span>
        </td>
    </tr>
    <tr>
        <td valign="top" align="right"><b>or:</b></td>
        <td>
            <p/>
            <script type="text/javascript">
                function changeDefPubFromSelectionBox(val) {
                    if (val == '-' || val == '--')
                        val = '';
                    document.getElementById('Def-Pub').value = val;
                        getPubDetail(val);

                }
            </script>
            <form:select path="attribution"
                         onchange="changeDefPubFromSelectionBox(this.value)"
                         id="curatorPubs">
                <form:options items="${formBean.defPubList}" itemLabel="key" itemValue="value"/>
            </form:select>
        </td>
    </tr>
</table>

<script type="text/javascript">
    function getPubDetail(pubID) {
        //alert("Pub ID: " + pubID);
        var pars = "hello";
        if (pubID != null && pubID != '' && pubID != '-' && pubID != '--')
            new Ajax.Updater('pub-detail', "/action/def-pub?zdbID=" + pubID, {method:"get", parameters: pars});
        else {
            //alert("Pub ID: " + pubID);
            document.getElementById('pub-detail').innerHTML = '<span style="color: red;font-weight:bold;text-align:center;">Please provide a Publication</span> <br/><hr/>';
        }
    }

    function updateSelectionBox(pubID) {
        //alert("Pub ID: " + pubID);
        document.getElementById('curatorPubs').value = pubID;
        getPubDetail(pubID);
    }

    var defPub = "Def-Pub";

    // causes to call onload() function to be executed upon loading the page
    function addLoadEvent(_function) {
        var _onload = window.onload;
        if (typeof window.onload != 'function') {
            if (window.onload) {
                window.onload = _function;
            } else {
                var _addEventListener = window.addEventListener || document.addEventListener;
                var _attachEvent = window.attachEvent || document.attachEvent;
                if (_addEventListener) {
                    _addEventListener('load', _function, true);
                    return true;
                } else if (_attachEvent) {
                    var _result = _attachEvent('onload', _function);
                    return _result;
                } else {
                    //todo: preloading fix for ie5.2 on mac os
                    return false;
                }
            }
        } else {
            window.onload = function() {
                _onload();
                _function();
            }
        }
    }

    function onload() {
        var pubID = '${formBean.antibodyDefPubZdbID}';
        if (pubID != null || pubID != '') {
            form = document.getElementById(defPub);
            form.value = pubID;
            getPubDetail(pubID);
        }
    }
</script>

