#!/usr/bin/perl
require "header.pl";
require "footer.pl";

use CGI;

my $Query = new CGI();

print "Content-type: text/html\n\n";
header();

if ($Query->param('scenario') eq "2") {

  $scenario = "2";

  $action = "fxQuery.cgi";
  $results = "true";

  $gene = "";
  $gene_disabled = "disabled";

  $structures = "spinal cord";
  $structures_disabled = ""; 

  

} else {

  $scenario = "1";

  $action = "fxResultsSummary.cgi";
  $results = "false";

  $gene = "epha4b";
  $gene_disabled = "";

  $structures = "";
  $structures_disabled = "disabled"
}


  $results_html = <<ENDRESULTS;
<br>
    <TABLE bgcolor="CCCCCC"> 
      <TR>
       <TD colspan=2>
       <B><font size=-1>Anatomy Items Searched...</font></B>
       </TD>
      </TR>
      <TR>
       <TD bgcolor="#EEEEEE" valign=top> 
        <font size=-1 style="font-family:arial; font-weight:bold;">
	<u>presumptive spinal cord</u></font> 
      </TD>
      
      <TD bgcolor="#EEEEEE">
      <FONT size=-1 style="font-family:arial;">       
      has <u><B>(4)</B></u> gene(s), total, expressed in all stages 
     </FONT>
     <FONT size=-1 style="font-family:arial; font-weight:bold;  ">

      <I>(synonym match)</I> 
     </FONT>
   </TD>
   </TR>
  
  </TABLE>     
  <br>
  <table width=100% border=0>
    <tr>
      <td width=20%>&nbsp;</td>
      <td width=60% align=center>
        <font size="+1">Expression Pattern Search Results<br>
          (<b>4</b> genes with expression)</font>
      </td>
      <td width="20%" align="center">
        <u>Modify Search</u>
        <!-- Insert a form with one button. Label button Your Input Welcome -->
         <table leftmargin="0" topmargin="0" marginwidth="0" marginheight="0"
         border="0" cellspacing="0" cellpadding="0">
    <form method=post 
          action="/cgi-bin_iguana/webdriver" 
          target=comments>
      <input type=hidden name=MIval value="aa-input_welcome_generic.apg">
      <input type=hidden name=page_name value="ZFIN Expression Search Results">
     <tr>
      <td>
        <input type=submit value="Your Input Welcome" disabled>
      </td>
    </tr>
    </form>
  </table>
    </td>
    </tr>
  </table>   
    <TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
      <TR>
        <TH> &nbsp; </TH>
        <TH align="left">Gene symbol - name</TH>
	<TH align="left">Expression <font size="-1">(<u>current status</u>)</font></TH>

	<TH align="left">Publication(s)</TH>
      </TR>

  <!-- row one -->
  <TR bgcolor="#EEEEEE">

    <TD> &nbsp; </TD>
    <TD valign="top">      
      <i><u>ashb</u>&nbsp;&nbsp;- achaete-scute complex-like 1b (Drosophila)</i>    
    </TD>
    <TD valign="top">
      <u>Expression Data</u> (n assays)
      <font size="-1">(15&nbsp;images)</font>    
    </TD>
    <TD valign="top">
      <u>(6)</u>
    </TD>
 </TR>
 
 <!-- row two -->
 <TR bgcolor="#FFFFFF">
   <TD valign="top">&nbsp;</TD>
   <TD valign="top"> 
     <i><u>hdac1</u>&nbsp;&nbsp;- histone deacetylase 1 </i>
   </TD>
   <TD valign="top">     
     <u>Expression Data </u> (n assays)
     <font size="-1">(9&nbsp;images)</font>
   </TD>
   <TD valign="top"> 
     <u>(1)</u>   
   </TD>
 </TR>

 <!-- row three -->
 <TR bgcolor="#EEEEEE">
    <TD valign="top">&nbsp;</TD>
    <TD valign="top">
    <i><u>neurog1</u>&nbsp;&nbsp;- neurogenin 1 </i>
    </TD>
    <TD valign="top">
      <a href="fxExpSummary.cgi">Expression Data</a>
    <small>(71&nbsp;assays) (1 image)</small>
    </TD>
    <TD valign="top">
      <u>(6)</u>
    </TD>
 </TR>

 <!-- row four -->
 <TR bgcolor="#FFFFFF">
    <TD>&nbsp;</TD>
   <TD valign="top">
    <i><u>pax2a</u>&nbsp;&nbsp;- paired box gene 2a</i>
   </TD>
   <TD valign="top">
     <u>Expression Data</u>
    <small>(71&nbsp;assays) (12&nbsp;images)</small>
   </TD>
   <TD valign="top">
     <u>(5)</u>
   </TD>
 </TR>

</TABLE>


ENDRESULTS

if ($Query->param('results') eq "true") {
  print $results_html;
} else {
  print "[<a href=\"fxQuery.cgi?scenario=1\">Scenario 1</a> | <a href=\"fxQuery.cgi?scenario=2\">Scenario 2</a> | <a href=fxExpSummary.cgi>Link from Gene page</a> | <a href=fxFigures.cgi>Link from Publications Page </a> ]<br>";
}

