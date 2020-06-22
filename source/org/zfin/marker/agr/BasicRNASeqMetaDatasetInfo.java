package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.*;

import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.zfin.expression.HTPDataset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

public class BasicRNASeqMetaDatasetInfo extends AbstractScriptWrapper {


    private int numfOfRecords = 0;

    public BasicRNASeqMetaDatasetInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicRNASeqMetaDatasetInfo basicRNASeqMetaDataInfo = new BasicRNASeqMetaDatasetInfo(number);
        basicRNASeqMetaDataInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllRNASeqMetaDatasetDTO allDatasetDTO = getAllDatasetInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        String jsonInString = writer.writeValueAsString(allDatasetDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.1_HTP_Dataset.json"))) {
            out.print(jsonInString);
        }
    }

    public AllRNASeqMetaDatasetDTO getAllDatasetInfo() {

        AllRNASeqMetaDatasetDTO allRNASeqMetaDatasetDTO = new AllRNASeqMetaDatasetDTO();
        return allRNASeqMetaDatasetDTO;
    }
}
