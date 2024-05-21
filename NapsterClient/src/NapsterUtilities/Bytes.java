/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */
public class Bytes {
    public long totalBytes;
    public long transferringBytes;
    public boolean transferring;
    
    public double getPercentage() {
        return ((double)this.transferringBytes / this.totalBytes) * 100;
    }
    
    public String getPercentageStr() {
        return String.format("%.2f", ((double)this.transferringBytes / this.totalBytes) * 100) + "%";
    }
    
    public String getTotalBytesStr() {
        if (this.totalBytes < 1024) {
            return this.totalBytes + " B";
        } else if (this.totalBytes < 1024 * 1024) {
            return String.format("%.2f KB", (double) this.totalBytes / 1024);
        } else if (this.totalBytes < 1024 * 1024 * totalBytes) {
            return String.format("%.2f MB", (double) this.totalBytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", (double) this.totalBytes / (1024 * 1024 * 1024));
        }
    }
    
    public String getTransferringBytesStr() {
        if (this.transferringBytes < 1024) {
            return this.transferringBytes + " B";
        } else if (this.transferringBytes < 1024 * 1024) {
            return String.format("%.2f KB", (double) this.transferringBytes / 1024);
        } else if (this.transferringBytes < 1024 * 1024 * transferringBytes) {
            return String.format("%.2f MB", (double) this.transferringBytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", (double) this.transferringBytes / (1024 * 1024 * 1024));
        }
    }
    
    public static String getBytesStr(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", (double) bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", (double) bytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", (double) bytes / (1024 * 1024 * 1024));
        }
    }
    
    public String toString() {
        return "Transferred Bytes: " +  Long.valueOf(this.transferringBytes).toString()
                + ", Total Bytes: " + Long.valueOf(this.totalBytes)
                + ((this.transferring) ? ", Transferring" : ", Not Transferring");
    }
}
