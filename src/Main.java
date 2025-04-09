import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Main {

    private static ArrayList<Map<String,Object>> empleados = new ArrayList<>();
    private static ArrayList<Map<String,Object>> productos = new ArrayList<>();

    public static void main(String[] args) {
        menu();

    }
    public static void menu() {
        Scanner scanner = new Scanner(System.in);
        int opcion;
        do {
            System.out.println("\n1. Generar archivos base");
            System.out.println("2. Generar reporte de ventas");
            System.out.println("3. Salir");
            System.out.print("\nDigite opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion){
                case 1:
                    generarArchivosBase();
                    break;
                case 2:
                    try{
                        generarReportesVentas();
                    } catch (IOException e) {
                        System.out.println("Se presentó un error al generar el reporte de ventas");
                    }

                    break;
                case 3:
                    System.out.println("saliendo...");
                    break;
                default:
                    System.out.println("Opción no valida");
            }

        }while (opcion != 3);

    }

    public static void generarArchivosBase(){
        try{
            GenerateInfoFiles.createVendorsFile(10, "datos/vendedores.csv");
        }catch (IOException e){
            System.out.println("Se presentó un error al generar archivo de vendedores");
        }

        try{
            GenerateInfoFiles.createProductsFile(15, "datos/productos.csv");
        }catch (IOException e){
            System.out.println("Se presento un error al generar archivo de productos");
        }
        GenerateInfoFiles.ventasPorVendedor();
    }

    public static void generarReportesVentas() throws IOException{
        System.out.println("Validando archivos de ventas");
        DataValidator.validarArchivosDeVentasEnCarpeta("datos");

        // Paso 2: Si todo está bien, continuar con el procesamiento
        empleados = obtenerEmpleados();
        productos = obtenerProductos();

        ArrayList<Map<String,Object>> ventasPorVendedor = new ArrayList<>();

        for (Map<String,Object> empleado : empleados) {
            Map<String,Object> ventas = new HashMap<>();
            ventas.put("nombre_vendedor", empleado.get("nombres") + " " + empleado.get("apellidos"));
            ventas.put("total_ventas", ventasVendedor(empleado.get("numero_documento").toString()));
            ventasPorVendedor.add(ventas);
        }

        ventasPorVendedor.sort(Comparator.<Map<String,Object>>comparingDouble(m -> (Double)m.get("total_ventas")).reversed());

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream("datos/reporte_ventas_por_vendedor.csv"), StandardCharsets.UTF_8))) {

            for (Map<String,Object> ventas : ventasPorVendedor) {
                writer.write(ventas.get("nombre_vendedor") + ";" + ventas.get("total_ventas"));
                writer.newLine();
            }
        }

        System.out.println("Reporte generado: datos/reporte_ventas_por_vendedor.csv");
    }

    public static ArrayList<Map<String, Object>> obtenerEmpleados() {
        String archivoEmpleados = "datos/vendedores.csv";
        ArrayList<Map<String,Object>> empleados = new ArrayList<>();

        try (BufferedReader bf = new BufferedReader(new FileReader(archivoEmpleados))) {
            String linea;
            while ((linea = bf.readLine()) != null) {
                String[] columnas = linea.split(";");
                Map<String, Object> empleado = new HashMap<>();
                empleado.put("numero_documento", columnas[1]);
                empleado.put("nombres", columnas[2]);
                empleado.put("apellidos", columnas[3]);
                empleados.add(empleado);
            }
        } catch (IOException e) {
            System.out.println(" El archivo de empleados no existe!");
        }

        return empleados;
    }

    public static ArrayList<Map<String, Object>> obtenerProductos() {
        String archivoProductos = "datos/productos.csv";
        ArrayList<Map<String,Object>> productos = new ArrayList<>();

        try (BufferedReader bf = new BufferedReader(new FileReader(archivoProductos))) {
            String linea;
            while ((linea = bf.readLine()) != null) {
                String[] columnas = linea.split(";");
                Map<String, Object> producto = new HashMap<>();
                producto.put("codigo", columnas[0]);
                producto.put("nombre", columnas[1]);
                producto.put("precio", columnas[2]);
                productos.add(producto);
            }
        } catch (IOException e) {
            System.out.println(" El archivo de productos no existe!");
        }

        return productos;
    }

    public static double ventasVendedor(String cedula) {
        double ventas = 0.0;

        try (BufferedReader bf = new BufferedReader(new FileReader("datos/ventas_" + cedula + ".csv"))) {
            String linea;
            int numLinea = 0;
            while ((linea = bf.readLine()) != null) {
                if (numLinea == 0) {
                    numLinea++;
                    continue;
                }
                String[] columnas = linea.split(";");
                String codigoProducto = columnas[0];
                int cantidad = Integer.parseInt(columnas[1]);
                double precio = obtenerPrecio(codigoProducto);
                ventas += (cantidad * precio);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ventas;
    }

    public static double obtenerPrecio(String codigoProducto) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));

        for (Map<String,Object> producto : productos) {
            if (producto.get("codigo").toString().equals(codigoProducto)) {
                try {
                    Number number = nf.parse(producto.get("precio").toString());
                    return number.doubleValue();
                } catch (ParseException e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }
}
