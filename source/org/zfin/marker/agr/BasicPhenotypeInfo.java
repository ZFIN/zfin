package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@Log4j2
public class BasicPhenotypeInfo extends AbstractScriptWrapper {

    private int numOfRecords = 0;

    public BasicPhenotypeInfo(int number) {
        numOfRecords = number;
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
        AllPhenotypeDTO basicPhenotypeDTO = getBasicPhenotypeInfo(numOfRecords);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        //Object to JSON in String
        String jsonInString = writer.writeValueAsString(basicPhenotypeDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_phenotype.json"))) {
            out.print(jsonInString);
        }
    }

    public AllPhenotypeDTO getBasicPhenotypeInfo(int numOfRecords) {
        List<BasicPhenotypeDTO> basicPhenoDTOList = getMutantRepository().getBasicPhenotypeDTOObjects();
        log.info("Number of Phenotype annotations: " + String.format("%,d", basicPhenoDTOList.size()));
        System.out.printf("%,d%n", basicPhenoDTOList.size());

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
