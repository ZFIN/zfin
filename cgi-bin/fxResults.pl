#!/usr/bin/perl
require "header.pl";
require "footer.pl";
 print "Content-type: text/html\n\n";
 header();
 print <<ENDHTML ; 
<table width=100% border=0>
    <tr>
      <td width=20%>&nbsp;</td>
      <td width=60% align=center>
        <font size="+1">Expression Pattern Search Results<br>

         
	  (<b>1</b> genes with expression)</font>
        
      </td>
      <td width="20%" align="center">
        <a href=#modify>Modify Search</a>
        <!-- Insert a form with one button. Label button Your Input Welcome -->
        
	  <table leftmargin="0" topmargin="0" marginwidth="0" marginheight="0"
         border="0" cellspacing="0" cellpadding="0">

    <form method=post 
          action="<!--|WEBDRIVER_PATH_FROM_ROOT|-->" 
          target=comments>
      <input type=hidden name=MIval value="aa-input_welcome_generic.apg">
      <input type=hidden name=page_name value="ZFIN Expression Search Results">
    
    <tr>
      <td>
        <input type=submit value="Your Input Welcome">
      </td>
    </tr>

    </form>
  </table>

	
      </td>
    </tr>
  </table>

  
    
    <table width="100%" border="0" cellspacing="0" cellpadding="3">
      <tr>
        <th>&nbsp;</th>

        <th align="left">Gene symbol - name</th>
	<th align="left">Expression <font size="-1"></font></th>
	<th align="left">Publication(s)</th>
	
      	  <td align=left><b>Matching Text</b></th>
        
      </tr>

ENDHTML
footer();
