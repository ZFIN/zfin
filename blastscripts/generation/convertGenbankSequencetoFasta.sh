#!/bin/bash

# gb2fa.pl
#
# open ZF0, ">$prefix"."_zf_oth.fa" or die "Cannot open the file to write: $!.";
#    open ZF1, ">$prefix"."_zf_dna.fa" or die "Cannot open the file to write: $!.";
#    open ZF2, ">$prefix"."_zf_mrna.fa" or die "Cannot open the file to write: $!.";
#    open ZFACC, ">$prefix"."_zf_acc.unl" or die "Cannot open the file to write: $!.";      
#
#    open MS0, ">$prefix"."_ms_oth.fa" or die "Cannot open the file to write: $!.";
#    open MS1, ">$prefix"."_ms_dna.fa" or die "Cannot open the file to write: $!.";
#    open MS2, ">$prefix"."_ms_mrna.fa" or die "Cannot open the file to write: $!." ;
#  
#    open HS0, ">$prefix"."_hs_oth.fa" or die "Cannot open the file to write: $!.";
#    open HS1, ">$prefix"."_hs_dna.fa" or die "Cannot open the file to write: $!.";
#    open HS2, ">$prefix"."_hs_mrna.fa" or die "Cannot open the file to write: $!.";
#
# while (<IN>) {
#        next unless /LOCUS\s+(\w+)\s+(\d+)\sbp\s+(\w+)\s+.+/;
#        $locus = $1;
#        $bp = $2;
#        $type = $3;
#        /DEFINITION\s+(\w[^\^]+)\.\nACCESSION/ or "DEFINITION unmatched for $locus \n";
#        $definition = $1; 
#        $definition =~ s/\n\s+/ /g;
#        /VERSION\s+(\S+)\s+GI:(\d+)/ or "VERSION unmatched for $locus \n";
#        $accession = $1;
#        $gi = $2;
#        /ORGANISM\s+(\w.+)\n/ or "ORGANISM unmatched for $locus \n";
#        $organism = $1;     
#        /ORIGIN[^\n]*\n(.+)$/s or "ORIGIN unmatched for $locus \n";
#        $seq = $1; 
#        $seq =~ tr/tcag//cd;
#        $seq =~ s/(.{60})/$1\n/g;
#
#        if ($organism eq 'Danio rerio'){
#           
#            if ($type eq 'DNA') {
#                print ZF1 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print ZF1 "$seq\n\n";
#                print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-36|\n";
#            }
#            elsif ($type eq 'mRNA') {
#                print ZF2 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print ZF2 "$seq\n\n";
#                print ZFACC substr($accession,0,index($accession, '.'))."|$bp|ZDB-FDBCONT-040412-37|\n";
#            }
#            else {
#                print ZF0 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print ZF0 "$seq\n\n";
#            }
#        }       
#
#
#        if ($organism eq 'Mus musculus'){
#
#            if ($type eq 'DNA') {
#                print MS1 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print MS1 "$seq\n\n";
#            }
#            elsif ($type eq 'mRNA') {
#                print MS2 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print MS2 "$seq\n\n";
#            }
#            else {          
#                print MS0 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print MS0 "$seq\n\n";
#            }           
#        }       
#        
#
#        if ($organism eq 'Homo sapiens'){
#            
#            if ($type eq 'DNA') {
#                print HS1 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print HS1 "$seq\n\n";
#            }
#            elsif ($type eq 'mRNA') {
#                print HS2 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print HS2 "$seq\n\n";
#            }
#            else {
#                print HS0 ">gi|$gi|gb|$accession|$locus $definition \n";
#                print HS0 "$seq\n\n";           
#            }
#        }       
#    }
#
