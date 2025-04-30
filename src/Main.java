import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static ArrayList<Map<String,Object>> empleados = new ArrayList<>();
    private static ArrayList<Map<String,Object>> productos = new ArrayList<>();

    public static void main(String[] args) {
        String rutaCarpetaDatos = "datos";
        File carpeta = new File(rutaCarpetaDatos);

        if(!carpeta.exists()){
           carpeta.mkdirs();
        }else{
            System.out.println("No se pudo crear la carpeta de datos!");
        }

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
                        generarReporteVentasProductos();
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
                writer.write(ventas.get("nombre_vendedor") + ";" + formatearDecimales((Double)ventas.get("total_ventas")));
                writer.newLine();
            }
        }

        System.out.println("Reporte generado: datos/reporte_ventas_por_vendedor.csv");
    }

    public static void generarReporteVentasProductos() throws IOException{
        ArrayList<Map<String,Object>> ventasProductos = ventasProducto();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream("datos/reporte_ventas_por_producto.csv"), StandardCharsets.UTF_8))) {
            writer.write("CODIGO;NOMBRE;CANTIDAD;PRECIO;TOTAL");
            writer.newLine();

            ventasProductos.sort(
                    Comparator.<Map<String, Object>>comparingInt(
                            m -> Integer.parseInt(m.get("cantidad").toString())
                    ).reversed()
            );

            for (Map<String,Object> ventas : ventasProductos) {
                writer.write(ventas.get("codigo")+";"+ventas.get("nombre")+";"+ventas.get("cantidad")+";"+formatearDecimales((Double)ventas.get("precio"))+";"+formatearDecimales((Double)ventas.get("total")));
                writer.newLine();
            }
        }

        System.out.println("Reporte generado: datos/reporte_ventas_por_producto.csv");

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

    public static ArrayList<Map<String,Object>> ventasProducto() throws IOException {
        String carpetaArchivos = "datos";
        ArrayList<Map<String,Object>> ventasTotales = new ArrayList<>();
        Map<String, Integer> ventasProductos = new HashMap<>();

        try (Stream<Path> paths = Files.walk(Paths.get(carpetaArchivos))){
            List<Path> archivosCsv = paths
                    .filter(Files::isRegularFile)
                    .filter(p->p.getFileName().toString().startsWith("ventas_") && p.getFileName().toString().endsWith(".csv"))
                    .collect(Collectors.toList());

            for(Path archivo: archivosCsv){

                try (BufferedReader br = new BufferedReader(new FileReader(archivo.toFile()))){
                    String linea;
                    boolean primeraLinea = true;

                    while((linea=br.readLine()) != null) {
                        if(primeraLinea) {
                            primeraLinea = false;
                            continue;
                        }
                        String[] partes = linea.split(";");
                        String codigo = partes[0].trim();
                        int cantidad = Integer.parseInt(partes[1]);
                        ventasProductos.merge(codigo, cantidad, Integer::sum);
                    }
                }
            }

        }


        productos = obtenerProductos();

        for (Map<String, Object>producto : productos){
            Map<String, Object> v = new HashMap<>();
            double precio = obtenerPrecio(producto.get("codigo").toString());
            int cantidad = ventasProductos.getOrDefault(producto.get("codigo"),0);

            v.put("codigo",producto.get("codigo"));
            v.put("nombre",producto.get("nombre"));
            v.put("cantidad",cantidad);
            v.put("precio",precio);
            v.put("total",cantidad*precio);
            ventasTotales.add(v);

        }
        return ventasTotales;


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

    public static String formatearDecimales(double valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#.##", symbols);

        String valorFormateado = df.format(valor); // devuelve "2500,75"
        return valorFormateado;
    }

}
