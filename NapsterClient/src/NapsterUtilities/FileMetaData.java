/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */
import javax.naming.SizeLimitExceededException;

public class FileMetaData {
    public static final int maxUsernameLength = ClientManager.maxUsernameLength;
    public static final int maxFilenameLength = 200;
    public static final int maxIPAddressLength = 15;
    private String filename;
    private long filesize;
    private String username;
    private String ip_address;

    public FileMetaData(String filename, long filesize, String username, String ip_address) throws SizeLimitExceededException {
        if (filename.length() > maxFilenameLength) {
            throw new SizeLimitExceededException("Filename cannot be of more than " + maxFilenameLength + " characters.");
        }
        if (username.length() > maxUsernameLength) {
            throw new SizeLimitExceededException("Username cannot be of more than " + maxUsernameLength + " characters.");
        }
        if (ip_address.length() > maxIPAddressLength) {
            throw new SizeLimitExceededException("IP address cannot be of more than " + maxIPAddressLength + " characters.");
        }
        
        this.filename = filename;
        this.filesize = filesize;
        this.username = username;
        this.ip_address = ip_address;
    }


    public String toString() {
        return "Filename: " + filename 
            + ", Filesize: " + filesize 
            + ", Username: " + username 
            + ", IP Address: " + ip_address
            + '\n';
    }


    public String getFilename() {
        return this.filename;
    }
    
    public long getFilesize() {
        return this.filesize;
    }

    public String getUsername() {
        return this.username;
    }

    public String getUserIPAddress() {
        return this.ip_address;
    }
    
    public String getFilesizeStr() {
        if (this.filesize < 1024) {
            return this.filesize + " B";
        } else if (this.filesize < 1024 * 1024) {
            return String.format("%.2f KB", (double) this.filesize / 1024);
        } else if (this.filesize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", (double) this.filesize / (1024 * 1024));
        } else {
            return String.format("%.2f GB", (double) this.filesize / (1024 * 1024 * 1024));
        }
    }
    
    

}