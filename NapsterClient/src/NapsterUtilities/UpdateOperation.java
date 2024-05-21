/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */
public class UpdateOperation {
    public static final String publish = "publish";
    public static final String remove = "remove";
    public String operation;
    public FileMetaData fileMetaData;

    public UpdateOperation(FileMetaData fileMetaData, String operation) {
        this.operation = operation;
        this.fileMetaData = fileMetaData;
    }

    public String toString() {
        return "Filename: " + this.fileMetaData.getFilename() 
                + ", Filesize: " + this.fileMetaData.getFilesizeStr() 
                + ", Operation: " + this.operation + '\n';
    }
}

