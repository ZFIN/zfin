<#if reportTitle?has_content>
${reportTitle}
<#list 1..reportTitle?length as i>=</#list>

</#if>
<#if timeStamp??>
Report generated ${timeStamp?string("yyyy-MM-dd HH:mm:ss")}

</#if>
<#list introParagraphs as paragraph>
${paragraph}

</#list>
<#list summaryTables as table>
<#if table.caption?has_content>
=== ${table.caption} ===

</#if>
<#if table.data?has_content>
<#list table.data as row>
${row?join("\t")}
</#list>

</#if>
</#list>
<#list dataTables as table>
<#if table.caption?has_content>
=== ${table.caption} ===

</#if>
<#if table.data?has_content>
<#if table.head?has_content>
${table.head?join("\t")}
</#if>
<#list table.data as row>
${row?join("\t")}
</#list>

</#if>
</#list>
<#if errorMessages?has_content>
=== Errors ===

<#list errorMessages as error>
${error}

</#list>

</#if>
<#if codeSnippets?has_content>
=== Code being executed ===

<#list codeSnippets as code>
${code}

</#list>
</#if>
