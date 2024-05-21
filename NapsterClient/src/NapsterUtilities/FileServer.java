/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


public class FileServer {
    private volatile boolean isRunning = false;
    private ExecutorService executorService;
    private Thread serverThread;
    private static final int BUFFER_SIZE = 1024;
    private ServerSocket serverSocket;
    private int port = 5000;
    private int maxParallelUploads = 5;
    public Bytes[] uploadingBytes;
    public FileMetaData[] uploadingFiles;

    public FileServer() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        this.executorService = Executors.newFixedThreadPool(this.maxParallelUploads);
        this.uploadingBytes = new Bytes[10];
        for (int i = 0; i < this.maxParallelUploads; i++) {
            this.uploadingBytes[i] = new Bytes();
            this.uploadingBytes[i].transferring = false;
        }
        this.uploadingFiles = new FileMetaData[10];
        try {
            for (int i = 0; i < this.maxParallelUploads; i++) {
                this.uploadingFiles[i] = new FileMetaData("", 0, "", "");
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public int getMaxParallelUploads() {
        return this.maxParallelUploads;
    }
    
    // Function to send a file over the socket.
    private long sendFile(FileInputStream fis, Socket socket, Bytes uploadedBytes) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesSent = 0;
        int bytesRead;

        do {
            // Read a chunk of data from the file into the buffer.
            bytesRead = fis.read(buffer);
            if (bytesRead == -1) {
                break;
            }
            // Send the data chunk over the socket.
            socket.getOutputStream().write(buffer, 0, bytesRead);
            totalBytesSent += bytesRead;
            uploadedBytes.transferringBytes = totalBytesSent;
        } while (bytesRead > 0);

        return totalBytesSent;
    }

    private void uploadFile(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String requestedFilename = dis.readUTF();

        int i;
        for (i = 0; i < this.maxParallelUploads; i++) {
            if (!this.uploadingBytes[i].transferring) {
                break;
            }
        }
        if (i < this.maxParallelUploads) {
            long filesize = Files.size(Paths.get("publish/" + requestedFilename));
            this.uploadingBytes[i].transferring = true;
            this.uploadingBytes[i].totalBytes = filesize;
            
            try {
                this.uploadingFiles[i] = new FileMetaData(requestedFilename, filesize, 
                                                            "", socket.getInetAddress().getHostAddress());
            }
            catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        
        FileInputStream fis = new FileInputStream("publish/" + requestedFilename);
        long bytesSent = this.sendFile(fis, socket, this.uploadingBytes[i]);
        this.uploadingBytes[i].transferring = false;
        
        fis.close();
        socket.close();

        System.out.println("Sent File: " + requestedFilename);
        System.out.println("File Size: " + bytesSent + " bytes");
    }

    private void startServing() throws IOException {
        while (true) {
            Socket clientSocket = this.serverSocket.accept();
            InetAddress clientAddress = clientSocket.getInetAddress();
            System.out.println("Accepted a connection request.");
            System.out.println("Client IP Address: " + clientAddress.getHostAddress());

            // Execute the uploadFile function asynchronously using the executor service.
            executorService.execute(() -> {
                try {
                    this.uploadFile(clientSocket);
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            });
        }
    }

    public void start() {
        if (!isRunning) {
            this.serverThread = new Thread(() -> {
                try {
                    startServing();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            });
            this.serverThread.start();
            this.isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            try {
                executorService.shutdown();
                serverSocket.close();
                serverThread.join();
            } catch (IOException | InterruptedException e) {
                System.out.println(e.toString());
            }
            isRunning = false;
        }
    }


    
}
