import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

class AccidentDataLoader {

    public static List<AccidentRecord> loadDataset(String filePath) {
        List<AccidentRecord> dataset = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Sanitize the row to ensure it has the correct number of columns
                if (values.length >= 22) {
                    dataset.add(new AccidentRecord(
                        values[3],  // Junction_Control
                        values[4],  // Junction_Detail
                        values[7],  // Light_Conditions
                        values[14], // Road_Surface_Conditions
                        values[19], // Weather_Conditions
                        values[21], // Vehicle_Type
                        values[5]   // Accident_Severity (Target)
                    ));
                } else {
                    String[] sanitizedValues = sanitizeRow(values, 22); // Ensure 22 columns
                    dataset.add(new AccidentRecord(
                        sanitizedValues[3],  
                        sanitizedValues[4],  
                        sanitizedValues[7],  
                        sanitizedValues[14], 
                        sanitizedValues[19], 
                        sanitizedValues[21], 
                        sanitizedValues[5]
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataset;
    }

    private static String[] sanitizeRow(String[] values, int expectedLength) {
        String[] sanitized = new String[expectedLength];
        
        // Copy existing values and fill in missing ones
        for (int i = 0; i < expectedLength; i++) {
            if (i < values.length) {
                sanitized[i] = values[i];
            } else {
                sanitized[i] = "Data missing"; // Default value for missing data
            }
        }
        return sanitized;
    }
}

class AccidentRecord {
    String junctionControl;
    String junctionDetail;
    String lightConditions;
    String roadSurfaceConditions;
    String weatherConditions;
    String vehicleType;
    String accidentSeverity; // This is the target

    public AccidentRecord(String junctionControl, String junctionDetail, String lightConditions,
                          String roadSurfaceConditions, String weatherConditions, String vehicleType, String accidentSeverity) {
        this.junctionControl = junctionControl;
        this.junctionDetail = junctionDetail;
        this.lightConditions = lightConditions;
        this.roadSurfaceConditions = roadSurfaceConditions;
        this.weatherConditions = weatherConditions;
        this.vehicleType = vehicleType;
        this.accidentSeverity = accidentSeverity;
    }
}

class NaiveBayesAccidentClassifier {

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
        System.out.println("Testing Results:");
        for (AccidentRecord record : testSet) {
            String predictedSeverity = classifier.predict(record);
            System.out.println("Actual: " + record.accidentSeverity + ", Predicted: " + predictedSeverity);
            if (predictedSeverity.equals(record.accidentSeverity)) {
                correctPredictions++;
            }
        }

        double accuracy = (double) correctPredictions / testSet.size();
        System.out.println("Accuracy: " + accuracy);
    }
}
