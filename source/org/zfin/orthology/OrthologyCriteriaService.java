package org.zfin.orthology;

import org.apache.commons.lang.StringUtils;
import org.zfin.orthology.presentation.SpeciesCriteriaBean;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: giles
 * Date: Aug 3, 2006
 * Time: 4:49:34 PM
 */

/**
 * Service class that takes speciesCriteriaBean objects from form bean and returns matching SpeciesCriteria objects
 * to be sent to the repository by the controller
 */
public class OrthologyCriteriaService {

    public static List<SpeciesCriteria> getSpeciesCriteria(List<SpeciesCriteriaBean> criteriaBeans) {
        List<SpeciesCriteria> criteria = new ArrayList<SpeciesCriteria>();

        for (SpeciesCriteriaBean currentSpecies : criteriaBeans) {
            SpeciesCriteria newCriteria = new SpeciesCriteria();

            newCriteria.setName(currentSpecies.getName());

            GeneSymbolCriteria symbolCriteria = getSymbolCriteria(currentSpecies);
            newCriteria.setSymbol(symbolCriteria);

            ChromosomeCriteria chromCriteria = getChromosomeCriteria(currentSpecies);
            newCriteria.setChromosome(chromCriteria);

            PositionCriteria posCriteria = getPositionCriteria(currentSpecies);
            newCriteria.setPosition(posCriteria);

            criteria.add(newCriteria);
        }
        return criteria;
    }

    private static GeneSymbolCriteria getSymbolCriteria(SpeciesCriteriaBean currentSpecies) {
        if (!StringUtils.isEmpty(currentSpecies.getGeneSearchTerm())) {
            GeneSymbolCriteria symbolCriteria = new GeneSymbolCriteria();
            String geneSymbolFilterType = currentSpecies.getGeneSymbolFilterType();
            symbolCriteria.setType(FilterType.getFilterType(geneSymbolFilterType));

            symbolCriteria.setSymbol(currentSpecies.getGeneSearchTerm());
            return symbolCriteria;
        } else {
            return null;
        }
    }

    private static ChromosomeCriteria getChromosomeCriteria(SpeciesCriteriaBean currentSpecies) {
        if (!StringUtils.isEmpty(currentSpecies.getChromosome())) {
            ChromosomeCriteria chromCriteria = new ChromosomeCriteria();
            List<String> chromosomes = new ArrayList<String>();

            String chromFilterType = currentSpecies.getChromosomeFilterType();
            chromCriteria.setType(FilterType.getFilterType(chromFilterType));

            if (chromCriteria.getType().getName().equals("equals")) {
                chromosomes.add(currentSpecies.getChromosome().trim());
                chromCriteria.setChromosomesNames(chromosomes);
            } else if (chromCriteria.getType().getName().equals("list")) {
                List<String> tokens = Arrays.asList(currentSpecies.getChromosome().trim().split(","));
                for (String currentToken : tokens) {
                    chromosomes.add(currentToken);
                }
                chromCriteria.setChromosomesNames(chromosomes);
            } else if (chromCriteria.getType().getName().equals("range")) {
                String[] tokens = currentSpecies.getChromosome().trim().split("-");
                if (tokens.length == 2) {
                    int min = Integer.parseInt(tokens[0]);
                    int max = Integer.parseInt(tokens[1]);
                    if (min < max) {
                        chromCriteria.setMin(min);
                        chromCriteria.setMax(max);
                    } else {
                        chromCriteria.setMin(max);
                        chromCriteria.setMax(min);
                    }
                }
            }
            return chromCriteria;
        } else {
            return null;
        }
    }

    private static PositionCriteria getPositionCriteria(SpeciesCriteriaBean currentSpecies) {

        if (!StringUtils.isEmpty(currentSpecies.getPosition())) {
            PositionCriteria posCriteria = new PositionCriteria();

            String posFilterType = currentSpecies.getPositionFilterType();
            posCriteria.setType(FilterType.getFilterType(posFilterType));

            if (posCriteria.getType().getName().equals("equals") || posCriteria.getType().getName().equals("begins")) {
                double doubPosition = Double.parseDouble(currentSpecies.getPosition().trim());
                posCriteria.setPosition(doubPosition);
            } else if (posCriteria.getType().getName().equals("range")) {
                String[] tokens = currentSpecies.getPosition().trim().split("-");
                if (tokens.length == 2) {
                    double min = Double.parseDouble(tokens[0]);
                    double max = Double.parseDouble(tokens[1]);
                    if (min < max) {
                        posCriteria.setMin(min);
                        posCriteria.setMax(max);
                    } else {
                        posCriteria.setMin(max);
                        posCriteria.setMax(min);
                    }
                }
            }
            return posCriteria;
        } else {
            return null;
        }
    }
}
