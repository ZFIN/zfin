package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;

public class BasicExpressionInfo extends AbstractScriptWrapper{

        private int numfOfRecords = 0;

        public BasicExpressionInfo(int number) {
            numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicExpressionInfo basicexpressionInfo = new BasicExpressionInfo(number);
        basicexpressionInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllExpressionDTO basicExpressionDTO = getBasicExpressionInfo(numfOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        //Object to JSON in String
        String jsonInString = writer.writeValueAsString(basicExpressionDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.4_1_expression.json"))) {
            out.print(jsonInString);
        }
    }

    public AllExpressionDTO getBasicExpressionInfo(int numberOrRecords) {
        List<BasicExpressionDTO> basicExpressionDTOList = getExpressionRepository().getBasicExpressionDTOObjects();

        AllExpressionDTO allExpressionDTO = new AllExpressionDTO();
        allExpressionDTO.setExpressionList(basicExpressionDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allExpressionDTO.setMetaData(meta);
        return allExpressionDTO;
    }
}
