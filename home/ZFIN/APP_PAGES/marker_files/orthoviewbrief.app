<!-- This file is for orthologue and ortho evidence table display 
   Parameter  (brief version):
   OID     the zdb id of a marker			
-->

<SCRIPT LANGUAGE="JAVASCRIPT" TYPE="TEXT/JAVASCRIPT">
<!--

window.alert("id: <?MIVAR>$OID<?/MIVAR>");

//-->
</SCRIPT>


 <?MIVAR>
 <TABLE width="100%" border=0 bgcolor="$highlight">
 <?/MIVAR>

<?MIVAR NAME=$lastitem><?/MIVAR>
<?MIVAR NAME=$idx>1<?/MIVAR>
<?MIVAR NAME=$CURROW>0<?/MIVAR>
<?MIVAR NAME=$EXIST>0<?/MIVAR>
<?MIVAR NAME=$hasEv>0<?/MIVAR>
<?MIVAR NAME=$ball_img><img src="/images/fill_green_ball.gif" border=0 height=10><?/MIVAR>
 <!-- Get number of orthologue organisms, so that know the number of rows-->
 <?MISQL SQL="
   select count(distinct organism)
     from orthologue
    where c_gene_id = '$OID'
    ;">  
 <?/MISQL>
 <?MIVAR NAME=$orthoNum>$1<?/MIVAR>

<SCRIPT LANGUAGE="JAVASCRIPT" TYPE="TEXT/JAVASCRIPT">
<!--

window.alert("The number of rows: <?MIVAR>$orthoNum<?/MIVAR>");

//-->
</SCRIPT>

 <?MIBLOCK COND="$(>,$orthoNum,0)">

  <!-- Get number of different evidence code-->
  <?MISQL SQL="select count(*)
		from orthologue_evidence_display
		where oevdisp_gene_zdb_id = '$OID'
	     ;">
  <?/MISQL>
  <?MIVAR NAME=$envNum>$1<?/MIVAR>

<SCRIPT LANGUAGE="JAVASCRIPT" TYPE="TEXT/JAVASCRIPT">
<!--

window.alert("The number of evidence: <?MIVAR>$envNum<?/MIVAR>");

