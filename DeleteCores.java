import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DeleteCores {

    // Solr URL
    private static final String SOLR_URL = "http://localhost:8984/solr/admin/cores";

    // Core names
    private static final String[] CORE_NAMES = {"users", "files"};

    public static void main(String[] args) {
        // Delete cores
        for (String coreName : CORE_NAMES) {
            deleteCore(coreName);
        }
    }

    private static void deleteCore(String coreName) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SOLR_URL + "?action=UNLOAD&core=" + coreName + "&deleteInstanceDir=true&wt=json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println("Core '" + coreName + "' unloaded and deleted successfully.");
            } else {
                System.out.println("Error deleting core '" + coreName + "'.");
                System.out.println("Error message: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error deleting core '" + coreName + "'.");
            System.out.println("Error message: " + e.getMessage());
        }
    }
}
