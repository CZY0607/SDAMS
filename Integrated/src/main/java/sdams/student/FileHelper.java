package sdams.student;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {


    /**
     * Helper class and method that loads all the objects into a list from a file
     * @param filename a file that has to be loaded to
     * @param expectedLength length of the contents for each line
     * @return a list loaded with all the contents
     * @throws IOException IOException
     */
    public static List<FileRecord> loadAll(String filename, int expectedLength) throws IOException {
        Validator.checkInput(filename, "File Name");

        if (expectedLength == 0 || expectedLength < 0) {
            throw new IllegalArgumentException("Expected field must be greater than 0");
        }

        File file = new File("data", filename);
        List<FileRecord> records = new ArrayList<>();

        if (!file.exists()) {
            return records;
        }

        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8)))  {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.isBlank()) {
                    continue;
                }

                String[] stringParts = line.split("\\|", -1);

                if (stringParts.length != expectedLength) {
                    throw new IOException("Invalid record on line " + lineCount + ". Expected " + expectedLength + " fields, but found " + stringParts.length + ".");
                }

               records.add(new FileRecord(lineCount, stringParts));

            }
        }

        return records;

    }

    /**
     * Similar to load method this saves all the contents and objects properly onto a designated file.
     * @param fileName the file where the data will be saved at
     * @param records a list of records and data that has to be saved
     * @throws IOException IOException
     */
    public static void saveAll(String fileName, List<String> records) throws IOException {
        Validator.checkInput(fileName, "File Name");

        if (records == null) {
            throw new IllegalArgumentException("Records cannot be null.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data", fileName), java.nio.charset.StandardCharsets.UTF_8))) {

            for (String record : records) {
                writer.write(record);
                writer.newLine();

            }
        }
    }

}
