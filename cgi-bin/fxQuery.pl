#!/usr/bin/perl
sub fxQuery {
require "header.pl";
require "footer.pl";
 print "Content-type: text/html\n\n";
 header();
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

  <form name=critform 
        method=post
        action="/cgi-bin/webdriver">
    <input type=hidden name=MIval value=aa-xpatselect.apg>
    <input type=hidden name=query_results value="exist">
    <input type=hidden name=START value="0">

    
    
    
    <table border=0 width=100% cellpadding=3 cellspacing=0>
      
	<tr>

	  <td nowrap>

	    <!-- gene name -->

	    <font size=2><b>Gene/EST</b></font>

	    <select name=searchtype>
	      <option value=begins >Name begins with
	      <option value=contains SELECTED>Name contains
	    </select>
	    <input type=text name=gene_name size=30
		    value="epha4b"  >

	    <!-- end gene name -->
	  </td>
	  <td rowspan=2>
	    <!-- stage -->
	    <font size=2><b> Between stages:</b> <br>
	      <select NAME="stage_start" SIZE=1>
	      <b>&</b><br>
	      <select NAME="stage_end" SIZE=1>
	    </font>
	    <br>Developmental Staging Series
	    <!-- end stage -->
	  </td>
	</tr>
	<tr>
	  <td>
	    <!-- begin mutant -->

	    <font size=2><b>Mutant Background</b></font>
	      <select name=mutsearchtype>
		<option value=begins >Name begins with
		<option value=contains SELECTED>Name contains
	      </select>
	      <input type=text name=mutant size=20
		    value="">
	      <!-- end mutant -->
	    </font>
	  </td>

	</tr>
	<tr>
	  <td>
	    <!-- begin author -->
	    <font size=2><b>Author</b></font>
	      <select name=authsearchtype>
		<option value=begins >First Author begins with
		<option value=contains SELECTED>Name contains
	      </select>

	      <input type=text name=author size=28  value="">
	    </font>
	    <!-- end author -->
	  </td>
	  <td>
	    <!-- begin assay type -->
	    <font size=2><b>Assay Type</b></font>
	      <select NAME=assay>

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
	        <textarea cols="30" rows="5" name="TA_selected_structures" onChange="process_structures(document.critform.TA_selected_structures.value);"></textarea>

	        <input type="hidden" name="processed_selected_structures" >

	 	<br>
		<font SIZE=-1>
		  <a HREF="/cgi-bin/webdriver?MIval=aa-anatdict.apg&mode=search"><b>Search</b> the Anatomical Ontology</a>
		</font>
	      </td> 
	      <td>

		
		<font size=-1>
		  
		    
		  

	          <input type=checkbox name="include_substructures" value="checked"  checked=checked  >  Include substructures <br>

		  

	          <b>Return data for genes<br>expressed in:</b> <br>
		  <input type="radio" name="structure_bool" value="and" checked=checked
	          ><b>Every</b> structure entered
		  <br>

		  <input type="radio" name="structure_bool" value="or"    > 
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
	  <input type="text" name="WINSIZE" size="3" onChange='document.critform.START.value = "";' value=25>.
	   
	</td>

	<td>
          <input type="checkbox" name="xpatsel_sortByDate" value="sortByDate"  >
	  Sort by release date
	</td>	
      </tr>

      <tr>
	<td colspan=3 bgcolor=#CCCCCC align=right> 
	
	  <input type=button value=Search onClick="call_submit();">
	  <input type=button value=Reset onClick="call_reset();">

        </td>
      </tr>
    </table>
  </form>
ENDHTML
footer();
}
1;
