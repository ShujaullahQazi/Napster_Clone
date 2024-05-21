/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.naming.SizeLimitExceededException;


public class ClientManager {
    public static final int maxUsernameLength = 100;
    public static final int maxFilenameLength = FileMetaData.maxFilenameLength;
    public static final int recordLength = FileMetaData.maxFilenameLength + Long.BYTES;
    private File usernameFile;
    private File publishedFilesRecordFile;
    private File downloadDir;
    private File publishDir;

    public ClientManager() throws IOException {
        this.usernameFile = new File("username.dat");
        this.publishedFilesRecordFile = new File("published_files.dat");
        this.downloadDir = new File("downloads");
        this.publishDir = new File("publish");

        // Create the files and directories if they do not exist.
        this.usernameFile.createNewFile();
        this.publishedFilesRecordFile.createNewFile();
        this.downloadDir.mkdir();
        this.publishDir.mkdir();
    }

    public void deleteFiles() {
        this.usernameFile.delete();
        this.publishedFilesRecordFile.delete();
    }
    
    public void writeUsername(String username) throws FileNotFoundException, IOException, SizeLimitExceededException {
        if (username.length() >= maxUsernameLength) {
            throw new SizeLimitExceededException("Username cannot be of more than " + maxUsernameLength + "characters.");
        }
        FileOutputStream fos = new FileOutputStream(this.usernameFile);
        fos.write(username.getBytes());
        fos.close();
    }

    public String readUsername() throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(this.usernameFile);
        String username = new String(fis.readAllBytes());
        fis.close();
        return username;
    }

    public void writeFileRecords() throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(this.publishedFilesRecordFile);
        File[] files = this.publishDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fos.write(files[i].getName().getBytes());
                for (int j = 0; j < maxFilenameLength - files[i].getName().length(); j++) {
                    fos.write('\0');
                }
                fos.write(ByteBuffer.allocate(Long.BYTES).putLong(files[i].length()).array());
            }
        }
        fos.close();
    }

    public List<FileMetaData> readFileRecords() throws FileNotFoundException, IOException, SizeLimitExceededException {
        FileInputStream fis = new FileInputStream(this.publishedFilesRecordFile);
        List<FileMetaData> records = new ArrayList<>();
        String filename;
        long filesize;
        byte[] filenameBytes = new byte[maxFilenameLength];
        byte[] filesizeBytes = new byte[Long.BYTES];

        int numRecords = (int) this.publishedFilesRecordFile.length() / recordLength;
        for (int i = 0; i < numRecords; i++) {
            fis.read(filenameBytes, 0, filenameBytes.length);
            filename = new String(filenameBytes).trim();
            fis.read(filesizeBytes, 0, filesizeBytes.length);
            filesize = ByteBuffer.wrap(filesizeBytes).getLong();
            records.add(new FileMetaData(filename, filesize, "", ""));
        }
        fis.close();
        return records;
    }

    public List<UpdateOperation> getUpdateOperations(List<FileMetaData> publishedFiles) throws FileNotFoundException, IOException, SizeLimitExceededException {
        List<UpdateOperation> changes = new ArrayList<>();
        List<FileMetaData> localFiles = this.readFileRecords();
        boolean found;

        // Check for files to be removed
        for (FileMetaData publishedFile : publishedFiles) {
            found = false;
            for (FileMetaData localFile : localFiles) {
                if (publishedFile.getFilename().equals(localFile.getFilename()) && publishedFile.getFilesize() == localFile.getFilesize()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changes.add(new UpdateOperation(publishedFile, UpdateOperation.remove));
            }
        }

        // Check for files to be published
        for (FileMetaData localFile : localFiles) {
            found = false;
            for (FileMetaData publishedFile : publishedFiles) {
                if (localFile.getFilename().equals(publishedFile.getFilename()) && localFile.getFilesize() == publishedFile.getFilesize()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changes.add(new UpdateOperation(localFile, UpdateOperation.publish));
            }
        }

        return changes;
    }
    
    public String getIPAddress() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        return localhost.getHostAddress();   
    }
    

}



