<html>
<head>
<title>Search ZFIN Tips</title>
<style type="text/css">
.help_body   { margin-left: 40px; margin-right: 40px; }
</style>
</head>
<script language="JavaScript" src="/header.js"></script>

<div class="help_body">
    <h2 align="center">Search ZFIN Tips</h2>
    <p>The <b>Search ZFIN</b> feature provides a quick and easy glance at data available on the ZFIN web site.  You may use this feature by entering the topic of interest to you.  Snapshots of all ZFIN pages are searched for an occurrence of the query string
     <p>You may find differences in results from the <b>Search ZFIN</b> versus the specific search forms. <b>Search ZFIN</b> will scan the entire ZFIN site including the Zebrafish Book, meetings and jobs.  <b>Search ZFIN</b> does not take advantage of the relationships that are defined for data in our database.  For a comprehensive search of expression or anatomical structures we recommend you use the <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-xpatselect.apg">Expression</a> or  <A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-anatdict.apg&mode=search">Anatomy</a> search forms.
     <p>If you do not find what you are looking for, please be sure to try one of ZFIN's more specific search forms that may be accessed from the links in the top menu bar.</p>
    <h3>Comparison of <b>Search ZFIN</b> and specific ZFIN search forms</h3>
    <br>
    <table>
      <tr>
	<th> </th>
        <th>ZFIN Search</th> 
        <th>Specific Search Forms</th>
      </tr>
      <tr>
        <td><b>Highly Customizable Searches</b></td>
        <td align="center">No</td>
        <td align="center">Yes</td>
      </tr>
      <tr>
        <td><b>Uses established relationships between data in ZFIN</b></td>
        <td align="center">No</td>
        <td align="center">Yes</td>
      </tr>
      <tr>
        <td><b>Provides a quick view of data in ZFIN related to query term</b></td>
        <td align="center">Yes</td>
        <td align="center">No</td>
      </tr>
    </table>
      <p>Search for <b>eye</b> using <b>Search ZFIN</b></p>
      <ul>
	<li> Results will include all occurrences of <b>eye</b> and words for which <b>eye</b> is a root such as:<b>eye</b>less, <b>eye</b>sight, <b>eye</b>cup.  
	<li>Results are organized by categories that reflect the various data types in ZFIN. Categories include:
	  <ul>
	    <li>Mutants/Trangenics
	    <li>Genes/Markers/Clones
	    <li>Expression
	    <li>Images
	    <li>Anatomy
	    <li>Publications
	    <li>People
	    <li>The Zebrafish Book
	  </ul>
	<li>The number of matches for each category are displayed.  
	<li>Each category links to a complete list of matches for that category.
	<li>Each item in the complete results category will link directly to data for that category type. 
      </ul>
      <p>Search for <b>eye</b> using <b>Gene Expression Search Form</b></p>
      <ul>
	<li>Queries may be refined by specifying other parameters such as: Gene/EST, developmental stage or stage range,author or assay name.
	<li>Results will include all expression data that has been annotated to the <b>eye</b> and to any substructures of the <b>eye</b>. (lens, optic vesicle, presumptive retina, etc.)
        <li>Results are limited to expression data.
	<li>Results list will provide hot links to expression data, genes and publications.
	<li>Results are ordered by gene symbol.
      </ul>
      <p>
    <h3>Wildcard Searches</h3>
      <p><b>Search ZFIN</b> does not require use of wildcards.  Wildcard matches are performed automatically. To search for all hox genes, you may use the search: <b>hox</b><br>
      Links to all members of the hox gene family (<b>hox</b>a1a, <b>hox</b>a2b, <b>hox</b>a3a, etc.)</p>

    <h3>Multiple Word Searches</h3>
      <p>Searches may include one or more words separated by spaces.  A search for <b>inner ear</b> will return instances of <b>inner ear</b> as well as matches to root words <b>inner</b> and <b>ear</b> such as <b>inner</b>vating, <b>ear</b>ly, and <b>ear</b>ly <b>ear</b>.</p>

   <h3>Excluded Words</h3>
      <p>Commonly occurring words such as <b>from</b>, <b>any</b>, <b>but</b>, <b>some</b>, <b>were</b>, <b>what</b> and <b>when</b> that are specified in a query are not included in search results.</p>


<p><b>Search ZFIN</b> is powered by <a HREF="http://jakarta.apache.org/lucene/docs/index.html">Lucene</a>.</p>
<p>&nbsp;</p>

<script language="JavaScript" src="/footer.js"></script>
