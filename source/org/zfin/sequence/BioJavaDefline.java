package org.zfin.sequence;

import org.biojavax.bio.seq.RichSequence;

/**
 * Description:
 * the blast database it is found in is important <limitable?>
 *  a unique  identifier <accession> is important  <searchable>
 *  the zfin object the sequence is associated with  <searchable> is good
 *  the origin of the sequence would be *awesome* <attribution>
 *  the <chromosome> if possible is nice
 *  the sequence length is good
 *
 * Proposal: <accession*> <data id*> <blastdb> <other data info [attribution chromosome]> <length>
 * Example:  <ZFINPROT000000123*>  <ZDB-TSCRIPT-12345-1*> <unpublishedRNA> <ddx56-001 mRNA non-coding ZDB-PUB-1234-1 lg3> <154 bp>
 * Note: * is searchable.
 */
public class BioJavaDefline implements Defline {

    private RichSequence richSequence;

    public BioJavaDefline(RichSequence richSequence){
        this.richSequence = richSequence ;
    }

    @Override
    public String toString() {
        return ">"+richSequence.getAccession()+  " " + richSequence.getDescription() ;
    }

    public String getAccession() {
        return richSequence.getAccession();
    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString()) ;
    }

}