package org.zfin.sequence;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    private Element seqEl;

    public EFetchDefline(Element gbSequenceElement){
        this.seqEl = gbSequenceElement;
    }

    @Override
    public String toString() {
        return ">ncbi|" + getElementText("GBSeq_primary-accession") + " " + getElementText("GBSeq_definition") + "\n";
    }

    public String getAccession() {
        return getElementText("GBSeq_primary-accession");
    }

    @Override
    public boolean equals(Object o) {
        return o != null && this.toString().equals(o.toString());
    }

    private String getElementText(String elementName) {
        NodeList nodes = seqEl.getElementsByTagName(elementName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }
}