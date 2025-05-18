package ec.edu.utpl.carrera.compu.progava;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

public class UrlProcessor {

    /**
     * Este programa procesa un archivo de texto llamado "urls.txt" que contiene una lista de URLs.
     * Para cada URL, cuenta cuántos enlaces internos (del mismo dominio) están presentes.
     * Luego, genera un archivo CSV llamado "report.csv" con el URL y la cantidad de URLs internas encontradas.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Leer las URLs desde el archivo
        List<String> urls = Files.readAllLines(Paths.get("urls.txt"));
        Map<String, Integer> urlCountMap = Collections.synchronizedMap(new HashMap<>());

        // Crear lista de hilos virtuales
        List<Thread> threads = new ArrayList<>();

        // Iniciar hilos para procesar cada URL
        for (String url : urls) {
            Thread thread = Thread.ofVirtual().start(() -> {
                int count = (int) urls.stream()
                        .filter(link -> isInternalLink(url, link))
                        .count();
                urlCountMap.put(url, count);
            });
            threads.add(thread);
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // Escribir el reporte CSV
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("report.csv"))) {
            writer.write("URL,Internal URLs");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : urlCountMap.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    /**
     * Verifica si un enlace es interno al comparar su dominio con el dominio base.
     * @param baseUrl URL base que se está procesando.
     * @param link URL a comparar.
     * @return true si el link es interno, false de lo contrario.
     */
    private static boolean isInternalLink(String baseUrl, String link) {
        try {
            URI baseUri = new URI(baseUrl);
            URI linkUri = new URI(link);
            return baseUri.getHost() != null && baseUri.getHost().equals(linkUri.getHost());
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
