#!/private/bin/perl

use Net::FTP;


###MAIN####
my $ftpFile;

&getRemoteFileTimestamp();

#########
sub getRemoteFileTimestamp () {
    my $info_file = "@SCRIPT_PATH@/Ensembl/ensembl.ftp";
    my $probnext = 1;

    open INFOFILE, "$info_file" or die "getRemoteFileTimestamp: cannot open $info_file to read: $! \n";
    my $info_line = <INFOFILE>;
    my @info = split(/\|/, $info_line);
    
    my $ftp_url = $info[0];

    my $ftp_path = $info[1];

    my $ftp_file = $info[2];

    close INFOFILE;

    # in case of probing next version, the file name need to be updated.
    if ($probnext) {
	       	foreach my $field ( split (/\.|_/, $ftp_file) ) {
			if ( $field =~ /^\d+$/ ) {
				my $length_version = length($field);		
				my $nextversion = $field + 1;
				while (length($nextversion) < $length_version) {
					$nextversion = "0".$nextversion;
				}
				$ftp_file =~ s/$field/$nextversion/;
                                last;
			}
		}
    }
    my $user_name = "anonymous";
    my $passwd = "someone@";    
    my $ftp = new Net::FTP($ftp_url);
    
    die "Failed to connect to server '$ftp_url':$!\n" unless $ftp;
    die "Failed to login as $user_name\n" unless $ftp->login($user_name, $passwd);
    die "Failed to change directory to $ftp_path\n" unless $ftp->cwd($ftp_path);
    warn "Failed to set binary mode\n" unless $ftp->binary();
    
    #Ensembl ftp site hidden the file at
    # danio_rerio_47_7a/cdna/Danio_rerio.ZFISH7.47.cdna.all.fa.gz
    # where both the directory path and file name are dynamic

    if($ftp_url =~ /ensembl/){
	foreach ($ftp->dir()) {
	        
	    if ( /(danio_rerio)/ ) {
		die "Failed to change directory \n" unless $ftp->cwd("$1/cdna");
		foreach ($ftp->dir()) {
		    if ( /(Danio_rerio.+cdna\.all\.fa\.gz)/ ) {
			$ftp_file = $1;
			$ftpFile = $1;  #set ftpFile with is a local value from caller
			my $ftpFullPath;
			$ftpFullPath = $ftp->pwd();
			$ftpFile = $ftpFullPath."/".$ftpFile;
			last;
		    }
		}
		last;
	    }
	}
    }

    my $r_mdtm = $ftp->mdtm($ftp_file);
    print $ftpFile;
    return ($ftpFile);
}
