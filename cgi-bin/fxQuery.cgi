#!/usr/bin/perl
require "header.pl";
require "footer.pl";

use CGI;

my $Query = new CGI();

print "Content-type: text/html\n\n";
header();

if ($Query->param('scenario') == 2) {

  $scenario = "2";

  $gene = "";
  $gene_disabled = "disabled";

  $structures = "spinal cord";
  $structures_disabled = "";

} else {

  $scenario = "1";

  $gene = "epha4b";
  $gene_disabled = "";

  $structures = "";
  $structures_disabled = "disabled"
}

print "[<a href=\"fxQuery.cgi?scenario=1\">Scenario 1</a> | <a href=\"fxQuery.cgi?scenario=2\">Scenario 2</a> ]<br>";

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

        <form method=post
        action="fxResultsSummary.cgi">

    
    
    
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
