<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">Summary Report:</td>
    </tr>
    <tr class="odd">
        <th width="150">Cron Job Name</th>
        <td>${root.jobName}</td>
    </tr>
    <tr class="even">
        <th> Start</th>
        <td> ${root.startDate}</td>
    </tr>
    <tr class="odd">
        <th> End</th>
        <td> ${root.endDate}</td>
    </tr>
    <tr class="even">
        <th> Duration</th>
        <td> ${root.duration}</td>
    </tr>
    <tr class="odd">
        <th> Status</th>
        <td> ${root.status}</td>
    </tr>
</table>


<p/>

<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">Detailed Report:</td>
    </tr>
<#list root.messageMap?keys as key>
    <#if key_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td width="200" class="cell bold">${key}</td>
    <td class="cell">${root.messageMap[key]?replace("\n","<br/>")}</td>
</tr>
</#list>
<#list root.errorMessages as error>
    <tr>
        <td width="150" class="cell bold">Error</td>
        <td>${error?replace("\n","<br/>")}</td>
    </tr>
</#list>
</table>

<#include "footer-fish-search-check-template.ftl">