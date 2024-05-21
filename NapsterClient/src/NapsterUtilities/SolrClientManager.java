/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NapsterUtilities;

/**
 *
 * @author HP
 */

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SolrClientManager {
    private String server1_url = "http://192.168.43.47:8080/";
    private String server2_url = "http://192.168.43.182:8081/";
    
    private final String users_core = "users";
    private final String files_core = "files";
    private SolrClient usersSolrClient;
    private SolrClient filesSolrClient;
    private SolrClient usersSolrClient1;
    private SolrClient filesSolrClient1;
    private SolrClient usersSolrClient2;
    private SolrClient filesSolrClient2;
    
    private int solrClient = 1;

    public SolrClientManager() {
        this.usersSolrClient1 = new Http2SolrClient.Builder(server1_url + this.users_core).useHttp1_1(true).build();
        this.filesSolrClient1 = new Http2SolrClient.Builder(server1_url + this.files_core).useHttp1_1(true).build();
        this.usersSolrClient2 = new Http2SolrClient.Builder(server2_url + this.users_core).useHttp1_1(true).build();
        this.filesSolrClient2 = new Http2SolrClient.Builder(server2_url + this.files_core).useHttp1_1(true).build();
        
        this.usersSolrClient = this.usersSolrClient1;
        this.filesSolrClient = this.filesSolrClient1;
    }
    
    
    public void changeSolrClient() {
        if (this.solrClient == 1) {
            this.usersSolrClient = this.usersSolrClient2;
            this.filesSolrClient = this.filesSolrClient2;
            this.solrClient = 2;
        }
        else {
            this.usersSolrClient = this.usersSolrClient1;
            this.filesSolrClient = this.filesSolrClient1;
            this.solrClient = 1;
        }
    }
    
    
    private boolean checkUserExists(String username) throws IOException, SolrServerException {
        return usersSolrClient.getById(username) != null;
    }


    public void addUser(String username, String ipAddress, boolean isOnline) throws Exception {
        if (checkUserExists(username)) {
            throw new Exception("User with username \"" + username + "\" already exists.");
        }
        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id", username);
            document.addField("ip_address", ipAddress);
            document.addField("is_online", isOnline);

            usersSolrClient.add(document);
            usersSolrClient.commit();
            System.out.println("User \"" + username + "\" added successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error adding user \"" + username + "\": " + e.getMessage());
        }
    }


    public void deleteUser(String username) throws Exception {
        try {
            usersSolrClient.deleteById(username);
            usersSolrClient.commit();
            System.out.println("User \"" + username + "\" removed successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error removing user \"" + username + "\": " + e.getMessage());
        }
    }


    public void updateUserStatus(String username, String ipAddress, boolean isOnline) throws Exception {
        if (!checkUserExists(username)) {
            throw new Exception("User with username \"" + username + "\" does not exist.");
        }
        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id", username);
            document.addField("ip_address", ipAddress);
            document.addField("is_online", isOnline);

            UpdateRequest request = new UpdateRequest();
            request.add(document);

            usersSolrClient.request(request);
            usersSolrClient.commit();
            System.out.println("User \"" + username + "\" status updated successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error updating status for user \"" + username + "\": " + e.getMessage());
        }
    }
    
    

    private boolean checkFileExists(String id) throws IOException, SolrServerException {
        return filesSolrClient.getById(id) != null;
    }


    public void publishFile(String filename, long filesize, String user) throws Exception {
        String id = user + "_" + filename;
        if (!checkUserExists(user)) {
            throw new Exception("User with username \"" + user + "\" does not exist.");
        }
        if (checkFileExists(id)) {
            throw new Exception("File with ID \"" + id + "\" already exists.");
        }
        try {
            SolrInputDocument document = new SolrInputDocument();
            document.addField("id", id);
            document.addField("filename", filename);
            document.addField("filesize", filesize);
            document.addField("user", user);

            filesSolrClient.add(document);
            filesSolrClient.commit();
            System.out.println("File \"" + filename + "\" published successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error publishing file \"" + filename + "\": " + e.getMessage());
        }
    }


    public void removeFile(String filename, String username) throws Exception {
        String id = username + "_" + filename;
        try {
            filesSolrClient.deleteById(id);
            filesSolrClient.commit();
            System.out.println("File \"" + filename + "\" removed successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error removing file \"" + filename + "\": " + e.getMessage());
        }
    }



    public void removeFilesByUsername(String username) throws Exception {
        String query = "id:" + username + "*";
        
        try {
            UpdateRequest request = new UpdateRequest();
            request.deleteByQuery(query);
            filesSolrClient.request(request);
            filesSolrClient.commit();
            System.out.println("Files associated with user \"" + username + "\" removed successfully.");
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error removing files associated with user \"" + username + "\": " + e.getMessage());
        }
    }


    public void removeUser(String username) throws Exception {
        removeFilesByUsername(username); // Remove all files associated with the user
        deleteUser(username); // Then remove the user
    }


    public List<FileMetaData> searchFiles(String filename) throws Exception {
        List<FileMetaData> searchResults = new ArrayList<>();

        try {
            // Construct Solr query for case insensitive search with wildcard
            String query = "filename:*" + filename + "*";

            // Set query options for case insensitive search
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.set(CommonParams.SORT, "filename asc");
            solrQuery.set(CommonParams.ROWS, Integer.toString(Integer.MAX_VALUE));
            
            QueryResponse response = filesSolrClient.query(solrQuery);

            // Retrieve additional information from the users core
            for (SolrDocument document : response.getResults()) {
                String username = (String) document.getFieldValue("user");
                SolrQuery userQuery = new SolrQuery("id:" + username);
                QueryResponse userResponse = usersSolrClient.query(userQuery);

                // Get user details from the users core
                SolrDocument userDocument = userResponse.getResults().get(0);
                String ipAddress = (String) userDocument.getFieldValue("ip_address");
                boolean isOnline = (boolean) userDocument.getFieldValue("is_online");

                if (isOnline) {
                    // Create FileMetaData object and add to searchResults list
                    FileMetaData metaData = new FileMetaData(
                            (String) document.getFieldValue("filename"),
                            (long) document.getFieldValue("filesize"),
                            username,
                            ipAddress
                    );
                    searchResults.add(metaData);
                }
            }
        } catch (SolrServerException | IOException e) {
            
            
            throw new Exception("Error searching files: " + e.getMessage());
        }

        return searchResults;
    }

    public List<FileMetaData> getPublishedFiles(String username) throws Exception {
        List<FileMetaData> searchResults = new ArrayList<>();

        try {
            // Construct Solr query to find all files associated with the given username
            SolrQuery query = new SolrQuery("user:" + username);
            query.set(CommonParams.ROWS, Integer.toString(Integer.MAX_VALUE));
            
            QueryResponse response = filesSolrClient.query(query);

            // Retrieve additional information from the users core
            for (SolrDocument document : response.getResults()) {
                String filename = (String) document.getFieldValue("filename");
                long filesize = (long) document.getFieldValue("filesize");

                // Get user details from the users core
                SolrQuery userQuery = new SolrQuery("id:" + username);
                QueryResponse userResponse = usersSolrClient.query(userQuery);
                SolrDocument userDocument = userResponse.getResults().get(0);
                String ipAddress = (String) userDocument.getFieldValue("ip_address");
                boolean isOnline = (boolean) userDocument.getFieldValue("is_online");

                // Create FileMetaData object and add to searchResults list
                FileMetaData metaData = new FileMetaData(filename, filesize, username, ipAddress);
                searchResults.add(metaData);
            }
        } catch (SolrServerException | IOException e) {
            throw new Exception("Error retrieving published files for user \"" + username + "\": " + e.getMessage());
        }

        return searchResults;
    }


    public void closeClients() throws Exception {
        try {
            usersSolrClient.close();
            filesSolrClient.close();
            System.out.println("Solr clients closed successfully.");
        } catch (IOException e) {
            throw new Exception("Error closing Solr clients: " + e.getMessage());
        }
    }
}





