import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Ejercicio2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Solicitar la ruta de la carpeta a comprimir
        System.out.println("Introduce la ruta de la carpeta para comprimir:");
        String origen = sc.nextLine();

        // Solicitar la ruta de destino donde se moverá el archivo ZIP
        System.out.println("Introduce la ruta de destino donde moverlo:");
        String destino = sc.nextLine();

        // Crear un CompletableFuture para realizar la compresión y mover el archivo ZIP
        CompletableFuture.supplyAsync(() -> {
            try {
                // Nombre del archivo ZIP a crear
                String archivoZip = "comprimido.zip";

                // Crear el archivo ZIP y agregar contenidos
                try (FileOutputStream fos = new FileOutputStream(archivoZip);
                     ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                    // Llamar al método para comprimir la carpeta
                    File fileToZip = new File(origen);
                    zipFile(fileToZip, fileToZip.getName(), zipOut);
                }

                System.out.println("Archivo ZIP creado.");
                return archivoZip;

            } catch (IOException e) {
                System.out.println("Error durante la compresión: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(rutaArchivoZip -> {
            try {
                // Verificar si la ruta del archivo ZIP no es nula
                if (rutaArchivoZip != null) {
                    // Crear el directorio de destino si no existe
                    File destinoDir = new File(destino);
                    if (!destinoDir.exists()) {
                        destinoDir.mkdirs();
                    }

                    // Mover el archivo ZIP después de la creación
                    Files.move(Paths.get(rutaArchivoZip), Paths.get(destinoDir.getPath(), new File(rutaArchivoZip).getName()));
                    System.out.println("ZIP movido a " + destinoDir.getPath());
                } else {
                    System.out.println("La ruta es nula, no se encontró el archivo.");
                }
            } catch (IOException e) {
                System.out.println("Error al mover el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Método para comprimir una carpeta y sus contenidos
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();

            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }

        // Agregar un archivo al archivo ZIP
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}
