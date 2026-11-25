package sdams.student;

public class FileRecord {

    private final int lineNumber;
    private final String[] fields;

    /**
     * Constructor FileRecord class to keep track of records in the file
     * @param lineNumber line number in the file record
     * @param fields contents that are passed into the file
     */
    public FileRecord(int lineNumber, String[] fields) {
        this.lineNumber = lineNumber;
        this.fields = fields;
    }

    /**
     * Retrieves the line number from the file records
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Retrieves the fields and the contents of the file records
     * @return fields of the file record
     */
    public String[] getFields() {
        return fields;
    }
}
