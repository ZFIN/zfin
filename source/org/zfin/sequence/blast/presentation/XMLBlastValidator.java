package org.zfin.sequence.blast.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Validates XMLBlastBeans on submit
 */
@Component
public class XMLBlastValidator implements Validator {

    private static Logger logger = LogManager.getLogger(XMLBlastValidator.class);

    private int querySequenceLimit = 50000;

    public boolean supports(Class aClass) {
        return (XMLBlastBean.class.equals(aClass));
    }

    private boolean isProteinDataLibrary(String dataLibraryString) {
        return (true == dataLibraryString.contains("rotein") // internal ZFIN new-style
                ||
                true == dataLibraryString.contains("_aa") // internal ZFIN old-style
                ||
                true == dataLibraryString.contains("sptr_") // UniProt designation
        );
    }

    private boolean isNucleotideDataLibrary(String dataLibraryString) {
        return (false == dataLibraryString.contains("rotein") // internal ZFIN new-style
                &&
                false == dataLibraryString.contains("_aa") // internal ZFIN old-style
                &&
                false == dataLibraryString.contains("sptr_") // UniProtDesignation
        );

    }

    public void validate(Object o, Errors errors) {
        XMLBlastBean xmlBlastBean = (XMLBlastBean) o;

        if (errors.hasErrors()) {
            return;
        }


        if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.SEQUENCE_ID.toString())) {
            validateFASTASequence(xmlBlastBean, errors, "sequenceID");
        } else if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.UPLOAD.toString())) {
            validateFASTASequence(xmlBlastBean, errors, "sequenceFile");
        } else if (xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.FASTA.toString())) {
            validateFASTASequence(xmlBlastBean, errors, "querySequence");
        }


        if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTN.getValue())
                ||
                xmlBlastBean.getProgram().equals(XMLBlastBean.Program.TBLASTX.getValue())
                ) {
            if (false == xmlBlastBean.getSequenceType().equals(XMLBlastBean.SequenceType.NUCLEOTIDE.getValue())) {
                errors.rejectValue("sequenceType", "code", "Sequence type must be nucleotide.");
            }

            if (isProteinDataLibrary(xmlBlastBean.getDataLibraryString())) {
                errors.rejectValue("dataLibraryString", "code", "Database must be nucleotide.");
            }
        } else if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTP.getValue())) {
            if (false == xmlBlastBean.getSequenceType().equals(XMLBlastBean.SequenceType.PROTEIN.getValue())) {
                errors.rejectValue("sequenceType", "code", "Sequence type must be protein.");
            }

            if (isNucleotideDataLibrary(xmlBlastBean.getDataLibraryString())) {
                errors.rejectValue("dataLibraryString", "code", "Database must be protein.");
            }
        } else
            // trans-nucle - protein
            if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTX.getValue())) {
                if (false == xmlBlastBean.getSequenceType().equals(XMLBlastBean.SequenceType.NUCLEOTIDE.getValue())) {
                    errors.rejectValue("sequenceType", "code", "Sequence type must be nucleotide.");
                }
                if (isNucleotideDataLibrary(xmlBlastBean.getDataLibraryString())) {
                    errors.rejectValue("dataLibraryString", "code", "Database must be protein.");
                }
            } else
                // protein , trans-nucl
                if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.TBLASTN.getValue())) {
                    if (false == xmlBlastBean.getSequenceType().equals(XMLBlastBean.SequenceType.PROTEIN.getValue())) {
                        errors.rejectValue("sequenceType", "code", "Sequence type must be protein.");
                    }
                    if (isProteinDataLibrary(xmlBlastBean.getDataLibraryString())) {
                        errors.rejectValue("dataLibraryString", "code", "Database must be nucleotide.");
                    }
                }


        // clear if not the one originally chosen
        if (false == xmlBlastBean.getQueryType().equals(XMLBlastBean.QueryTypes.FASTA.toString())) {
            if (errors.hasErrors()) {
                xmlBlastBean.setQuerySequence("");
            }
        }
    }

    public void validateFASTASequence(XMLBlastBean xmlBlastBean, Errors errors, String field) {
        try {
            xmlBlastBean.setQuerySequence(XMLBlastController.prependSequenceWithDefline(xmlBlastBean.getQuerySequence()));
            BufferedReader bufferedReader = new BufferedReader(new StringReader(xmlBlastBean.getQuerySequence()));
            SymbolTokenization symbolTokenization;
            if (xmlBlastBean.getSequenceType() == null ||
                    XMLBlastBean.SequenceType.NUCLEOTIDE == XMLBlastBean.SequenceType.getSequenceType(xmlBlastBean.getSequenceType().toString())) {
                symbolTokenization = RichSequence.IOTools.getNucleotideParser();
            } else {
                symbolTokenization = RichSequence.IOTools.getProteinParser();
            }

            RichSequenceIterator iterator = RichSequence.IOTools.readFasta(bufferedReader, symbolTokenization, new SimpleNamespace("fasta-in"));
            if (iterator.hasNext() == true) {
                while (iterator.hasNext()) {
                    RichSequence richSequence = iterator.nextRichSequence();
                    String sequence = richSequence.getInternalSymbolList().seqString();
                    if (StringUtils.isEmpty(sequence)
                            &&
                            false == errors.hasErrors()
                            ) {
                        errors.rejectValue(field, "code", "Problem parsing sequence for accession: " + richSequence.getAccession());
                    } else {
                        if (sequence.length() > querySequenceLimit) {
                            errors.rejectValue(field, "code", "Sequence must not exceed " + querySequenceLimit + " characters.");
                        }
                    }
                }
            } else {
                errors.rejectValue(field, "code", "No sequences read.");
            }
        } catch (Exception e) {
            String sequenceType;
            if (xmlBlastBean.getSequenceType().equals("nt")) {
                sequenceType = "Nucleotide";
            } else {
                sequenceType = "Protein";
            }
            String errorString = "Sequence type does not match program " + xmlBlastBean.getProgram() + ".  " + sequenceType + " sequence expected.";
            errors.rejectValue(field, "code", errorString);
            logger.debug("Sequence type does not match program " + xmlBlastBean.getProgram() + ".  " + sequenceType + " sequence expected.");
            logger.debug("FASTA does not match for sequence type [" + xmlBlastBean.getSequenceType() + "[is invalid:\n" + xmlBlastBean.getQuerySequence() + "\n", e);
        }
    }

    public int getQuerySequenceLimit() {
        return querySequenceLimit;
    }

    public void setQuerySequenceLimit(int querySequenceLimit) {
        this.querySequenceLimit = querySequenceLimit;
    }
}
