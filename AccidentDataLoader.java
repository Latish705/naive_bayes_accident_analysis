import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AccidentDataLoader {

    public static List<AccidentRecord> loadDataset(String filePath) {
        List<AccidentRecord> dataset = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Sanitize the row to ensure it has the correct number of columns
                if (values.length >= 22) {
                    // Use columns as is
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
                    // Handle missing columns (fill missing values with default data or skip the row)
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
