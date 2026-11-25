package sdams.general;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipe-delimited .txt storage, one record per line:  field1|field2|field3
 * Special characters inside a field are escaped:  \ becomes \\   | becomes \p   newline becomes \n
 * Writes go to a temp file first and are then moved into place, so a crash never leaves half a file.
 * Everyone on the team should use this class for reading and writing their .txt files.
 */
public class FileStore {
    private final Path file;

    public FileStore(String fileName) {
        Path dir = Paths.get(System.getProperty("sdams.data", "data"));
        this.file = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            if (!Files.exists(file)) {
                Files.write(file, new byte[0]);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot prepare data file " + file + ": " + e.getMessage(), e);
        }
    }

    static String escape(String value) {
        String v = value == null ? "" : value;
        return v.replace("\\", "\\\\").replace("|", "\\p").replace("\r\n", "\\n").replace("\n", "\\n");
    }

    static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                sb.append(next == 'p' ? '|' : next == 'n' ? '\n' : next);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Every non-empty line, split into its fields. */
    public List<String[]> readAll() {
        try {
            List<String[]> rows = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = unescape(parts[i]);
                }
                rows.add(parts);
            }
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file + ": " + e.getMessage(), e);
        }
    }

    public void writeAll(List<String[]> rows) {
        List<String> lines = new ArrayList<>();
        for (String[] row : rows) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    sb.append('|');
                }
                sb.append(escape(row[i]));
            }
            lines.add(sb.toString());
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save " + file + ": " + e.getMessage(), e);
        }
    }
}
