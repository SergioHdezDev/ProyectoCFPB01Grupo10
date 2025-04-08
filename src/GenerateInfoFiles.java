import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GenerateInfoFiles {

    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    // Constantes para vendedores
    private static final String TIPO_DOCUMENTO = "CC";
    private static final String[] NOMBRES = {"Valentina Sofía", "Emilio Ricardo", "Mateo Esteban",
            "Luciana Gabriela", "Sebastián Alejandro", "Camila Fernanda",
            "Mariana Isabel", "Daniel Eduardo", "Gabriela Juliana",
            "Andrés Felipe"};
    private static final String[] APELLIDOS = {"Torres Sánchez", "Ríos Fernández", "Jiménez Herrera",
            "Ortega Gómez", "Castro Pérez", "López Martínez",
            "Pérez Duarte", "Ruiz Montoya", "Vargas Rojas",
            "Gómez Ramírez"};

    // Constantes para productos
    private static final String[] CATEGORIAS = {"Laptop", "Camiseta", "Zapatos", "Teléfono", "Libro", "Mochila"};
    private static final String[] DESCRIPTORES = {"Premium", "Económico", "Deportivo", "Profesional", "Clásico", "Inteligente"};
    private static final String[] MARCAS = {"HP", "Nike", "Samsung", "Apple", "Adidas", "Dell"};




    public static void createVendorsFile(int vendorsCount, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            for (int i = 0; i < vendorsCount; i++) {
                String numeroDoc = String.format("%010d", random.nextInt(1000000000));
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];

                String line = String.join(";",
                        TIPO_DOCUMENTO,
                        numeroDoc,
                        nombre,
                        apellido) + System.lineSeparator();

                writer.write(line);
            }
            System.out.println("\nArchivo de vendedores generado: " + fileName);
        }
    }

    // Métodos para productos
    public static void createProductsFile(int productsCount, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            for (int i = 1; i <= productsCount; i++) {
                String idProducto = String.format("PROD%03d", i);
                String nombreProducto = generateProductName();
                String precio = String.format("%.2f", 10.0 + (990.0 * random.nextDouble()));

                String line = String.join(";",
                        idProducto,
                        nombreProducto,
                        precio) + System.lineSeparator();

                writer.write(line);
            }
            System.out.println("\nArchivo de productos generado: " + fileName);
        }
    }


    public static void ventasPorVendedor(){
        try(BufferedReader br = new BufferedReader(new FileReader("datos/vendedores.csv"))) {
            String linea;

            while((linea=br.readLine())!=null) {

                String[] columnas = linea.split(";");
                String idVendedor = columnas[1].toString();
                createSalesMenFile(random.nextInt(15), idVendedor);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void createSalesMenFile(int randomSalesCount, String id) {
        String fileName = "datos/ventas_"+id+".csv";
        Set<String> idProductosUnico = new HashSet<>();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            writer.write("ID_PRODUCTO;CANTIDAD" + System.lineSeparator());
            ArrayList<String> productos = idProductos();
            int cantidad_productos = productos.size();
            String productoId;

            for (int i = 1; i <= randomSalesCount; i++) {


                do {
                    productoId = productos.get(random.nextInt(cantidad_productos));
                } while (idProductosUnico.contains(productoId));

                idProductosUnico.add(productoId);

                String nuevaLinea = productoId+";"+random.nextInt(100);
                writer.write(nuevaLinea+System.lineSeparator());

            }
            System.out.println("Archivo de ventas generado: " + fileName);
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public static ArrayList<String> idProductos(){
        String fileName = "datos/productos.csv";
        ArrayList<String> ids = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String linea;

            while((linea = br.readLine())!=null){

                String[] columnas = linea.split(";");
                ids.add(columnas[0]);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return ids;
    }

    private static String generateProductName() {
        return CATEGORIAS[random.nextInt(CATEGORIAS.length)] + " " +
                DESCRIPTORES[random.nextInt(DESCRIPTORES.length)] + " " +
                MARCAS[random.nextInt(MARCAS.length)];
    }
}