print <<ENDHTML ; 
<table border=0 width=100% cellpadding=3 cellspacing=0>
    <tr>
      <td bgcolor="#CCCCCC" width=70%>
         &nbsp;&nbsp;

	
          <font size="+1"><b>Search for Gene Expression Data </b></font>
	

      </td>

      <td bgcolor="#CCCCCC" align=right>
        <!-- Insert a form with one button. Label button Your Input Welcome -->
        <table leftmargin="0" topmargin="0" marginwidth="0" marginheight="0"
         border="0" cellspacing="0" cellpadding="0">

    
    <tr>
      <td>
        <input type=submit value="Your Input Welcome">
      </td>
    </tr>

  </table>

      </td>

    </tr>
  </table>

        <form method=get
        action="$action">

    <input type="hidden" name="scenario" value="$scenario">
    <input type="hidden" name="results" value="$results">
        
    <table border=0 width=100% cellpadding=3 cellspacing=0>
      
	<tr>

	  <td nowrap>

	    <!-- gene name -->

	    <font size=2><b>Gene/EST</b></font>

	    <select name=searchtype disabled>
	      <option value=begins >Name begins with
	      <option value=contains SELECTED>Name contains
	    </select>
	    <input type=text name=gene_name size=30
		    value="$gene" $gene_disabled >

	    <!-- end gene name -->
	  </td>
	  <td rowspan=2>
	    <!-- stage -->
	    <font size=2><b> Between stages:</b> <br>
	      <select NAME="stage_start" disabled SIZE=1><option SELECTED VALUE="ZDB-STAGE-010723-4"> Zygote:1-cell</select>
              <b>&</b><br>
	      <select NAME="stage_end" disabled SIZE=1><option  SELECTED VALUE="ZDB-STAGE-010723-4"> Zygote:1-cell </select>
	    </font>
	    <br>Developmental Staging Series
	    <!-- end stage -->
	  </td>
	</tr>
	<tr>
	  <td>
	    <!-- begin mutant -->

	    <font size=2><b>Mutant Background</b></font>
	      <select name=mutsearchtype disabled>
		<option value=begins >Name begins with
		<option value=contains SELECTED>Name contains
	      </select>
	      <input type=text name=mutant size=20
		    value="" disabled>
	      <!-- end mutant -->
	    </font>
	  </td>

	</tr>
	<tr>
	  <td>
	    <!-- begin author -->
	    <font size=2><b>Author</b></font>
	      <select name=authsearchtype disabled>
		<option value=begins >First Author begins with
		<option value=contains SELECTED>Name contains
	      </select>

	      <input type=text name=author size=28  value="" disabled>
	    </font>
	    <!-- end author -->
	  </td>
	  <td>
	    <!-- begin assay type -->
	    <font size=2><b>Assay Type</b></font>
	      <select NAME=assay disabled>

		<option SELECTED value=ANY>ANY
		
		  <option  value='RNA in situ whole mount'>RNA in situ whole mount
		
		  <option  value='RNA in situ section'>RNA in situ section
		
		  <option  value='protein in situ whole mount'>protein in situ whole mount
		
		  <option  value='protein in situ section'>protein in situ section
		
		  <option  value='Northern blot'>Northern blot
		
		  <option  value='Western blot'>Western blot
		
		  <option  value='RT-PCR'>RT-PCR
		
		  <option  value='cDNA clones'>cDNA clones
		
		  <option  value='RNase protection'>RNase protection
		
	      </select>
	    </font>
	    <!-- end assay type -->
	  </td>

	</tr>
      
      <tr>

        

        <td colspan=2> 
	  <table border=0 bgcolor="#EEEEEE"> 
            <tr>
	      <td colspan=2>
		<font size=2><b>Expressed In</b></font>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;


 	          <font size="-1">[<i>Enter a return separated list of structures</i>]</font>

	     </td>
	     </tr>
	     <tr>
	     <td>
	        <textarea cols="30" rows="5" name="TA_selected_structures" onChange="process_structures(document.critform.TA_selected_structures.value);" $structures_disabled >$structures</textarea>

	        <input type="hidden" name="processed_selected_structures"  disabled>

	 	<br>
		<font SIZE=-1>
		  <b>Search</b> the Anatomical Ontology</a>
		</font>
	      </td> 
	      <td>

		
		<font size=-1>
		  
		    
		  

	          <input type=checkbox name="include_substructures" value="checked"  checked=checked  disabled>  Include substructures <br>

		  

	          <b>Return data for genes<br>expressed in:</b> <br>
		  <input type="radio" name="structure_bool" value="and" checked=checked disabled
	          ><b>Every</b> structure entered
		  <br>

		  <input type="radio" name="structure_bool" value="or"  disabled  > 
	          <b>Any</b> of structure entered 
		</font>
	      </td>
	    </tr>
	  </table>
        </td>
      </tr>

      <tr>
	<td class="resultcount">
	  Display results in groups of 
	  <input type="text" name="WINSIZE" size="3" disabled onChange='document.critform.START.value = "";' value=25>.
	   
	</td>

	<td>
          <input type="checkbox" name="xpatsel_sortByDate" value="sortByDate" disabled >
	  Sort by release date
	</td>	
      </tr>

      <tr>
	<td colspan=3 bgcolor=#CCCCCC align=right> 
	
	  <input type=submit value=Search onClick="call_sumbit()">
	  <input type=button value=Reset onClick="call_reset();">

        </td>
      </tr>
    </table>
  </form>
ENDHTML
footer();
