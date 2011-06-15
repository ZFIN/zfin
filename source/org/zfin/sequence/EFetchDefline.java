package org.zfin.sequence;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;

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
public class EFetchDefline implements Defline {

    private EFetchSequenceServiceStub.GBSeq_type0 gb_Seq ;

    public EFetchDefline(EFetchSequenceServiceStub.GBSeq_type0 gb_Seq){
        this.gb_Seq = gb_Seq ;
    }

    @Override
    public String toString() {
        StringBuilder sequenceStringBuilder = new StringBuilder() ;
        sequenceStringBuilder.append(">").append("ncbi").append("|") ;
        sequenceStringBuilder.append(gb_Seq.getGBSeq_accessionVersion()).append(" ") ;
        sequenceStringBuilder.append(gb_Seq.getGBSeq_definition()).append("\n");
        return sequenceStringBuilder.toString();
    }

    public String getAccession() {
        return gb_Seq.getGBSeq_primaryAccession() ;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        return this.toString().equals(o.toString()) ;
    }
}