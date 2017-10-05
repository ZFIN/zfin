package org.zfin.marker.agr;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.collections.CollectionUtils;
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

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getLinkageRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class BasicGeneInfo extends AbstractScriptWrapper {

    private int numfOfRecords = 0;

    public BasicGeneInfo(int number) {
        numfOfRecords = number;
    }


    public static void main(String[] args) throws IOException {
        int number = 0;
        if (args.length > 0) {
            number = Integer.valueOf(args[0]);
        }
        BasicGeneInfo basicGeneInfo = new BasicGeneInfo(number);
        basicGeneInfo.init();
        System.exit(0);
    }

    private void init() throws IOException {
        initAll();
        AllGeneDTO allGeneDTO = getAllGeneInfo();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
//        mapper.writeValue(new File("basic-gene-info-zfin.json"), allGeneDTO);

//Object to JSON in String
        String jsonInString = writer.writeValueAsString(allGeneDTO);
        try (PrintStream out = new PrintStream(new FileOutputStream("basic-gene-info-zfin.json"))) {
            out.print(jsonInString);
        }
    }

    public AllGeneDTO getAllGeneInfo() {
        List<Marker> allGenes = getMarkerRepository().getMarkerByGroup(Marker.TypeGroup.GENEDOM, numfOfRecords);
        System.out.println(allGenes.size());

        List<GeneDTO> allGeneDTOList = allGenes.parallelStream()
                .map(
                        gene -> {
                            GeneDTO dto = new GeneDTO();
                            dto.setName(gene.name);
                            dto.setSymbol(gene.getAbbreviation());
                            dto.setPrimaryId(gene.getZdbID());
                            dto.setSoTermId(gene.getSoTerm().getOboID());
                            if (CollectionUtils.isNotEmpty(gene.getAliases())) {
                                List<String> aliasList = new ArrayList<>(gene.getAliases().size());
                                for (MarkerAlias alias : gene.getAliases()) {
                                    aliasList.add(alias.getAlias());
                                }
                                dto.setSynonyms(aliasList);
                            }
                            if (CollectionUtils.isNotEmpty(gene.getDbLinks())) {
                                List<String> dbLinkList = new ArrayList<>(gene.getDbLinks().size());
                                for (MarkerDBLink link : gene.getDbLinks()) {
                                    String dbName = DataProvider.getExternalDatabaseName(link.getReferenceDatabase().getForeignDB().getDbName());
                                    if (dbName == null)
                                        continue;
                                    // do not include ENSDARP records
                                    if (dbName.equals(ForeignDB.AvailableName.ENSEMBL.toString()) && link.getAccessionNumber().startsWith("ENSDARP"))
                                        continue;
                                    CrossReferenceDTO xRefDto = new CrossReferenceDTO(dbName, link.getAccessionNumber());
                                    dbLinkList.add(xRefDto.getGlobalID());
                                }
                                dto.setCrossReferenceIds(dbLinkList);
                            }
                            // get genomic data
                            List<MarkerGenomeLocation> locations = getLinkageRepository().getGenomeLocation(gene);
                            Set<GenomeLocationDTO> locationDTOList = new HashSet<>();
                            ChromosomeService<MarkerGenomeLocation> chromosomeService = new ChromosomeService<>(locations);
                            if (locations != null && chromosomeService.isTrustedValue()) {
                                for (MarkerGenomeLocation loc : locations) {
                                    // ignore records that do not equal the official chromosome number
                                    if (!loc.getChromosome().equals(chromosomeService.getChromosomeNumber()))
                                        continue;
                                    GenomeLocationDTO genomeDto = new GenomeLocationDTO(loc.getAssembly(), loc.getChromosome());
                                    if (loc.getStart() != null)
                                        genomeDto.setStartPosition(loc.getStart());
                                    if (loc.getEnd() != null)
                                        genomeDto.setEndPosition(loc.getEnd());
                                    locationDTOList.add(genomeDto);
                                }
                                dto.setGenomeLocations(locationDTOList);
                            }
                            if (CollectionUtils.isNotEmpty(gene.getSecondaryMarkerSet())) {
                                Set<String> secondaryDTOs = new HashSet<>();
                                for (SecondaryMarker secMarker : gene.getSecondaryMarkerSet()) {
                                    secondaryDTOs.add(secMarker.getOldID());
                                }
                                dto.setSecondaryIds(secondaryDTOs);
                            }

                            return dto;
                        })
                .collect(Collectors.toList());
        AllGeneDTO allGeneDTO = new AllGeneDTO();
        allGeneDTO.setGenes(allGeneDTOList);
        MetaDataDTO meta = new MetaDataDTO("ZFIN");
        allGeneDTO.setMetaData(meta);
        return allGeneDTO;
    }
}