//-->
</SCRIPT>

  <!-- Main Query to get orthologue -->
  <?MISQL SQL="
   select ortho_name, ortho_abbrev, organism, ortho_chromosome, ortho_position, fdb_DB_name,
         fdb_DB_query || dblink_acc_num, dblink_acc_num_display, a.zdb_id, 
         d.organism_display_order, fdb_db_significance
    from orthologue a, OUTER (db_link,foreign_db_contains,foreign_DB), organism d 
    where a.zdb_id = dblink_linked_recid 
      and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
      and fdbcont_fdb_db_name = fdb_db_name
      and c_gene_id = '$OID' 
      and fdb_db_significance < 10
      and a.organism = d.organism_common_name	
    order by d.organism_display_order, fdb_db_significance;">
	
   <?MIVAR NAME=$ortho_name>$1<?/MIVAR>
   <?MIVAR NAME=$ortho_abbrev>$2<?/MIVAR>
   <?MIVAR NAME=$organism>$3<?/MIVAR>
   <?MIVAR NAME=$chromosome>$4<?/MIVAR>
   <?MIVAR NAME=$position>$5<?/MIVAR>
   <?MIVAR NAME=$DB_name>$6<?/MIVAR>
   <?MIVAR NAME=$hyperlink>$7<?/MIVAR>
   <?MIVAR NAME=$dblink_acc_num_display>$8<?/MIVAR>	
   <?MIVAR NAME=$zdb_id>$9<?/MIVAR>
   <?MIVAR NAME=$db_significance>$11<?/MIVAR>
   <?MIVAR NAME=$chrLoc>&nbsp;$chromosome ($position)<?/MIVAR>

   <?MIVAR>$(SETVAR,$CURROW,$(+,$CURROW,1))<?/MIVAR> 
   <?MIBLOCK COND="$(=,$CURROW,1)">
	<?MIVAR>
        $(SETVAR,$EXIST,1)
        <TR><TD colspan=4></TD>
        <TD bgcolor=#ccccccc colspan=$envNum><b>Evidence</b></TD></TR>
	<?/MIVAR>
	<TR bgcolor=#ccccccc><TD><b>Species</b></TD><TD><b>Symbol</b></TD><TD><b>Chromosome</b></TD><TD><b>AccID</b></TD>
	<?MISQL SQL="
		select count(*), oevdisp_evidence_code, oevcode_order  
		from orthologue_evidence_display, orthologue_evidence_code
		where oevdisp_gene_zdb_id = '$OID'
	 	 and oevdisp_evidence_code = oevcode_code
		group by oevdisp_evidence_code, oevcode_order
		order by oevcode_order
		;">
 	    $(SETVAR,$oevcount[$MI_CURRENTROW],$1)
	    $(SETVAR,$oevcode[$MI_CURRENTROW],$2)          	
	<?/MISQL>
	
	<?MIBLOCK COND="$(=,$MI_ROWCOUNT,0)">
           <?MIVAR>
	   <TD bgcolor="$highlight" align=center rowspan=$(+,$orthoNum,2)>
            None submitted</TD><?/MIVAR>
	<?MIELSE>
          <?MIVAR>$(SETVAR,$hasEv,1)<?/MIVAR>
	  <?MIBLOCK INDEX=$item FOREACH=$oevcode>
    	   <?MIVAR>
	    <TD colspan=$oevcount[$idx] align=center><b><a href="/zf_info/oev.html">$item</a></b></TD>
	    $(SETVAR,$idx,$(+,$idx,1))
           <?/MIVAR>         
 	   <?/MIBLOCK>
        <?/MIBLOCK>
	</TR>
	<TR><TD><b>Zebrafish</b></TD>
        <?MIVAR><TD>$abbrev</TD>
        
        <?MICOMMENT> *** Displaying zebrafish chromosome info begins here *** <?/MICOMMENT>
        <?MISQL NAME=$longChrString 
                SQL="
                   select WebExplode(object,'&mapdetails_mode=mini&oID=$OID') 
                   from webPages where ID='aa-mappingdetail.apg';">
        $1
        <?/MISQL>
        <?MIVAR NAME=$noLGChrString>$(TRIM,$(REPLACE,$longChrString,"LG:",""))<?/MIVAR>
        <?MIVAR NAME=$posOfSpace>$(POSITION,$noLGChrString," ")<?/MIVAR>
        <?MIVAR NAME=$shortChrString>$(SUBSTR,$noLGChrString,1,$posOfSpace)<?/MIVAR>
        <?MIVAR NAME=$restChrString>$(SUBSTR,$noLGChrString,$(+,$posOfSpace,1))<?/MIVAR>
    
        <?MIVAR NAME=$posOfCommer>$(POSITION,$restChrString,",")<?/MIVAR>
        <?MIBLOCK COND="$(>,$posOfCommer,0)">
           <?MIVAR NAME=$noCommerString>$(TRIM,$(REPLACE,$restChrString,",",""))<?/MIVAR>
           <?MIVAR NAME=$posOfSpace>$(POSITION,$noCommerString," ")<?/MIVAR>
           <?MIVAR NAME=$secondChrString>$(SUBSTR,$noCommerString,1,$posOfSpace)<?/MIVAR>
           <?MIVAR NAME=$shortChrString>$shortChrString, $secondChrString<?/MIVAR>
        <?/MIBLOCK>
        
        <?MIVAR NAME=$posOfNone>$(POSITION,$shortChrString,"None")<?/MIVAR>       
        <TD><?MIVAR>$(IF,$(=,$posOfNone,0),$shortChrString,Unknown)<?/MIVAR></TD> 
        <?MICOMMENT> *** Displaying zebrafish chromosome info ends here *** <?/MICOMMENT>     
        
        <TD bgcolor=$highlight></TD><?/MIVAR>
  	<?MIBLOCK COND="$(AND,$(>,$MI_ROWCOUNT,0),$(>,$envNum,0))">	 
	    <?MIBLOCK INDEX=$subitem TO=$envNum FROM=1 STEP=1>
	      <TD align=center><b><?MIVAR>$ball_img<?/MIVAR></b></TD>	   
	    <?/MIBLOCK>
	<?/MIBLOCK>
	</TR>
  <?/MIBLOCK> <?MICOMMENT> *** first 3 rows end here *** <?/MICOMMENT>
 		
  <?MIBLOCK COND="$(AND,$(NC,$lastitem,$organism),$(>,$CURROW,1))">
    <?MICOMMENT> *** there used to be a </TD> here *** <?/MICOMMENT>
    <?MISQL SQL="
            select oevdisp_organism_list, oevcode_order
              from orthologue_evidence_display, orthologue_evidence_code
             where oevdisp_gene_zdb_id = '$OID'
	       and oevdisp_evidence_code = oevcode_code
	     order by oevcode_order, oevdisp_organism_list
	     ;">
       <?MIVAR>
       $(IF,$(>,$(POSITION,$1,$lastitem),0),<TD align=center><b><?MIVAR>$ball_img<?/MIVAR></b></TD>, <TD></TD>)
       <?/MIVAR>
     <?/MISQL>
    </TR><TR>
  <?/MIBLOCK>

  <?MIBLOCK COND="$(NC,$organism,$lastitem)">
	<?MIVAR>
	    <TD><b>$organism</b></TD><TD>$ortho_abbrev</TD>
            <?MICOMMENT> *** Display chromosome Info *** <?/MICOMMENT>
            <TD>$chrLoc</TD>
        <?/MIVAR>
     <TD>   <?MICOMMENT>  *** ??? *** <?/MICOMMENT>
  <?/MIBLOCK>

  <?MIVAR>
  $(IF,$(NC,$dblink_acc_num_display,),"<li><A HREF="$hyperlink">"$DB_name":"$dblink_acc_num_display"</a>") 
  $(SETVAR,$lastitem,$organism)
  <?/MIVAR>
 <?/MISQL>
 </TD>
 <!-- Display the orthologue code for the last organism -->
 <?MISQL SQL="
            select oevdisp_organism_list, oevcode_order
              from orthologue_evidence_display, orthologue_evidence_code
             where oevdisp_gene_zdb_id = '$OID'
	       and oevdisp_evidence_code = oevcode_code
	     order by oevcode_order, oevdisp_organism_list
	     ;">
       <?MIVAR>
       $(IF,$(>,$(POSITION,$1,$lastitem),0),<TD align=center><b>$ball_img</b></TD>, <TD></TD>)
       <?/MIVAR>
  <?/MISQL>
  </TR> 
  

  <!-- Query for references -->
  <?MIBLOCK COND="$(=,$hasEv,1)">
   <TR><TD bgcolor=#ccccccc colspan=4 align=right>Number of References</TD>
   <?MISQL SQL="
	select count(*), recattrib_data_zdb_id,
               oevdisp_organism_list, oevcode_order  
	  from orthologue_evidence_display, orthologue_evidence_code, 
               record_attribution
 	 where oevdisp_gene_zdb_id = '$OID'
 	   and oevdisp_evidence_code = oevcode_code
           and oevdisp_zdb_id = recattrib_data_zdb_id
	group by recattrib_data_zdb_id,
		oevdisp_organism_list, oevcode_order 
	order by oevcode_order,oevdisp_organism_list
	;">
     <?MIVAR NAME=$count>$1<?/MIVAR>
     <?MIVAR NAME=$oevdispId>$2<?/MIVAR>
     <TD align=center>
     <?MISQL COND="$(=,$count,1)" SQL="
	select recattrib_source_zdb_id 
	  from record_attribution
	 where recattrib_data_zdb_id = '$oevdispId' ; ">
	(<A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-pubview2.apg&OID=$1">$count</A>)
     <?/MISQL>
     <?MIVAR COND="$(>,$count,1)"> 
        (<A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-showpubs.apg&OID=$oevdispId&rtype=marker&title=$(URLENCODE,$title)&name=$(URLENCODE,$name)&abbrev=$abbrev">$count</A>)<?/MIVAR>
     </TD>
   <?/MISQL>
   </TR>
  <?/MIBLOCK>

  <?MIVAR>
	$(UNSETVAR,$oevcount)
	$(UNSETVAR,$oevcode)
	
  <?/MIVAR>	

<?MIELSE> <?MICOMMENT> *** if no orthologue data *** <?/MICOMMENT>
  <TR><TD>&nbsp None submitted.</TD></TR>
<?/MIBLOCK>

</TABLE>