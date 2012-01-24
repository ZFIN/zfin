package org.zfin.util;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ComplexityScoringSystemTest {

    private static Logger logger = Logger.getLogger(ComplexityScoringSystemTest.class);
    //private static int[] scores = {1, 2, 4, 8, 13, 21, 31, 45, 66, 81, 97, 123, 148};
    //private static int[] scores = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34, 37};
    // score value of first element
    private int[] scores = {1};
    private int numberOfElements;
    private static int maxValue = 10000;
    private String message;

    public static void main(String[] arguments) {
        ComplexityScoringSystemTest test = new ComplexityScoringSystemTest(13);
        test.generateImprovedScoreSchema();
    }

    public ComplexityScoringSystemTest(int numberOfValues) {
        this.numberOfElements = numberOfValues;
        if (numberOfValues < 1)
            throw new RuntimeException("Need to have an integer number greater than one.");
    }

    // calculate all combinations for a given initial score matrix
    // that is known to be a valid matrix.
    // then choose the lowest number above the given maximum number that still
    // fulfills the requirements.
    private void generateImprovedScoreSchema() {
        System.out.println("Start calculating scoring matrix");

        boolean iterate = true;
        while (iterate) {
            if (scores.length < numberOfElements) {
                populateCombinedScoreMap();
                int nextScoreMatrixElement = getNextScoreMatrixElement();
                initializeNewMatrix(nextScoreMatrixElement);
                continue;
            }
            iterate = false;
        }
        message = null;
        checkDoubleCombinations(scores);
        checkTripleCombinations(scores);
        printMessage();
        printScoringMatrix();
        printAllCombinedValues(scores);
        printAllDoubleValues();
        printAllTripleValues();
    }

    private void populateCombinedScoreMap() {
        addAllScores(combinedScore);
        addAllDoubleCombinations();
        addAllTripleCombinations();
    }

    private void initializeNewMatrix(int nextScoreMatrixElement) {
        int[] oldScores = scores;
        scores = new int[scores.length + 1];
        int index = 0;
        for (int score : oldScores)
            scores[index++] = score;
        scores[index] = nextScoreMatrixElement;
        combinedScore = new HashMap<String, Integer>();
    }


    private static Map<String, Integer> combinedScore = new HashMap<String, Integer>();

    private void addAllTripleCombinations() {
        for (int int1 = 0; int1 < scores.length; int1++) {
            for (int int2 = 0; int2 < scores.length; int2++) {
                if (int2 < int1)
                    continue;
                for (int int3 = 0; int3 < scores.length; int3++) {
                    if (int3 < int2)
                        continue;
                    int totalScore = scores[int1] + scores[int2] + scores[int3];
                    combinedScore.put(scores[int1] + "," + scores[int2] + "," + scores[int3], totalScore);
                }
            }
        }
    }

    private void addAllDoubleCombinations() {
        for (int int1 = 0; int1 < scores.length; int1++) {
            for (int int2 = 0; int2 < scores.length; int2++) {
                if (int2 < int1)
                    continue;
                int totalScore = scores[int1] + scores[int2];
                combinedScore.put(scores[int1] + "," + scores[int2], totalScore);
            }
        }
    }

    private void generateScoreSchema() {
        //scores = new int[numberOfValues];
        // initial scoring matrix
        //initialScoringMatrix();
        //while (!checkDoubleCombinations()) {
        while (!checkDoubleCombinations(scores) || !checkTripleCombinations(scores)) {
            //printScoringMatrix();
            adjustScoringMatrix();
        }
        printScoringMatrix();
        printMessage();
//
    }

    private void printMessage() {
        System.out.println("");
        if (message != null)
            System.out.println(message);
    }

    private void initialScoringMatrix() {
        for (int index = 0; index < numberOfElements; index++) {
            scores[index] = index + 1;
        }
    }

    private void printScoringMatrix() {
        for (int score : scores) {
            System.out.print(score);
            System.out.print(",");
        }
        System.out.println("");
    }

    private void printAllCombinedValues(int[] scores) {
        combinedScore = new HashMap<String, Integer>();
        populateCombinedScoreMap();
        List<Integer> values = new ArrayList<Integer>();
        values.addAll(combinedScore.values());
        Collections.sort(values);
        for (int score : values) {
            System.out.print(score);
            System.out.print(",");
        }
        System.out.println("");
        int combinations = getNumberOfCombinations(scores);
        System.out.println("Number of Combinations found: " + values.size());
        System.out.println("Number of Combinations possible: " + combinations);
        if (values.size() != combinations)
            throw new RuntimeException("Thwe number of combinations found is smaller than the one possible, " +
                    "i.e. some values are duplicated and thus this scoring matrix is not likely to fulfill the" +
                    "requirements.");
    }

    private void printAllDoubleValues() {
        System.out.println("All Double Combinations:");
        combinedScore = new HashMap<String, Integer>();
        addAllScores(combinedScore);
        addAllDoubleCombinations();
        printValuesAndCombos();
    }

    private void printAllTripleValues() {
        System.out.println("All triple Combinations:");
        combinedScore = new HashMap<String, Integer>();
        addAllScores(combinedScore);
        addAllTripleCombinations();
        printValuesAndCombos();
    }

    private void printValuesAndCombos() {
        List<Integer> values = new ArrayList<Integer>();
        values.addAll(combinedScore.values());
        Collections.sort(values);
        for (int score : values) {
            System.out.print(score);
            System.out.print(": ");
            for (String combo : combinedScore.keySet()) {
                if (combinedScore.get(combo) == score)
                    System.out.println(combo);
            }
        }
    }

    private void adjustScoringMatrix() {
        int i = scores.length - 1;
        for (int index = i; index > 0; index--) {
            if (scores[index] < maxValue) {
                scores[index] += 1;
                rescoreUpperElements(scores[index], index);
                return;
            }
        }
    }

    private void rescoreUpperElements(int scoreValue, int index) {
        if (index >= scores.length - 1)
            return;
        for (int i = index + 1; i < scores.length; i++)
            scores[i] = scores[i - 1] + 1;
    }

    private boolean checkDoubleCombinations(int[] scores) {
        // add every pair of numbers
        Map<String, Integer> combinedScore = new HashMap<String, Integer>();
        addAllScores(combinedScore);

        for (int int1 = 0; int1 < scores.length; int1++) {
            for (int int2 = 0; int2 < scores.length; int2++) {
                if (int2 < int1)
                    continue;
                int totalScore = scores[int1] + scores[int2];
                if (combinedScore.containsValue(totalScore)) {
                    String combination = null;
                    for (String combo : combinedScore.keySet()) {
                        if (combinedScore.get(combo).equals(totalScore)) {
                            combination = combo;
                            break;
                        }
                    }
                    message = "Tried combination: " + scores[int1] + "," + scores[int2];
                    message += "  existing tuple: " + combination;
                    return false;
                } else {
                    combinedScore.put(scores[int1] + "," + scores[int2], totalScore);
                }
            }
        }
        return true;
    }

    private void addAllScores(Map<String, Integer> combinedScore) {
        for (int score : scores)
            combinedScore.put(score + "", score);
    }

    private static int getNumberOfCombinations(int[] scores) {
        int n = scores.length;
        // number of combinations for all distinct pairs
        int combinations = n * (n - 1) / faculty(2);
        combinations += n * (n - 1) * (n - 2) / faculty(3);
        // account for all equal numeral
        combinations += 2. * n;
        // account for two equals numbers in triple sum
        combinations += n * (n - 1);
        // account for all single numbers
        combinations += n;
        return combinations;
    }

    private boolean checkTripleCombinations(int[] scores) {
        // add every pair of numbers
        Map<String, Integer> combinedScore = new HashMap<String, Integer>();
        addAllScores(combinedScore);

        for (int int1 = 0; int1 < scores.length; int1++) {
            for (int int2 = 0; int2 < scores.length; int2++) {
                if (int2 < int1)
                    continue;
                for (int int3 = 0; int3 < scores.length; int3++) {
                    if (int3 < int2)
                        continue;
                    int totalScore = scores[int1] + scores[int2] + scores[int3];
                    if (combinedScore.containsValue(totalScore)) {
                        String combination = null;
                        for (String combo : combinedScore.keySet()) {
                            if (combinedScore.get(combo).equals(totalScore)) {
                                combination = combo;
                                break;
                            }
                        }
                        message = "Tried combination: " + scores[int1] + "," + scores[int2] + "," + scores[int3];
                        message += "  existing tuple: " + combination;
                        return false;
                    } else {
                        combinedScore.put(scores[int1] + "," + scores[int2] + "," + scores[int3], totalScore);
                    }
                }
            }
        }
        return true;
    }

    private static int faculty(int length) {
        int total = 1;
        for (int index = 1; index <= length; index++)
            total *= index;
        return total;
    }


    public int getNextScoreMatrixElement() {
        int newMaxElement = scores[scores.length - 1];
        while (combinedScore.containsValue(newMaxElement) || !checkDoubleCombinations(getNewScoreMatrix(newMaxElement))
                || !checkTripleCombinations(getNewScoreMatrix(newMaxElement)))
            newMaxElement++;
        return newMaxElement;
    }

    private int[] getNewScoreMatrix(int newMaxElement) {
        int[] oldScores = scores;
        int[] newScores = new int[scores.length + 1];
        int index = 0;
        for (int score : oldScores)
            newScores[index++] = score;
        newScores[index] = newMaxElement;
        return newScores;
    }
}
