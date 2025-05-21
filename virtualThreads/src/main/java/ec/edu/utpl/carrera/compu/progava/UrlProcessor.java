package ec.edu.utpl.carrera.compu.progava;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class UrlProcessor {

    public static int process(String urlString) throws IOException {
        try {
            // Handle Twitter to X.com transition
            if (urlString.contains("twitter.com")) {
                urlString = urlString.replace("twitter.com", "x.com");
            }
            
            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            // Add specific headers for Twitter/X.com
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setInstanceFollowRedirects(true);

            if (connection.getResponseCode() != 200) {
                System.out.println(urlString + " | devolviendo -1");
                return -1;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder html = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();

            Set<String> foundUrls = extractUrls(html.toString(), url.getHost());

            return foundUrls.size();

        } catch (Exception e) {
            throw new IOException("No se pudo procesar: " + urlString, e);
        }
    }

    private static Set<String> extractUrls(String html, String host) {
        Set<String> result = new HashSet<>();
        Pattern pattern = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"|src\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String link = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

            if (link.startsWith("http")) {
                result.add(link);
            } else if (link.startsWith("/")) {
                result.add("https://" + host + link);
            }
        }

        return result;
    }
}