package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.mapping.ChromosomeService;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.SecondaryMarker;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

@Log4j2
public class BasicGeneInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicGeneInfo(int number) {
        numfOfRecords = number;
    }

    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.parseInt(args[0]);
        }
        BasicGeneInfo basicGeneInfo = new BasicGeneInfo(number);
        basicGeneInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllGeneDTO allGeneDTO = getAllGeneInfo();
        ObjectMapper mapper = new ObjectMapper();
/*
        allGeneDTO.getGenes().forEach(geneDTO -> {
            geneDTO.getBasicGeneticEntity().secondaryIds.
        });
*/

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String jsonInString = writer.writeValueAsString(allGeneDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("ZFIN_1.0.1.4_basicGeneInformation.json"))) {
            out.print(jsonInString);
        }
    }

    public AllGeneDTO getAllGeneInfo() {
        List<Marker> allGenes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numfOfRecords);
        log.info("Number of Genes: " + String.format("%,d", allGenes.size()));
        System.out.printf("%,d%n", allGenes.size());

        List<GeneDTO> allGeneDTOList = allGenes.stream()
                .map(
                        gene -> {
                            GeneDTO dto = new GeneDTO();
                            BasicGeneticEntity bgeDto = new BasicGeneticEntity();
                            dto.setName(gene.name);
                            dto.setSymbol(gene.getAbbreviation());
                            bgeDto.setPrimaryId(gene.getZdbID());
                            //dto.setGeneLiteratureUrl("http://zfin.org/action/marker/citation-list/"+gene.getZdbID());
                            if (gene.getSoTerm().getOboID().toString() == "SO:0000704") {
                                dto.setSoTermId("SO:0001217");
                            } else {
                                dto.setSoTermId(gene.getSoTerm().getOboID());
                            }
                            if (CollectionUtils.isNotEmpty(gene.getAliases())) {
                                Set<String> aliasList = new HashSet<>(gene.getAliases().size());
                                for (MarkerAlias alias : gene.getAliases()) {

                                    aliasList.add(alias.getAlias().trim());
                                }
                                bgeDto.setSynonyms(aliasList);
                            }
                            List<String> dblinkPages = new ArrayList<>();
                            Set<CrossReferenceDTO> dbLinkList = new HashSet<>(gene.getDbLinks().size() + 1);
                            if (CollectionUtils.isNotEmpty(gene.getDbLinks())) {

                                for (MarkerDBLink link : gene.getDbLinks()) {
                                    String dbName = DataProvider.getExternalDatabaseName(link.getReferenceDatabase().getForeignDB().getDbName());
                                    if (dbName == null)
                                        continue;
                                    // do not include ENSDARP records
                                    if (dbName.equals(ForeignDB.AvailableName.ENSEMBL.toString()) && link.getAccessionNumber().startsWith("ENSDARP"))
                                        continue;

                                    CrossReferenceDTO xRefDto = new CrossReferenceDTO(dbName, link.getAccessionNumber(), dblinkPages);
                                    dbLinkList.add(xRefDto);
                                }
                            }
                            //TODO: make enum out of the pages attribute, and generate it in a service/method.

                            int hasExpression = getExpressionRepository().getExpressionFigureCountForGene(gene);
                            if (hasExpression > 0) {
                                int hasWTExpression = getExpressionRepository().getWtExpressionFigureCountForGene(gene);
                                if (hasWTExpression > 0) {
                                    List<String> wtXpatPages = new ArrayList<>();
                                    wtXpatPages.add("gene");
                                    wtXpatPages.add("gene/expression");
                                    wtXpatPages.add("gene/wild_type_expression");
                                    wtXpatPages.add("gene/expression_images");
                                    wtXpatPages.add("gene/references");
                                    CrossReferenceDTO wildTypeExpressionCrossReference = new CrossReferenceDTO("ZFIN", gene.getZdbID(), wtXpatPages);
                                    dbLinkList.add(wildTypeExpressionCrossReference);
                                } else {
                                    List<String> xpatPages = new ArrayList<>();
                                    xpatPages.add("gene");
                                    xpatPages.add("gene/expression");
                                    xpatPages.add("gene/expression_images");
                                    xpatPages.add("gene/references");
                                    CrossReferenceDTO expressionCrossReference = new CrossReferenceDTO("ZFIN", gene.getZdbID(), xpatPages);
                                    dbLinkList.add(expressionCrossReference);
                                }
                            } else {
                                List<String> modPages = new ArrayList<>();
                                modPages.add("gene");
                                modPages.add("gene/references");
                                CrossReferenceDTO modRefDto = new CrossReferenceDTO("ZFIN", gene.getZdbID(), modPages);
                                dbLinkList.add(modRefDto);
                            }

                            bgeDto.setCrossReferences(dbLinkList);
                            // get genomic data
                            List<MarkerGenomeLocation> locations = getLinkageRepository().getGenomeLocation(gene);
                            Set<GenomeLocationDTO> locationDTOList = new HashSet<>();
                            ChromosomeService<MarkerGenomeLocation> chromosomeService = new ChromosomeService<>(locations);
                            if (locations != null && chromosomeService.isTrustedValue()) {
                                for (MarkerGenomeLocation loc : locations) {
                                    // ignore records that do not equal the official chromosome number
                                    if (!loc.getChromosome().equals(chromosomeService.getChromosomeNumber()))
                                        continue;
                                    if (loc.getAssembly().equals("GRCz11")) {
                                        if (loc.getSource().toString().equals("ZFIN")) {
                                            GenomeLocationDTO genomeDto = new GenomeLocationDTO(loc.getAssembly(), loc.getChromosome());
                                            if (loc.getStart() != null)
                                                genomeDto.setStartPosition(loc.getStart());
                                            if (loc.getEnd() != null)
                                                genomeDto.setEndPosition(loc.getEnd());
                                            locationDTOList.add(genomeDto);
                                        }
                                    }
                                }
                                bgeDto.setGenomeLocations(locationDTOList);
                            }
                            if (CollectionUtils.isNotEmpty(gene.getSecondaryMarkerSet())) {
                                Set<String> secondaryDTOs = new HashSet<>();
                                for (SecondaryMarker secMarker : gene.getSecondaryMarkerSet()) {
                                    secondaryDTOs.add(secMarker.getOldID());
                                }
                                bgeDto.setSecondaryIds(secondaryDTOs);
                            }
                            dto.setBasicGeneticEntity(bgeDto);
                            return dto;
                        })
                .collect(Collectors.toList());
        AllGeneDTO allGeneDTO = new AllGeneDTO();
        allGeneDTO.setGenes(allGeneDTOList);
        String dataProvider = "ZFIN";
        List<String> pages = new ArrayList<>();
        pages.add("homepage");
        DataProviderDTO dp = new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages));
        MetaDataDTO meta = new MetaDataDTO(new DataProviderDTO("curated", new CrossReferenceDTO(dataProvider, dataProvider, pages)));
        allGeneDTO.setMetaData(meta);
        return allGeneDTO;
    }
}
