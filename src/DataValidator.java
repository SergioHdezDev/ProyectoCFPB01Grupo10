import java.io.*;

public class DataValidator {

    public static boolean validarArchivo(String rutaArchivo) {
        boolean valido = true;
        int numeroLinea = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;

                // Saltar encabezado
                if (numeroLinea == 1) continue;

                String[] columnas = linea.split(";");

                if (columnas.length != 2) {
                    System.out.println("❌ Línea " + numeroLinea + " no tiene 2 columnas: " + linea);
                    valido = false;
                    continue;
                }

                String idProducto = columnas[0].trim();
                String cantidadStr = columnas[1].trim();

                if (idProducto.isEmpty()) {
                    System.out.println("❌ Línea " + numeroLinea + ": El ID del producto está vacío.");
                    valido = false;
                }

                try {
                    int cantidad = Integer.parseInt(cantidadStr);
                    if (cantidad < 0) {
                        System.out.println("❌ Línea " + numeroLinea + ": Cantidad negativa.");
                        valido = false;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Línea " + numeroLinea + ": Cantidad no es un número válido.");
                    valido = false;
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Error al leer el archivo: " + e.getMessage());
            return false;
        }

        return valido;
    }

    public static void validarArchivosDeVentasEnCarpeta(String rutaCarpeta) {
        File carpeta = new File(rutaCarpeta);
        File[] archivos = carpeta.listFiles();

        if (archivos == null) {
            System.out.println("❌ No se pudo acceder a la carpeta: " + rutaCarpeta);
            return;
        }

        for (File archivo : archivos) {
            String nombre = archivo.getName().toLowerCase();
            if (archivo.isFile() && nombre.startsWith("ventas_") && nombre.endsWith(".csv")) {
                System.out.println("📄 Validando archivo: " + archivo.getName());
                boolean valido = validarArchivo(archivo.getAbsolutePath());
                if (valido) {
                    System.out.println("✅ " + archivo.getName() + " es válido.");
                } else {
                    System.out.println("⚠️ " + archivo.getName() + " contiene errores.");
                }
            } else {
                System.out.println("⏩ Ignorando archivo: " + archivo.getName());
            }
        }
    }
}
