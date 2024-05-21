/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileClient {
    private static final int BUFFER_SIZE = 1024;
    private int port = 5000;
    private ExecutorService executorService;
    private int maxParallelDownloads = 5;

    public FileClient() {
        this.executorService = Executors.newFixedThreadPool(this.maxParallelDownloads);
    }

    public int getMaxParallelDownloads() {
        return this.maxParallelDownloads;
    }
    
    // Function to receive a file from a client socket.
    private long receiveFile(FileOutputStream fos, Socket socket, long filesize, Bytes downloadedBytes) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesReceived = 0;
        int maxBytesToRead;
        int bytesRead;

        while (true) {
            // If remaining bytes are greater than or equal to BUFFER_SIZE then bytes equal BUFFER_SIZE can be received.
            if (filesize - totalBytesReceived >= BUFFER_SIZE) {
                maxBytesToRead = BUFFER_SIZE;
            } else {
                maxBytesToRead = (int) (filesize - totalBytesReceived);
            }
            bytesRead = socket.getInputStream().read(buffer, 0, maxBytesToRead);
            if (bytesRead == -1 || totalBytesReceived >= filesize) {
                break;
            }

            // Write the received data to the local file.
            fos.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
            downloadedBytes.transferringBytes = totalBytesReceived;
        }
        
        return totalBytesReceived;
    }

    public void downloadFile(String filename, long filesize, String serverIP, Bytes downloadedBytes) throws IOException {
        Socket socket = new Socket(serverIP, this.port);

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // Request the file by sending filename.
        dos.writeUTF(filename);

        FileOutputStream fos = new FileOutputStream("downloads/" + filename);
        long bytesReceived = this.receiveFile(fos, socket, filesize, downloadedBytes);
        fos.close();
        socket.close();

        System.out.println("Received File: " + filename);
        System.out.println("File Size: " + bytesReceived + " bytes");
    }

    public void downloadFileAsync(String filename, long filesize, String serverIP, Bytes downloadedBytes) {
        executorService.execute(() -> {
            try {
                this.downloadFile(filename, filesize, serverIP, downloadedBytes);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        });
    }
}

