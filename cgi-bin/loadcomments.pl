#!/private/bin/perl -w
##/usr/bin/perl

use DBI;

  ### the hard coded env paths need a better idea
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';

  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', 
                         '', '', 
                         {AutoCommit => 1, RaiseError => 1})
    or die "Failed while connecting to <!--|DB_NAME|--> ";


#User must specify whether to update comments or keywords or both.
($ARGV[2] == 1 || $ARGV[2] == 2 || $ARGV[2] == 3) or die "command args needs arg[2] as int.\n\t(1) update comments\n\t(2) update keywords\n\t(3) update comments & keywords\n";

#Get database name or deny execution
if($ARGV[3]){
  $db = $ARGV[3];
}
else {die "specify db at ARGV[3]";}

#For each file, count the number of lines. 
$oldRecordCount = &numDataRecords($ARGV[0],"\t",6) 
  or die "Cant open file at arg[0]";
$newRecordCount = &numDataRecords($ARGV[1],"\t",6) 
  or die "Cant open file at arg[1]";

#Both counts should be the same.  Exit with warning if counts are different.
if($newRecordCount != $oldRecordCount)
  {
    &printLineDiff($ARGV[0],$ARGV[1]);
  }

($newRecordCount != 0) or die "No data records were found";

#create unique date/time identifiers for output files
($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$datetime = "$year.$mon.$mday.$hour.$min.$sec";

$keyword_stg_window_did_not_overlap = "keyword_stg_window_did_not_overlap$datetime";
$keyword_list_matching_stg_window_denied_list = "keyword_list_matching_stg_window_denied_list$datetime";
$non_keyword_list = ">>non_keyword_list$datetime";
$keyword_list = ">>keyword_list$datetime";
$rejected_comment_list = "rejected_comment_list$datetime";
$comment_list = "comment_list$datetime";

#Parse, Compare and store relevant data.
#Parse:
open(OLD, "$ARGV[0]") or die "Cant open OLD file at arg[0]"; 
#Read old file
$i = 0;
while(<OLD>)  #for each line in the file
{
  #split the fields
  ($gene,$xpat_id,$start_stg_name,$end_stg_name,$comments,$keywords) = split /\t/, $_, 6;
      
  #save important data
  $xpatstgOld[$i] = "$gene|$xpat_id|$start_stg_name|$end_stg_name";
  $comments =~ s/description: //;
  while($comments =~ /\s\Z/)
    {
      chop($comments) 
    }
  $commentsOld[$i] = $comments;
  while($keywords =~ /\s\Z/)
    {
      chop($keywords) 
    }
  $keywordsOld[$i] = $keywords;

  #print $gene."\n";
  #don't forget to increment
  $i++;
}#end while OLD
close(OLD); 


open(NEW, "$ARGV[1]") or die "Cant open NEW file at arg[1]";
#read new file
$i = 0;
while(<NEW>)  #for each line in the file
{
  #split the fields
  ($gene,$xpat_id,$start_stg_name,$end_stg_name,$keywords,$comments) = split /\t/, $_, 6;

  $xpatstgNew[$i] = "$gene|$xpat_id|$start_stg_name|$end_stg_name";
  
  while($comments =~ /\s\Z/)
    {
      chop($comments) 
    }
  $commentsNew[$i] = $comments;
  $keywords =~ s/, /,/;
  while($keywords =~ /\s\Z/)
    {
      chop($keywords) 
    }
  $keywordsNew[$i] = $keywords;

  #don't forget to increment
  $i++;
}#end while OLD
close(NEW);


&dataFileRecordsMatch() || die "Unmatched Data records";


if($ARGV[2] == 1 || $ARGV[2] == 3){
$addedKeywords[$newRecordCount-1] = 0;
#compare keywordOld vs. keywordNew
#Write all keywords in New to $addedKeywords if kword not in Old.
for($i=0; $i<scalar(@keywordsOld); $i++)
{
  @keyword = split /,/, $keywordsNew[$i];
  #print "$i keywords: @keyword";

  for($j=0; $j <= scalar(@keyword); $j++)
  {
    while($keyword[$j] =~ /\s\Z/) #remove trailing white space
      {
	chop($keyword[$j]) 
      }
    unless($keywordsOld[$i] =~ /$keyword[$j]/)
    {
      $keyword[$j] =~ s/\A\s//; #remove leading white space
      if($addedKeywords[$i])
      {
	$addedKeywords[$i].=",$keyword[$j]";
      }
      else
      {
	$addedKeywords[$i] = $keyword[$j];
      }
    }
      #print "$i $keywordsOld[$i] : $keyword[$j]\n";
  }
}


#insert new keywords
for($i=0; $i<=scalar(@addedKeywords); $i++)
  {
    if($addedKeywords[$i])
      {
	($geneN,$xpatN,$start_stg_name_N,$end_stg_name_N) = split /\|/,$xpatstgNew[$i];
	($geneO,$xpatO,$start_stg_name_O,$end_stg_name_O) = split /\|/,$xpatstgOld[$i];

	$start_stg_zdb_N = getStgZdbFromStgName($start_stg_name_N);
	$end_stg_zdb_N = getStgZdbFromStgName($end_stg_name_N);
	
	#$start_stg_zdb_O = getStgZdbFromStgName($start_stg_name_O);
	#$end_stg_zdb_O = getStgZdbFromStgName($end_stg_name_O);


	@keywords = split /,/,$addedKeywords[$i];
	foreach $keyword (@keywords)
	  {
	    #$waiting = 1;
	    #1.validate anatitem exists in ZFIN
	    #open(QUERY,">keyword.sql");
	    #&lock();
	    #print QUERY "select anatitem_zdb_id from anatomy_item where anatitem_name = \"$keyword\";";
	    #&unlock();
	    #close(QUERY);
#	    #chmod 0700,"keyword.sql";
	    #while ($waiting){}
	    #$queryResults = `dbaccess $db keyword.sql`;
	    #$queryResults =~ /(ZDB-.*?)\s/s;
	    #$anat_zdb = $1;
	    #$anat_zdb =~ s/\s//; #remove all white space
	    #print "$i $anat_zdb\n";

	    $queryDB = $dbh->prepare( "select anatitem_zdb_id 
                                   from anatomy_item 
                                   where anatitem_name = \"$keyword\";") 
              or die "Cannot prepare statement: $DBI::errstr\n";
	    $queryDB->execute;
	    $anat_zdb = $queryDB->fetchrow();
	    $queryDB->finish;
	    
	    if ($anat_zdb && $anat_zdb =~ /ZDB/)
	      {
		#2.check stage constraint for anatitems
		#3.add keyword
		open(QUERY,">insertKeyword.sql");
		print QUERY "execute function expression_pattern_anatomy_insert_anatitem(\"$anat_zdb\",\"$xpatN\",\"$start_stg_zdb_N\",\"$end_stg_zdb_N\");";
		close(QUERY);
		$queryResults = `dbaccess $db insertKeyword.sql`;
		if($queryResults =~ /2/)
		  {
		    open(KEYWORD,$keyword_stg_window_did_not_overlap);
		    print KEYWORD "$geneN\t$xpatN\t$start_stg_name_N\t$end_stg_name_N\t$keyword\t \n";
		    close(KEYWORD);
		    open(KEYWORD,">>$keyword_list_matching_stg_window_denied_list");
		    print KEYWORD "$geneO\t$xpatO\t$start_stg_name_O\t$end_stg_name_O\t \t \n";
		    close(KEYWORD);
		  }
		elsif($queryResults !~ /0/)
		  {
		    print "$geneO - Could not insert - \"$keyword\" with $xpatstgNew[$i]; it already exists\n\n";
		  }
	      }
	    else
	      {
		open(KEYWORD,$non_keyword_list);
		print KEYWORD "$geneN\t$xpatN\t$start_stg_name_N\t$end_stg_name_N\t$keyword\t \n";
		close(KEYWORD);
		
		open(KEYWORD,$keyword_list);
		print KEYWORD "$geneO\t$xpatO\t$start_stg_name_O\t$end_stg_name_O\t \t \n";
		close(KEYWORD);
	      }
	  }
      }
  }
}

$dbh->disconnect;

if($ARGV[2] == 2 || $ARGV[2] == 3){
#compare xpatstg_comments. remove NewComments that match OldComments
for($i=0; $i <= scalar(@commentsOld); $i++)
  {
    unless($commentsOld[$i] eq $commentsNew[$i])
      {
	($gene,$xpat_id,$start_stg_name,$end_stg_name) = split /\|/,$xpatstgNew[$i], 4;
       	$start_stg_zdb = getStgZdbFromStgName($start_stg_name);
	$end_stg_zdb = getStgZdbFromStgName($end_stg_name);
	
	#print "$gene:$start_stg_name-$commentsOld[$i] <=> $commentsNew[$i]\n";

	open(QUERY,">modified_xpat_comment.sql");
	print QUERY "update expression_pattern_stage 
                  set xpatstg_comments = \"$commentsNew[$i]\"
                where xpatstg_xpat_zdb_id = \"$xpat_id\"
                  and xpatstg_start_stg_zdb_id = \"$start_stg_zdb\"
                  and xpatstg_end_stg_zdb_id = \"$end_stg_zdb\";";
	close(QUERY);
	chmod 0700,"modified_xpat_comment.sql";
	$query = `dbaccess $db modified_xpat_comment.sql`;
	
	#output error message if Informix complains
	if($query =~ /error/is)
	  {
	    #print "ERROR: ".$query."\n";
	    open(KEYWORD,">>$rejected_comment_list");
	    print KEYWORD "$gene\t$xpat\t$start_stg_name\t$end_stg_name\t \t$commentsNew[$i]\n";
	    close(KEYWORD);
	    
	    open(KEYWORD,">>$comment_list");
	    print KEYWORD "$gene\t$xpat\t$start_stg_name\t$end_stg_name\t$commentsOld[$i]\t \n";
	    close(KEYWORD);
	  } 
      }
  }
}

exit;


############################################################################


sub numDataRecords
{ 
  local $seperate = ".*?".$_[1];
  local $pattern = $_[1] . $seperate x ($_[2] - 2) . ".*";
  local $recordCount;

  open(COUNT, $_[0]);
  while(<COUNT>)
  {
    $recordCount++ if /$pattern/;
    print $_ if !/$pattern/;
  }
  close(COUNT);  

  $recordCount;
}


sub getStgZdbFromStgName
  {
    open(QUERY,">get_stg_name.sql");
    print QUERY "select stg_zdb_id from stage where stg_name = \"$_[0]\";";
    chmod 0700,"get_stg_name.sql";
    close(QUERY);
    $query =  `dbaccess $db get_stg_name.sql`;
    
    unless($query =~ /No rows/is #lowercase,one line, ot is short for not
	   ||  $query =~ /error/is) #lowercase,one line
      {
	$query =~ /(ZDB-.*?)\s/;
	return $1;
      }
    else
      {
	return "$_[0] is not a stage.";
      }
  }

sub printLineDiff
  {
    $i = 0;
    #get old gene
    open(OLD, $_[0]) or die;
    while(<OLD>)
      {
	s/\t.*//;
	$cbOld[$i] = $_;
	chop($cbOld[$i]);
	#print $cbOld[$i];
	$i++;
      }
    close(OLD);
    
    #compare to new gene
    $i = 0;
    open(NEW, $_[1]) or die;
    while(<NEW>)
      {
	print "$cbOld[$i]"."<=>".$_  unless /$cbOld[$i]/;
	$i++;
      }
    close(NEW);

  die "Unequal number of data records:\n\tOld = $oldRecordCount \n\tNew = $newRecordCount\n";
  }


sub dataFileRecordsMatch
  {
    $match = 1;
    for($i=0;$i<scalar(@xpatstgNew);$i++)
      {
	if($xpatstgNew[$i] ne $xpatstgOld[$i])
	  {
	    print "\nDATA FILE RECORDS do not match at line $i\n\n" if $match;
	    print "\t$xpatstgNew[$i] ne $xpatstgOld[$i]\n";
	    $match = 0;
	  }
      }
    return $match;
  }


sub lock {
  flock(QUERY,2);
}

sub unlock {
  flock(QUERY,8);
  $waiting = 0;
}
