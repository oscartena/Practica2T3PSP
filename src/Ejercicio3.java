import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<String> urls = List.of(
                "https://www.google.com",
                "https://www.youtube.com",
                "https://www.facebook.com",
                "https://www.futbin.com",
                "https://www.instagram.com",
                "https://www.whatsapp.com",
                "https://www.wikipedia.org",
                "https://www.reddit.com",
                "https://www.spotify.com",
                "https://myanimelist.net");

        // Creo un array de CompletableFuture para almacenar el contenido de las URL
        CompletableFuture<String>[] futures = urls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Configurar el cliente HTTP y realizar la solicitud GET
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .GET()
                                .build();

                        // Obtener el cuerpo de la respuesta como String
                        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                    } catch (Exception e) {
                        // Manejar cualquier excepción imprevista e imprimir la traza de errores
                        e.printStackTrace();
                        return null;
                    }
                }))
                .toArray(CompletableFuture[]::new);

        // Combino los CompletableFuture previos en una lista de contenidos para poder operar con ella
        CompletableFuture<List<String>> contenidoFuturo = CompletableFuture.allOf(futures)
                .thenApply(ignored -> List.of(futures).stream()
                        .map(CompletableFuture::join)
                        .toList());

        // Proceso asíncrono para comprimir los contenidos en un archivo ZIP
        CompletableFuture<Void> compresionFuturo = contenidoFuturo.thenAcceptAsync(contenidos -> {
            try {
                // Creo el archivo ZIP y agrego los contenidos
                String nombreZip = "paginas.zip";
                try (FileOutputStream fos = new FileOutputStream(nombreZip);
                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    for (int i = 0; i < contenidos.size(); i++) {
                        String contenido = contenidos.get(i);
                        if (contenido != null) {
                            String nombrePagina = urls.get(i).substring(8);
                            ZipEntry zipEntry = new ZipEntry(nombrePagina + ".txt");
                            zipOut.putNextEntry(zipEntry);
                            zipOut.write(contenido.getBytes());
                            zipOut.closeEntry();
                        }
                    }
                }

                System.out.println("Archivo ZIP creado: " + nombreZip);
            } catch (IOException e) {
                System.out.println("Error durante la compresión: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Esperar a que ambas tareas se completen
        CompletableFuture.allOf(contenidoFuturo, compresionFuturo).join();
    }
}
