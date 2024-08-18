import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NaiveBayesAccidentClassifier {

    private Map<String, Integer> classCounts = new HashMap<>();
    private Map<String, Map<String, Integer>> featureCounts = new HashMap<>();
    private int totalRecords = 0;

    public void train(List<AccidentRecord> dataset) {
        for (AccidentRecord record : dataset) {
            classCounts.put(record.accidentSeverity, classCounts.getOrDefault(record.accidentSeverity, 0) + 1);
            totalRecords++;

            updateFeatureCount("junctionControl", record.junctionControl, record.accidentSeverity);
            updateFeatureCount("junctionDetail", record.junctionDetail, record.accidentSeverity);
            updateFeatureCount("lightConditions", record.lightConditions, record.accidentSeverity);
            updateFeatureCount("roadSurfaceConditions", record.roadSurfaceConditions, record.accidentSeverity);
            updateFeatureCount("weatherConditions", record.weatherConditions, record.accidentSeverity);
            updateFeatureCount("vehicleType", record.vehicleType, record.accidentSeverity);
        }
    }

    private void updateFeatureCount(String feature, String featureValue, String classLabel) {
        featureCounts.putIfAbsent(feature, new HashMap<>());
        Map<String, Integer> countsForFeature = featureCounts.get(feature);
        String key = classLabel + "_" + featureValue;
        countsForFeature.put(key, countsForFeature.getOrDefault(key, 0) + 1);
    }

    public String predict(AccidentRecord record) {
        double bestProbability = Double.NEGATIVE_INFINITY;
        String bestClass = null;

        for (String classLabel : classCounts.keySet()) {
            double classProbability = Math.log(classCounts.get(classLabel) * 1.0 / totalRecords);

            classProbability += Math.log(calculateFeatureProbability("junctionControl", record.junctionControl, classLabel));
            classProbability += Math.log(calculateFeatureProbability("junctionDetail", record.junctionDetail, classLabel));
            classProbability += Math.log(calculateFeatureProbability("lightConditions", record.lightConditions, classLabel));
            classProbability += Math.log(calculateFeatureProbability("roadSurfaceConditions", record.roadSurfaceConditions, classLabel));
            classProbability += Math.log(calculateFeatureProbability("weatherConditions", record.weatherConditions, classLabel));
            classProbability += Math.log(calculateFeatureProbability("vehicleType", record.vehicleType, classLabel));

            if (classProbability > bestProbability) {
                bestProbability = classProbability;
                bestClass = classLabel;
            }
        }

        return bestClass;
    }

    private double calculateFeatureProbability(String feature, String featureValue, String classLabel) {
        int featureCount = featureCounts.getOrDefault(feature, new HashMap<>()).getOrDefault(classLabel + "_" + featureValue, 0);
        int totalClassCount = classCounts.get(classLabel);
        return (featureCount + 1.0) / (totalClassCount + featureCounts.get(feature).size()); // Laplace smoothing
    }

    public static void main(String[] args) {
        String filePath = "RoadAccidentData.csv";
        List<AccidentRecord> dataset = AccidentDataLoader.loadDataset(filePath);

        // Split dataset into training and testing sets (e.g., 80/20 split)
        int splitIndex = (int) (dataset.size() * 0.8);
        List<AccidentRecord> trainingSet = dataset.subList(0, splitIndex);
        List<AccidentRecord> testSet = dataset.subList(splitIndex, dataset.size());

        NaiveBayesAccidentClassifier classifier = new NaiveBayesAccidentClassifier();
        classifier.train(trainingSet);

        // Test the model
        int correctPredictions = 0;
        for (AccidentRecord record : testSet) {
            String predictedSeverity = classifier.predict(record);
            if (predictedSeverity.equals(record.accidentSeverity)) {
                correctPredictions++;
            }
        }

        double accuracy = (double) correctPredictions / testSet.size();
        System.out.println("Accuracy: " + accuracy);
    }
}
