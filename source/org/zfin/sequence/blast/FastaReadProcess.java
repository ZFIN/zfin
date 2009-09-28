package org.zfin.sequence.blast;

import org.zfin.framework.exec.ExecProcess;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.BioJavaDefline;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.SimpleNamespace;
import org.biojava.bio.seq.io.SymbolTokenization;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class runs system exec calls robustly.
 */
public class FastaReadProcess extends ExecProcess{

    private DBLink dbLink ;
    private List<Sequence> sequences = new ArrayList<Sequence>() ;


    public FastaReadProcess(List<String> commandList, DBLink dbLink){
        super(commandList) ;
        this.dbLink = dbLink ;
    }

    public List<Sequence> getSequences(){

        try {
            sequences.clear();

            StringReader stringReader =new StringReader(getStandardOutput()) ;
            BufferedReader br = new BufferedReader(stringReader);
            RichSequenceIterator iterator ;
            SymbolTokenization symbolTokenization ;
            if(dbLink.getReferenceDatabase().getPrimaryBlastDatabase().getType()==Database.Type.NUCLEOTIDE){
                symbolTokenization = RichSequence.IOTools.getNucleotideParser();
            }
            else{
                symbolTokenization = RichSequence.IOTools.getProteinParser();
            }
            iterator = RichSequence.IOTools.readFasta(br,  symbolTokenization, new SimpleNamespace("fasta-in") ) ;
            while(iterator.hasNext()){
                RichSequence richSequence =iterator.nextRichSequence() ;
                Sequence sequence = new Sequence() ;
                sequence.setDbLink(dbLink);
                sequence.setData(richSequence.getInternalSymbolList().seqString().toUpperCase());
                sequence.setDefLine(new BioJavaDefline(richSequence));
                sequences.add(sequence) ;
            }
        } catch (Exception ex) {
            System.out.println ("Problem reading stream" + ex);
            ex.printStackTrace ();
        }
        return sequences ;
    }

}