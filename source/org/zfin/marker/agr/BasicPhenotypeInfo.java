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

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class BasicPhenotypeInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicPhenotypeInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicPhenotypeInfo basicPhenotypeInfo = new BasicPhenotypeInfo(number);
        basicPhenotypeInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllPhenotypeDTO basicPhenotypeDTO = getBasicPhenotypeInfo(numfOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        //Object to JSON in String
        String jsonInString = writer.writeValueAsString(basicPhenotypeDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.0.7_phenotype.json"))) {
            out.print(jsonInString);
        }
    }

    public AllPhenotypeDTO getBasicPhenotypeInfo(int numberOrRecords) {
        List<BasicPhenotypeDTO> basicPhenoDTOList = getMutantRepository().getBasicPhenotypeDTOObjects();

        AllPhenotypeDTO allPhenotypeDTO = new AllPhenotypeDTO();
        allPhenotypeDTO.setPhenotypeList(basicPhenoDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        DataProviderDTO dp = new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages));
	MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allPhenotypeDTO.setMetaData(meta);
        return allPhenotypeDTO;
    }
}
