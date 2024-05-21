import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CreateCores {

    // Solr URL
    private static final String SOLR_URL = "http://localhost:8984/solr/";

    // Core names
    private static final String[] CORE_NAMES = {"users", "files"};

    private static final String[] SCHEMAS = {
        // Schema for users core
        "{"
        + "\"add-field\": {"
        + "\"name\":\"ip_address\","
        + "\"type\":\"string\","
        + "\"indexed\":true,"
        + "\"stored\":true"
        + "\"multiValued\":false"
        + "},"
        + "\"add-field\": {"
        + "\"name\":\"is_online\","
        + "\"type\":\"boolean\","
        + "\"indexed\":true,"
        + "\"stored\":true"
        + "\"multiValued\":false"
        + "}"
        + "}",
        
        // Schema for files core
        "{"
        + "\"add-field\": {"
        + "\"name\":\"filename\","
        + "\"type\":\"string\","
        + "\"indexed\":true,"
        + "\"stored\":true"
        + "\"multiValued\":false"
        + "},"
        + "\"add-field\": {"
        + "\"name\":\"filesize\","
        + "\"type\":\"plong\","
        + "\"indexed\":true,"
        + "\"stored\":true"
        + "\"multiValued\":false"
        + "},"
        + "\"add-field\": {"
        + "\"name\":\"user\","
        + "\"type\":\"string\","
        + "\"indexed\":true,"
        + "\"stored\":true"
        + "\"multiValued\":false"
        + "}"
        + "}"
        };

    public static void main(String[] args) {
        // Create cores
        for (int i = 0; i < CORE_NAMES.length; i++) {
            createCore(CORE_NAMES[i]);
            addSchema(CORE_NAMES[i], SCHEMAS[i]);
        }
    }

    private static void createCore(String coreName) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SOLR_URL + "admin/cores?action=CREATE&name=" + coreName +
                        "&numShards=1&replicationFactor=1&wt=json")) // coreName + &configSet=_default
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Core '" + coreName + "' created successfully.");
            } else {
                System.out.println("Failed to create core '" + coreName + "'.");
                System.out.println("Error message: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to create core '" + coreName + "'.");
            System.out.println("Error message: " + e.getMessage());
        }
    }

    private static void addSchema(String coreName, String schema) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SOLR_URL + coreName + "/schema"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(schema))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Schema added for core '" + coreName + "'.");
            } else {
                System.out.println("Failed to add schema for core '" + coreName + "'.");
                System.out.println("Error message: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to add schema for core '" + coreName + "'.");
            System.out.println("Error message: " + e.getMessage());
        }
    }
}