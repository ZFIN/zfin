<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <th colspan="2" class="header-table">The following terms were un-obsoleted</th>
    </tr>
</table>
<hr/>
<table class="rowstripes">
    <tr class="header-table">
        <th>#</th>
        <th width="150">Term ID</th>
        <th >OBO ID</th>
        <th width="250">Term Name</th>
        <th >Ontology</th>
        <th >Obsolete</th>
    </tr>
<#list root.rows as row>
    <#if row_index%2==0>
    <tr class="odd">
        <#else>
        <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row[0]}</td>
    <td>${row[1]}</td>
    <td>${row[2]}</td>
    <td>${row[3]}</td>
    <td>${row[4]}</td>
</tr>
</#list>
</table>
<#include "footer-template.ftl">