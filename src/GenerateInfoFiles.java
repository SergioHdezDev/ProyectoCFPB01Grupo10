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


    public static void main(String[] args){
        int opcion;
        do {
            mostrarMenu();
            opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            try {
                switch(opcion) {
                    case 1:
                        createVendorsFile(10, "datos/vendedores.csv");
                        break;
                    case 2:
                        createProductsFile(15, "datos/productos.csv");
                        break;
                    case 3:
                        createVendorsFile(10, "datos/vendedores.csv");
                        createProductsFile(15, "datos/productos.csv");
                        break;
                    case 4:
                        ventasPorVendedor();
                        //createSalesReport(20, "datos/ventas.csv", "datos/vendedores.csv", "datos/productos.csv");
                        break;
                    case 5:
                        createVendorsFile(10, "datos/vendedores.csv");
                        createProductsFile(15, "datos/productos.csv");
                        ventasPorVendedor();
                        //createSalesReport(20, "datos/ventas.csv", "datos/vendedores.csv", "datos/productos.csv");
                        break;
                    case 6:
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción no válida!");
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        } while(opcion != 6);
    }

    private static void mostrarMenu() {
        System.out.println("\n=== GENERADOR DE ARCHIVOS ===");
        System.out.println("1. Generar archivo de vendedores");
        System.out.println("2. Generar archivo de productos");
        System.out.println("3. Generar archivos de vendedores y productos");
        System.out.println("4. Generar reporte de ventas por vendedor");
        System.out.println("5. Generar todos los archivos");
        System.out.println("6. Salir");
        System.out.print("Seleccione una opción: ");
    }

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

    // Método para generar reporte de ventas
    public static void createSalesReport(int salesCount, String fileName, String vendorsFile, String productsFile) throws IOException {
        // Verificamos si existen los archivos requeridos
        if (!new java.io.File(vendorsFile).exists()) {
            System.out.println("\nError: Archivo de vendedores no encontrado. Generando archivo...");
            createVendorsFile(10, vendorsFile);
        }

        if (!new java.io.File(productsFile).exists()) {
            System.out.println("\nError: Archivo de productos no encontrado. Generando archivo...");
            createProductsFile(15, productsFile);
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            // Escribir encabezado del reporte
            writer.write("ID_VENTA;FECHA;NUM_DOCUMENTO_VENDEDOR;ID_PRODUCTO;CANTIDAD;VALOR_TOTAL" + System.lineSeparator());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(30);

            for (int i = 1; i <= salesCount; i++) {
                String idVenta = String.format("V%05d", i);

                // Generar fecha aleatoria en los últimos 30 días
                int randomDays = random.nextInt(31); // 0-30 días atrás
                LocalDate saleDate = today.minusDays(randomDays);
                String fecha = saleDate.format(formatter);

                // Generar vendedor aleatorio (simulamos obtener del archivo)
                String numDocVendedor = String.format("%010d", random.nextInt(1000000000));

                // Generar producto aleatorio (simulamos obtener del archivo)
                String idProducto = String.format("PROD%03d", random.nextInt(15) + 1);

                // Generar cantidad y valor total
                int cantidad = random.nextInt(10) + 1;
                double valorUnitario = 10.0 + (990.0 * random.nextDouble());
                double valorTotal = cantidad * valorUnitario;

                String line = String.join(";",
                        idVenta,
                        fecha,
                        numDocVendedor,
                        idProducto,
                        String.valueOf(cantidad),
                        String.format("%.2f", valorTotal)) + System.lineSeparator();

                writer.write(line);
            }
            System.out.println("\nReporte de ventas generado: " + fileName);
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
            System.out.println("\nArchivo de ventas generado: " + fileName);
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
