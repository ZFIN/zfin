package org.zfin.orthology.presentation;

import org.zfin.framework.presentation.SearchFormValidator;
import org.zfin.orthology.CriteriaType;
import org.zfin.orthology.Species;
import org.zfin.util.ErrorCollection;

/**
 * User: giles
 * Date: Aug 17, 2006
 * Time: 12:15:16 PM
 */

/**
 * Used to validate the input on a particular field of the orthology form.  One validator object
 * must be created for each field of the form.  The parameters are given in the constructor, and
 * then a reference to the object can be kept to check validity any time in the future.
 */
public class OrthologyFormValidator implements SearchFormValidator {
    private boolean isValid;
    private ErrorCollection errors;
    public static final String LIST_PATTERN = "([1-9]\\d*)(,[1-9]\\d*,)*";

    /**
     * Constructor which takes all the parameters needed to decide whether a particular text field's
     * input is valid.
     *
     * @param species      The name of the species (Zebrafish, Human, Mouse, etc)
     * @param criteriaType The purpose of the text field to be checked.  Can be "symbol", "chromosome", or "position".
     * @param filterType   The text in the pulldown menu to the left of the field
     * @param criteria     The input to be validated
     */
    public OrthologyFormValidator(String species, String criteriaType, String filterType, String criteria) {
        isValid = true;
        errors = new ErrorCollection();

        if (species.equals(Species.ZEBRAFISH.toString())) {
            validateZebrafish(criteriaType, filterType, criteria);
        } else if (species.equals(Species.HUMAN.toString())) {
            validateHuman(criteriaType, filterType, criteria);
        } else if (species.equals(Species.MOUSE.toString())) {
            validateMouse(criteriaType, filterType, criteria);
        } else if (species.equals(Species.FLY.toString())) {
            validateFly(criteriaType, filterType, criteria);
        } else {
            errors.addError("The species '" + species + "' is not supported by this search.");
            isValid = false;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public ErrorCollection getErrors() {
        return errors;
    }

    /**
     * Uses regular expressions to validate input for zebrafish.  Adds errors to the error collection as they are discovered.
     *
     * @param criteriaType
     * @param filterType
     * @param criteria
     */
    private void validateZebrafish(String criteriaType, String filterType, String criteria) {
        Species species = Species.ZEBRAFISH;
        if (criteriaType.equals(CriteriaType.GENE_SYMBOL.getName())) {
            validateSymbol(Species.ZEBRAFISH, filterType, criteria);
        } else if (criteriaType.equals(CriteriaType.CHROMOSOME.getName())) {
            if (filterType.equals("equals")) {
                validateSingleChromosome(criteria, species, 25, null);
            } else if (filterType.equals("list")) {
                if (criteria.matches("([1-9]\\d*,)([1-9]\\d*,)*[1-9]\\d*")) {
                    //
                } else {
                    errors.addError("The chromosome field for Zebrafish must contain a comma-delineated list of integers.");
                    isValid = false;
                }
            } else if (filterType.equals("range")) {
                if (criteria.matches("([1-9]\\d*)\\-([1-9]\\d*)")) {
                    String[] tokens = criteria.split("-");
                    int min = Integer.parseInt(tokens[0]);
                    int max = Integer.parseInt(tokens[1]);
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The chromosome field for Zebrafish must contain a range of integers in the format min-max.");
                        isValid = false;
                    }
                } else {
                    errors.addError("The chromosome field for Zebrafish must contain a range of integers in the format min-max.");
                    isValid = false;
                }
            } else {
                errors.addError("The filter type '" + filterType + "' is not supported by the chromosome field for Zebrafish in this search.");
                isValid = false;
            }

        } else if (criteriaType.equals("position")) {
            errors.addError("The criteria type 'position' is not supported for Zebrafish");
            isValid = false;
        } else {
            errors.addError("The criteria type '" + criteriaType + "' is not supported by this search.");
            isValid = false;
        }
    }

    private void validateSingleChromosome(String chromosome, Species species, int maxChromosome, String[] sexChromomsomes) {
        if (chromosome.length() == 0)
            return;

        if (sexChromomsomes != null) {
            String expression = createRegExpressionPattern(sexChromomsomes);
            if (chromosome.matches("[a-zA-Z]")) {
                if (!chromosome.toUpperCase().matches(expression)) {
                    errors.addError("The chromosome field for " + species.toString() + " must be " + expression);
                    isValid = false;
                }
            } else {
                if (!chromosome.matches("\\-*\\d*")) {
                    errors.addError("The chromosome field for " + species.toString() + " must be an integer.");
                    isValid = false;
                } else {
                    validateChromosomeNumber(chromosome, species, maxChromosome);
                }
            }
        } else {
            if (!chromosome.matches("\\-*\\d*")) {
                errors.addError("The chromosome field for " + species.toString() + " must be an integer.");
                isValid = false;
            } else {
                validateChromosomeNumber(chromosome, species, maxChromosome);
            }
        }
    }

    private String createRegExpressionPattern(String[] sexChromomsomes) {
        StringBuilder regExp = new StringBuilder("[");
        for (String letter : sexChromomsomes) {
            regExp.append(letter);
            regExp.append(",");
        }
        regExp.deleteCharAt(regExp.length() - 1);
        regExp.append("]");
        return regExp.toString();
    }

    private void validateChromosomeNumber(String chromosome, Species species, int maxChromosome) {
        int chromoNb = Integer.parseInt(chromosome);
        if (chromoNb < 1) {
            errors.addError("The chromosome number for " + species.toString() + " must be a positive integer.");
            isValid = false;
        }
        if (chromoNb > maxChromosome) {
            errors.addError("The chromosome number for " + species.toString() + " must be smaller than " + (maxChromosome + 1) + ".");
            isValid = false;
        }
    }

    private void validateSymbol(Species species, String filterType, String criteria) {
        if (criteria == null)
            criteria = "";
        if (filterType.equals("equals") || filterType.equals("contains") ||
                filterType.equals("begins") || filterType.equals("ends")) {
            if (criteria.matches("\\w*")) {
                //
            } else {
                errors.addError("The symbol field for " + species.toString() +
                        " must consist only of alphanumeric characters (A-Z),(0-9).");
                isValid = false;
            }
        } else {
            errors.addError("The filter type '" + filterType + "' is not supported by the symbol field for " +
                    species.toString() + " in this search.");
            isValid = false;
        }
    }

    /**
     * Uses regular expressions to validate input for human.  Adds errors to the error collection as they are discovered.
     *
     * @param criteriaType
     * @param filterType
     * @param criteria
     */
    private void validateHuman(String criteriaType, String filterType, String criteria) {
        if (criteria == null)
            criteria = "";
        Species species = Species.HUMAN;
        int maxChromosome = 23;
        if (criteriaType.equals(CriteriaType.GENE_SYMBOL.getName())) {
            validateSymbol(species, filterType, criteria);
        } else if (criteriaType.equals(CriteriaType.CHROMOSOME.getName())) {
            if (filterType.equals("equals")) {
                String[] sexChromomsomes = {"X", "Y"};
                validateSingleChromosome(criteria, species, maxChromosome, sexChromomsomes);
            } else if (filterType.equals("list")) {
                if (criteria.matches(LIST_PATTERN)) {
                    //
                } else {
                    errors.addError("The chromosome field for Human must contain a comma-delineated list of integers.");
                    isValid = false;
                }
            } else if (filterType.equals("range")) {
                if (criteria.matches("([1-9]\\d*)\\-([1-9]\\d*)")) {
                    String[] tokens = criteria.split("-");
                    int min = Integer.parseInt(tokens[0]);
                    int max = Integer.parseInt(tokens[1]);
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The chromosome field for Human must contain a range of integers in the format min-max.");
                        isValid = false;
                    }
                } else {
                    errors.addError("The chromosome field for Human must contain a range of integers in the format min-max.");
                    isValid = false;
                }
            } else {
                errors.addError("The filter type '" + filterType + "' is not supported by the chromosome field for Human in this search.");
                isValid = false;
            }

        } else if (criteriaType.equals("position")) {
            if (filterType.equals("equals") || filterType.equals("begins")) {
                if (criteria.matches("[qp][1-9]\\d*\\.*\\d*") && !criteria.contains("..")) {
                    //
                } else {
                    errors.addError("The position field for Human must be a number and must begin with either p or q.");
                }
            } else if (filterType.equals("range")) {
                if ((criteria.matches("(p[1-9]\\d*\\.*\\d*)\\-(p[1-9]\\d*\\.*\\d*)") || criteria.matches("(q[1-9]\\d*\\.*\\d*)\\-(q[1-9]\\d*\\.*\\d*)")) && !criteria.contains("..")) {
                    String[] tokens = criteria.split("-");
                    double min = Double.parseDouble(tokens[0].substring(1)); // (chop off first character, then parse)
                    double max = Double.parseDouble(tokens[1].substring(1));
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The position field for Human must contain a range of numbers in the format p<min>-p<max> or q<min>-q<max> (excluding the < > brackets).");
                        isValid = false;
                    }
                } else {
                    errors.addError("The position field for Human must contain a range of numbers in the format p<min>-p<max> or q<min>-q<max> (excluding the < > brackets).");
                    isValid = false;
                }
            }
        } else {
            errors.addError("The criteria type '" + criteriaType + "' is not supported by this search.");
            isValid = false;
        }

    }

    /**
     * Uses regular expressions to validate input for mouse.  Adds errors to the error collection as they are discovered.
     *
     * @param criteriaType
     * @param filterType
     * @param criteria
     */
    private void validateMouse(String criteriaType, String filterType, String criteria) {
        if (criteria == null)
            criteria = "";
        Species species = Species.MOUSE;
        int maxChromosome = 20;
        if (criteriaType.equals(CriteriaType.GENE_SYMBOL.getName())) {
            validateSymbol(species, filterType, criteria);
        } else if (criteriaType.equals(CriteriaType.CHROMOSOME.getName())) {
            if (filterType.equals("equals")) {
                String[] sexChromomsomes = {"X", "Y"};
                validateSingleChromosome(criteria, species, maxChromosome, sexChromomsomes);
            } else if (filterType.equals("list")) {
                if (criteria.matches(LIST_PATTERN)) {
                    //
                } else {
                    errors.addError("The chromosome field for Mouse must contain a comma-delineated list of integers.");
                    isValid = false;
                }
            } else if (filterType.equals("range")) {
                if (criteria.matches("([1-9]\\d*)\\-([1-9]\\d*)")) {
                    String[] tokens = criteria.split("-");
                    int min = Integer.parseInt(tokens[0]);
                    int max = Integer.parseInt(tokens[1]);
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The chromosome field for Mouse must contain a range of integers in the format min-max.");
                        isValid = false;
                    }
                } else {
                    errors.addError("The chromosome field for Mouse must contain a range of integers in the format min-max.");
                    isValid = false;
                }
            } else {
                errors.addError("The filter type '" + filterType + "' is not supported by the chromosome field for Mouse in this search.");
                isValid = false;
            }

        } else if (criteriaType.equals("position")) {
            if (filterType.equals("equals") || filterType.equals("begins")) {
                if (criteria.matches("[1-9]\\d*\\.*\\d*") && !criteria.contains("..")) {
                    //
                } else {
                    errors.addError("The position field for Mouse must be a number.");
                }
            } else if (filterType.equals("range")) {
                if (criteria.matches("([1-9]\\d*\\.*\\d*)\\-([1-9]\\d*\\.*\\d*)") && !criteria.contains("..")) {
                    String[] tokens = criteria.split("-");
                    double min = Double.parseDouble(tokens[0]);
                    double max = Double.parseDouble(tokens[1]);
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The position field for Mouse must contain a range of numbers in the format min-max.");
                        isValid = false;
                    }
                } else {
                    errors.addError("The position field for Mouse must contain a range of numbers in the format min-max.");
                    isValid = false;
                }
            }
        } else {
            errors.addError("The criteria type '" + criteriaType + "' is not supported by this search.");
            isValid = false;
        }

    }

    /**
     * Uses regular expressions to validate input for fly.  Adds errors to the error collection as they are discovered.
     *
     * @param criteriaType
     * @param filterType
     * @param criteria
     */
    private void validateFly(String criteriaType, String filterType, String criteria) {
        if (criteria == null)
            criteria = "";
        Species species = Species.FLY;
        int maxChromosome = 4;
        if (criteriaType.equals(CriteriaType.GENE_SYMBOL.getName())) {
            validateSymbol(species, filterType, criteria);
        } else if (criteriaType.equals(CriteriaType.CHROMOSOME.getName())) {
            if (filterType.equals("equals")) {
                String[] sexChromomsomes = {"X", "Y"};
                validateSingleChromosome(criteria, species, maxChromosome, sexChromomsomes);
            } else if (filterType.equals("list")) {
                if (criteria.matches("([1-9]\\d*[RL],)([1-9]\\d*[RL],)*([1-9]\\d*[RL])")) {
                    //
                } else {
                    errors.addError("The chromosome field for Fly must contain a comma-delineated list of chromosomes in the format 2R|L, 3R|L, X or 4.");
                    isValid = false;
                }
            } else if (filterType.equals("range")) {
                if (criteria.matches("([1-9]\\d*R)\\-([1-9]\\d*R)") || criteria.matches("([1-9]\\d*L)\\-([1-9]\\d*L)")) {
                    String[] tokens = criteria.split("-");
                    int min = Integer.parseInt(tokens[0].substring(0, tokens[0].length() - 1));
                    int max = Integer.parseInt(tokens[1].substring(0, tokens[1].length() - 1));
                    if (min <= max) {
                        //
                    } else {
                        errors.addError("The chromosome field for Fly must contain a range of integers in the format <min>R-<max>R or <min>L-<max>L (excluding the  < > brackets).");
                        isValid = false;
                    }
                } else {
                    errors.addError("The chromosome field for Fly must contain a range of integers in the format <min>R-<max>R or <min>L-<max>L (excluding the  < > brackets).");
                    isValid = false;
                }
            } else {
                errors.addError("The filter type '" + filterType + "' is not supported by the chromosome field for Fly in this search.");
                isValid = false;
            }

        } else if (criteriaType.equals("position")) {
            errors.addError("The criteria type 'position' is not supported for Fly.");
            isValid = false;
        } else {
            errors.addError("The criteria type '" + criteriaType + "' is not supported by this search.");
            isValid = false;
        }

    }

}
