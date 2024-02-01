import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Ejercicio1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Introduce la URL con el siguiente formato [http://web.com]: ");
        String url = sc.nextLine();

        // Crear un cliente HTTP y una solicitud para la URL proporcionada
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Crear un CompletableFuture para realizar la solicitud HTTP de forma asíncrona
        CompletableFuture<String> futuro = CompletableFuture.supplyAsync(() -> {
            try {
                // Realizar la solicitud HTTP y obtener el contenido de la respuesta
                return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).thenApply((contenido) -> {
            // Imprimir el contenido de la página una vez que se completa la solicitud
            System.out.println("El contenido de la página es: \n" + contenido);
            return contenido;
        });

        // Esperar a que el CompletableFuture se complete
        futuro.join();
    }
}
