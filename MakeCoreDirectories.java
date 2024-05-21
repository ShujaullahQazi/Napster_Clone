import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class MakeCoreDirectories {
    public static final String SOLR_HOME = "D:/Programs/Solr/solr-9.5.0/";

    public static void main(String[] args) {
        String confDir = SOLR_HOME + "server/solr/configsets/_default/conf";
        String usersCoreDir = SOLR_HOME + "server/solr/users/conf";
        String filesCoreDir = SOLR_HOME + "server/solr/files/conf";

        File conf = new File(confDir);
        File users = new File(usersCoreDir);
        File files = new File(filesCoreDir);

        try {
            copyDirectory(conf, users);
            copyDirectory(conf, files);
            System.out.println("Core directories created successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void copyDirectory(File sourceDir, File destDir) throws IOException {
        // Create destination directory if it doesn't exist
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // List all files and sub-directories in the source directory
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                // Construct the destination file path
                Path destFilePath = destDir.toPath().resolve(file.getName());
                if (file.isDirectory()) {
                    // If it's a directory, recursively copy it
                    copyDirectory(file, destFilePath.toFile());
                } else {
                    // If it's a file, copy it to the destination directory
                    Files.copy(file.toPath(), destFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
