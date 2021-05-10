<#include "header-template.ftl">
<style type="text/css">
    h3 {
        font-variant: small-caps;
    }
</style>

<table class="primary-entity-attributes summary" style="text-align: left;">
<#if root.name??>
    <tr>
        <th>
            <span class="name-label">User Name</span>
        </th>
        <td>
        ${root.name}
        </td>
    </tr>
</#if>

    <tr>
        <th>
            <span class="name-label">Request</span>
        </th>
        <td>
        ${root.request}
        </td>
    </tr>

<#if root.queryRequestString??>
    <tr>
        <th>
            <span class="name-label">Parameter String</span>
        </th>
        <td>
        ${root.queryRequestString}
        </td>
    </tr>
</#if>

<#if root.requestParameterMap??>
    <tr>
        <th>
            <span class="name-label">Parameters</span>
        </th>
        <td>
            <table>
                <#assign keys = root.requestParameterMap?keys>
                <#list keys as key>
                    <tr>
                        <td>${key} </td>
                        <td>${root.requestParameterMap[key]}</td>
                    </tr>
                </#list>
            </table>
        </td>
    </tr>
</#if>

<#if root.tomcatJSessioncookie??>
    <tr>
        <th>
            <span class="name-label">Session ID</span>
        </th>
        <td>
        ${root.tomcatJSessioncookie.value}
        </td>
    </tr>
</#if>

<#if root.httRequest.remoteAddr??>
    <tr>
        <th>
            <span class="name-label">Remote Address</span>
        </th>
        <td>
        ${root.httRequest.remoteAddr}
        </td>
    </tr>
</#if>

<#if root.referrer??>
    <tr>
        <th>
            <span class="name-label">Referral URL</span>
        </th>
        <td>
        ${root.referrer}
        </td>
    </tr>
</#if>

<#if root.userAgent??>
    <tr>
        <th>
            <span class="name-label">Browser</span>
        </th>
        <td>
        ${root.userAgent}
        </td>
    </tr>
</#if>

    <tr>
        <th>
            <span class="name-label">Date</span>
        </th>
        <td>
        ${root.requestDate?datetime}
        </td>
    </tr>

</table>

<p/>

<h3>Errors: </h3>
<table class="searchresults">
<#if loggingEvents??>
    <#list loggingEvents as event>
        <tr style="font-size: 12px; background-color: #ed996b;">
            <td>${event.renderedMessage} </td>
        </tr>
        <#if event.throwableStrRep??>
            <#list event.throwableStrRep as stackLine>
                <tr style="font-size: 10px">
                    <#if stackLine?starts_with("Caused by:")>
                        <td style="background-color: #CCCCFF;">${stackLine}</td>
                    <#else>
                        <td>${stackLine}</td>
                    </#if>
                </tr>
            </#list>
        </#if>
    </#list>
</#if>
</table>

<p/>
<h3>Database Locks: </h3>
<table class="searchresults">
    <tr>
        <th style="text-align: left">Table Name</th>
        <th style="text-align: left">Lock Type</th>
        <th style="text-align: left"># of Locks</th>
        <th style="text-align: left">Lock Scope</th>
    </tr>
<#if root.locks??>
    <#list root.locks as lock>
        <tr style="font-size: 10px">
            <td>${lock.tableName}</td>
            <td>${lock.type}</td>
            <td>${lock.numOfLocks}</td>
            <td>${lock.rowLength}</td>
        </tr>
    </#list>
</#if>
</table>


</body>
</html>
