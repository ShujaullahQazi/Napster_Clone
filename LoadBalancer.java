import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;



public class LoadBalancer {
    private static final String[] SOLR_SERVERS = {"http://192.168.43.47:8984/solr", "http://192.168.43.182:8984/solr"};
    static int port = 8080;
    private static int[] requestCounts;
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java LoadBalancer <port>");
            System.exit(1);
        }
        port = Integer.parseInt(args[0]);
        requestCounts = new int[SOLR_SERVERS.length];
        for (int i = 0; i < requestCounts.length; i++) {
            requestCounts[i] = 0;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream requestBodyStream = null;
            OutputStream responseBodyStream = null;
            try {
                String requestUri = exchange.getRequestURI().toString();
                // Read request body
                requestBodyStream = exchange.getRequestBody();
                byte[] requestBodyBytes = readRequestBody(requestBodyStream);

                String solrUrl;
                if (exchange.getRequestMethod().equals("GET")) {
                    int serverIndex = getServerIndex();
                    requestCounts[serverIndex]++;
                    
                    solrUrl = SOLR_SERVERS[serverIndex] + requestUri;
                    System.out.println("Forwarding request to: " + solrUrl);
                    
                    // Forward request to Solr server
                    HttpURLConnection solrConnection = forwardRequest(solrUrl, exchange.getRequestMethod(), exchange.getRequestHeaders(), requestBodyBytes);
                    
                    // Get response from Solr server
                    int statusCode = solrConnection.getResponseCode();
                    InputStream responseStream = (statusCode >= 200 && statusCode < 300) ? solrConnection.getInputStream() : solrConnection.getErrorStream();

                    // Forward Solr response back to client
                    forwardResponse(exchange, statusCode, responseStream);
                    requestCounts[serverIndex]--;
                } 
                else {
                    solrUrl = SOLR_SERVERS[0] + requestUri;
                    System.out.println("Forwarding request to: " + solrUrl);
                    HttpURLConnection solrConnection = forwardRequest(solrUrl, exchange.getRequestMethod(), exchange.getRequestHeaders(), requestBodyBytes);
                    int statusCode = solrConnection.getResponseCode();
                    InputStream responseStream = (statusCode >= 200 && statusCode < 300) ? solrConnection.getInputStream() : solrConnection.getErrorStream();
                    forwardResponse(exchange, statusCode, responseStream);

                    for (int i = 1; i < SOLR_SERVERS.length; i++) {
                        solrUrl = SOLR_SERVERS[i] + requestUri;
                        System.out.println("Forwarding request to: " + solrUrl);
                        solrConnection = forwardRequest(solrUrl, exchange.getRequestMethod(), exchange.getRequestHeaders(), requestBodyBytes);
                        readRequestBody(solrConnection.getInputStream());
                    }
                }
                
            } 
            catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
            } 
            finally {
                if (requestBodyStream != null) {
                    requestBodyStream.close();
                }
                if (responseBodyStream != null) {
                    responseBodyStream.close();
                }
                exchange.close();
            }
        }

        private byte[] readRequestBody(InputStream requestBodyStream) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int bytesRead;
            byte[] data = new byte[1024];
            while ((bytesRead = requestBodyStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }

        private HttpURLConnection forwardRequest(String solrUrl, String requestMethod, Headers requestHeaders, byte[] requestBody) throws IOException, URISyntaxException {
            HttpURLConnection solrConnection = (HttpURLConnection) createURI(solrUrl).toURL().openConnection();
            solrConnection.setRequestMethod(requestMethod);
            
            // Forward headers
            for (Map.Entry<String, List<String>> header : requestHeaders.entrySet()) {
                for (String value : header.getValue()) {
                    solrConnection.addRequestProperty(header.getKey(), value);
                }
            }
            
            // Write request body, if present
            if (requestMethod.equals("POST")) {
                solrConnection.setDoOutput(true);
                OutputStream os = solrConnection.getOutputStream();
                os.write(requestBody);
                os.close();
            }
            
            return solrConnection;
        }

        private void forwardResponse(HttpExchange exchange, int statusCode, InputStream responseStream) throws IOException {
            exchange.sendResponseHeaders(statusCode, 0);
            OutputStream responseBodyStream = exchange.getResponseBody();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = responseStream.read(buffer)) != -1) {
                responseBodyStream.write(buffer, 0, bytesRead);
            }
            responseStream.close();
            responseBodyStream.close();
        }
        
        private URI createURI(String uriString) throws URISyntaxException {
            return new URI(uriString);
        }

        private int getServerIndex() {
            int minIndex = 0;
            for (int i = 1; i < requestCounts.length; i++) {
                if (requestCounts[i] < requestCounts[minIndex]) {
                    minIndex = i;
                }
            }
            return minIndex;
        }
    }
}
