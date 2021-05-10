<#include "header-template.ftl">
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">Antibodies added to Wiki: ${root.createdPages?size} antibodies</td>
    </tr>
</table>
<hr/>
<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="300">Antibody Name</th>
    </tr>
<#list root.createdPages as row>
    <#if row_index%2==0>
    <tr class="odd">
    <#else>
    <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row}</td>
</tr>
</#list>
</table>

<hr/>
<table class="rowstripes">
    <tr>
        <td colspan="2" class="header-table">Antibodies dropped in Wiki: ${root.droppedPages?size} antibodies</td>
    </tr>
</table>
<table class="rowstripes">
    <tr>
        <th>#</th>
        <th width="300">Antibody Name</th>
    </tr>
<#list root.droppedPages as row>
    <#if row_index%2==0>
    <tr class="odd">
    <#else>
    <tr class="even">
    </#if>
    <td>${row_index+1}</td>
    <td>${row}</td>
</tr>
</#list>
</table>
