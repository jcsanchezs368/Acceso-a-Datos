package ut5.ejemplos.ejemplo2;

import ut5.ejemplos.ejemplo2.modelo.Cliente;
import ut5.ejemplos.ejemplo2.modelo.Direccion;
import ut5.ejemplos.ejemplo2.modelo.Fabricante;
import ut5.ejemplos.ejemplo2.modelo.LineaPedido;
import ut5.ejemplos.ejemplo2.modelo.Pedido;
import ut5.ejemplos.ejemplo2.modelo.Producto;
import ut5.ejemplos.ejemplo2.modelo.ResumenProductoDto;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Updates.*;

public class EjemploTienda {

  public static void main(String[] args) {
    MongoClient conexion = null;
    int estado = 0;
    try {
      // 1. Crear conexión con el servidor
      conexion = crearConexion1("localhost", 27017);

      // 2. Obtener la base de datos y comprobar la conexión
      MongoDatabase db = conexion.getDatabase("ad");
      db.runCommand(new Document("ping", 1));
      System.out.println("Conexión comprobada. Base de datos: " + db.getName());

      // 3. Obtener las referencias a las colecciones tipadas
      MongoCollection<Producto> productos = db.getCollection("productos", Producto.class);
      MongoCollection<Cliente> clientes = db.getCollection("clientes", Cliente.class);
      MongoCollection<Pedido> pedidos = db.getCollection("pedidos", Pedido.class);

      // 4. Vaciar las tres colecciones antes de preparar los datos
      borrarDocumentosIniciales4(productos, clientes, pedidos);

      // 5. Insertar cinco productos y cinco clientes
      insertarDocumentosIniciales5(productos, clientes);

      // ------- DOCUMENTOS QUE UTILIZAREMOS -------
      Producto teclado = productos.find(eq("nombre", "Teclado mecánico")).first();
      Producto raton = productos.find(eq("nombre", "Ratón inalámbrico")).first();
      Cliente ana = clientes.find(eq("email", "ana@example.com")).first();
      // -------------------------------------------

      // 6. Insertar un pedido con referencias y líneas embebidas
      List<LineaPedido> lineas = List.of (
        new LineaPedido(raton, 2),
        new LineaPedido(teclado, 1)
      );

      insertarPedido6(pedidos, ana, lineas);
      // 7. Consultar con filtros, proyección, ordenación e into
      List<ResumenProductoDto> resultados = buscarProductosPerifericosDisponiblesOrdenadosPorPrecio7(productos, new BigDecimal("200"));
      System.out.println(resultados);
      // 8. Consultar documentos anidados y arrays
      System.out.println("PRODUCTOS DE ESPAÑA Y CON ETIQUETA USB");
      System.out.println(buscarProductosPorNacionalidadYEtiqueta8(productos, "España", "usb"));
      // 9. Contar documentos y tratar una búsqueda sin resultados
      System.out.println("PRODUCTOS SIN STOCK O INACTIVOS");
      System.out.println(buscarProductosSinStockOInactivos10(productos));
      // 10. Combinar filtros con or

      // 11. Seleccionar categorías con in
      System.out.println("PRODUCTOS DE LAS CATEGORÍAS PERIFÉRICOS Y ORDENADORES");
      System.out.println(buscarProductosPorCategorias11(productos, List.of("Periféricos", "Ordenadores")));
      // 12. Comprobar la presencia de un campo con exists
      System.out.println("PRODUCTOS CON FABRICANTE CONCIDO");
      System.out.println(buscarProductosConFabricanteConocido12(productos));
      // 13. Obtener los tres productos más caros con sort y limit

      // 14. Contar documentos que cumplen un filtro

      // 15. Incrementar el stock con inc

      // 16. Añadir una etiqueta sin duplicados con addToSet

      // 17. Cambiar nombre y precio conjuntamente con combine

      // 18. Actualizar el estado del pedido

      // 19. Actualizar varios documentos con updateMany

      // 20. Eliminar un campo anidado con unset

      // 21. Añadir y retirar elementos de un array con push y pull

      // 22. Borrar todos los productos agotados e inactivos
      
    } catch (MongoException | IllegalArgumentException e) {
      System.err.println("No se han completado los ejemplos: " + e.getMessage());
      estado = 1;
    }

    if (conexion != null) {
      conexion.close();
    }
    if (estado != 0) {
      System.exit(estado);
    }
  }

  /**
   * Crea un cliente de MongoDB con la dirección y el puerto indicados. El código
   * que lo recibe debe cerrarlo; la creación no comprueba por sí sola el acceso
   * al servidor.
   *
   * @param direccion dirección del servidor MongoDB
   * @param puerto    puerto del servidor MongoDB
   * @return cliente configurado para acceder al servidor
   */
  public static MongoClient crearConexion1(String direccion, int puerto) {
    return MongoClients.create("mongodb://" + direccion + ":" + puerto);
  }

  /**
   * Elimina todos los documentos de pedidos, clientes y productos mediante un
   * filtro vacío. Conserva las colecciones y sus índices.
   *
   * @param productos colección tipada de productos
   * @param clientes  colección tipada de clientes
   * @param pedidos   colección tipada de pedidos
   */
  public static void borrarDocumentosIniciales4(MongoCollection<Producto> productos,
      MongoCollection<Cliente> clientes, MongoCollection<Pedido> pedidos) {
    // el empty() es un filtro que afecta a todos los documentos de la colección
    pedidos.deleteMany(empty());
    clientes.deleteMany(empty());
    productos.deleteMany(empty());
  }

  /**
   * Inserta cinco productos y cinco clientes con sus direcciones embebidas. No
   * vacía las colecciones ni inserta pedidos; utiliza insertMany en cada
   * colección.
   *
   * @param productos colección tipada de productos
   * @param clientes  colección tipada de clientes
   */
  public static void insertarDocumentosIniciales5(MongoCollection<Producto> productos,
      MongoCollection<Cliente> clientes) {
    List<Producto> nuevosProductos = new ArrayList<>(List.of(
        new Producto("Teclado mecánico", "Periféricos", new BigDecimal("59.90"),
            25, new Fabricante("TecnoSur", "España"), List.of("usb", "oficina")),
        new Producto("Ratón inalámbrico", "Periféricos", new BigDecimal("24.50"),
            40, new Fabricante("TecnoSur", "España"), List.of("inalambrico", "oficina")),
        new Producto("Monitor 24 pulgadas", "Monitores", new BigDecimal("149.00"),
            0, new Fabricante("VistaTech", "Portugal"), List.of("hdmi", "oficina")),
        new Producto("Portátil 15 pulgadas", "Ordenadores", new BigDecimal("799.00"),
            8, new Fabricante("NovaTech", "Alemania"), List.of("portatil", "oficina")),
        new Producto("Alfombrilla de escritorio", "Accesorios", new BigDecimal("12.90"),
            30, null, List.of("escritorio", "oficina"))));
    // insertamos varios productos con insertMany()
    productos.insertMany(nuevosProductos);

    List<Cliente> nuevosClientes = new ArrayList<>(List.of(
        new Cliente(new ObjectId(), "Ana López", "ana@example.com", List.of(
            new Direccion("envio", "Calle Mayor, 10", "Valencia", "46001"),
            new Direccion("facturacion", "Calle Real, 5", "Sevilla", "41001"))),
        new Cliente(new ObjectId(), "Luis García", "luis@example.com", List.of(
            new Direccion("envio", "Calle Sol, 8", "Madrid", "28001"))),
        new Cliente(new ObjectId(), "Marta Ruiz", "marta@example.com", List.of(
            new Direccion("envio", "Plaza Mayor, 3", "Valencia", "46001"))),
        new Cliente(new ObjectId(), "Carlos Pérez", "carlos@example.com", List.of(
            new Direccion("envio", "Calle Luna, 12", "Barcelona", "08001"))),
        new Cliente(new ObjectId(), "Laura Martín", "laura@example.com", List.of(
            new Direccion("envio", "Avenida del Mar, 20", "Málaga", "29001")))));
    clientes.insertMany(nuevosClientes);

  }

  /**
   * Construye e inserta un pedido PENDIENTE con fecha actual, referencia al
   * cliente y las líneas recibidas. No descuenta stock ni modifica los productos.
   *
   * @param pedidos colección tipada de pedidos
   * @param cliente cliente al que se asocia el pedido
   * @param lineas  líneas del pedido; deben contener al menos un elemento
   * @return pedido insertado con su identificador
   * @throws IllegalArgumentException si la lista de líneas está vacía
   */
  public static Pedido insertarPedido6(MongoCollection<Pedido> pedidos,
      Cliente cliente, List<LineaPedido> lineas) {
    Pedido nuevoPedido = new Pedido(new ObjectId(), cliente.id(), new Date(), "PENDIENTE", lineas);
    pedidos.insertOne(nuevoPedido);
    return nuevoPedido;
  }

  /**
   * Consulta periféricos activos con stock positivo y precio inferior al máximo recibido.
   * Proyecta nombre, precio y stock sobre ResumenProductoDto, ordena por precio y
   * recoge los resultados en una lista mediante into.
   *
   * @param productos    colección tipada de productos
   * @param precioMaximo límite superior exclusivo del precio
   * @return lista de resúmenes; vacía si no hay coincidencias
   */
  public static List<ResumenProductoDto> buscarProductosPerifericosDisponiblesOrdenadosPorPrecio7(
      MongoCollection<Producto> productos, BigDecimal precioMaximo) {
    FindIterable<ResumenProductoDto> resultados = productos.find(
      and(
        eq("categoria", "Periféricos"),
        eq("activo", true),
        gt("stock", 0),
        lt("precio", precioMaximo)
      ), ResumenProductoDto.class)
      .projection(fields(include("nombre", "precio", "stock"), excludeId())) //Solo para consultar los campos nombre, precio y stock y excluir el campo de la id
      .sort(ascending("precio")); //ordenar por precio ascendente
    return resultados.into(new ArrayList<>());
  }

  /**
   * Busca productos cuyo fabricante pertenece al país indicado y cuyo array
   * de etiquetas contiene la etiqueta recibida.
   *
   * @param productos    colección tipada de productos
   * @param nacionalidad país del fabricante
   * @param etiqueta     etiqueta que debe contener el producto
   * @return productos coincidentes, o una lista vacía
   */
  public static List<Producto> buscarProductosPorNacionalidadYEtiqueta8(
      MongoCollection<Producto> productos, String nacionalidad, String etiqueta) {
    return productos.find(
      and(
        eq("fabricante.pais", nacionalidad),
        eq("etiquetas", etiqueta)
      ), Producto.class).into(new ArrayList<Producto>());
  }

  /**
   * Combina con or las condiciones stock igual a cero y activo igual a false.
   * Basta que el producto cumpla una de ellas.
   *
   * @param productos colección tipada de productos
   * @return productos coincidentes, o una lista vacía
   */
  public static List<Producto> buscarProductosSinStockOInactivos10(MongoCollection<Producto> productos) {
        return productos.find(
      or(
        eq("stock", 0),
        eq("activo", false)
      ), Producto.class).into(new ArrayList<Producto>());
  }

  /**
   * Selecciona mediante in los productos de cualquiera de las categorías
   * recibidas.
   *
   * @param productos  colección tipada de productos
   * @param categorias categorías admitidas por el filtro
   * @return productos coincidentes, o una lista vacía
   */
  public static List<Producto> buscarProductosPorCategorias11(MongoCollection<Producto> productos,
      List<String> categorias) {
      return productos.find(in("categoria", categorias), Producto.class).into(new ArrayList<Producto>());
  }

  /**
   * Selecciona mediante exists los documentos que contienen el campo fabricante.
   * Un campo presente con valor null también cumple la condición.
   *
   * @param productos colección tipada de productos
   * @return productos con el campo presente, o una lista vacía
   */
  public static List<Producto> buscarProductosConFabricanteConocido12(MongoCollection<Producto> productos) {
    return productos.find(exists("fabricante", true), Producto.class).into(new ArrayList<Producto>());
  }

  /**
   * Busca los tres productos más caros, ordenando por precio descendente
   * y limitando el resultado mediante limit(3).
   *
   * @param productos colección tipada de productos
   * @return hasta tres productos ordenados de mayor a menor precio
   */
  public static List<Producto> buscarTresProductosMasCaros13(MongoCollection<Producto> productos) {
    // TODO
    return null;
  }

  /**
   * Consulta clientes con una dirección de envío en la ciudad recibida. elemMatch
   * exige que
   * tipo y ciudad coincidan en el mismo elemento del array.
   *
   * @param clientes colección tipada de clientes
   * @param ciudad   ciudad de la dirección de envío buscada
   * @return clientes coincidentes, o una lista vacía
   */
  public static List<Cliente> buscarClientesConEnvioACiudad8(
      MongoCollection<Cliente> clientes, String ciudad) {
    // TODO
    return null;
  }

  /**
   * Cuenta todos los documentos de la colección de productos.
   *
   * @param productos colección tipada de productos
   * @return número total de productos
   */
  public static long contarProductos9(MongoCollection<Producto> productos) {
    // TODO
    return 0;
  }

  /**
   * Cuenta los productos cuyo stock es mayor que cero utilizando un filtro en
   * countDocuments.
   *
   * @param productos colección tipada de productos
   * @return número de productos con stock positivo
   */
  public static long contarConStock14(MongoCollection<Producto> productos) {
    // TODO
    return 0;
  }

  /**
   * Incrementa el stock del producto indicado mediante inc.
   * Un incremento negativo reduce el stock.
   *
   * @param productos  colección tipada de productos
   * @param id         identificador del producto
   * @param incremento unidades que se suman al stock
   * @return número de productos modificados
   */
  public static long incrementarStockProducto15(MongoCollection<Producto> productos,
      ObjectId id, int incremento) {
    // TODO
    return 0;
  }

  /**
   * Cambia el nombre y el precio del producto agrupando dos set mediante combine.
   * La operación no modifica el nombre ni el precio histórico de las líneas de
   * pedidos.
   *
   * @param productos colección tipada de productos
   * @param id        identificador del producto
   * @param nombre    nuevo nombre del producto
   * @param precio    nuevo precio del producto
   * @return número de productos modificados
   */
  public static long cambiarNombrePrecioProducto17(MongoCollection<Producto> productos,
      ObjectId id, String nombre, BigDecimal precio) {
    // TODO
    return 0;
  }

  /**
   * Desactiva todos los productos de la categoría indicada mediante updateMany.
   * Puede encontrar documentos que ya estaban inactivos y no necesitan cambios.
   *
   * @param productos colección tipada de productos
   * @param categoria categoría de los productos que se desactivarán
   * @return número de documentos modificados
   */
  public static long desactivarProductosPorCategoria19(MongoCollection<Producto> productos, String categoria) {
    // TODO
    return 0;
  }

  /**
   * Elimina con unset el campo pais del fabricante embebido de un producto.
   * Conserva el fabricante y sus demás campos.
   *
   * @param productos colección tipada de productos
   * @param id        identificador del documento sobre el que se opera
   * @return número de documentos modificados
   */
  public static long quitarPaisFabricante20(MongoCollection<Producto> productos, ObjectId id) {
    // TODO
    return 0;
  }

  /**
   * Añade una etiqueta mediante addToSet solo si todavía no está en el array.
   * No elimina duplicados que ya existieran.
   *
   * @param productos colección tipada de productos
   * @param id        identificador del producto
   * @param etiqueta  etiqueta que se quiere añadir
   * @return número de productos modificados; cero si no existe el producto o ya
   *         contiene la etiqueta
   */
  public static long anadirNuevaEtiquetaProducto16(MongoCollection<Producto> productos,
      ObjectId id, String etiqueta) {
    // TODO
    return 0;
  }

  /**
   * Añade al final del array la etiqueta recibida mediante push. Permite
   * duplicados, a diferencia de addToSet.
   *
   * @param productos colección tipada de productos
   * @param id        identificador del documento sobre el que se opera
   * @param etiqueta  etiqueta que se añade o retira
   * @return número de documentos modificados
   */
  public static long agregarEtiqueta21(MongoCollection<Producto> productos,
      ObjectId id, String etiqueta) {
    // TODO
    return 0;
  }

  /**
   * Elimina del array todas las apariciones de la etiqueta recibida mediante
   * pull.
   *
   * @param productos colección tipada de productos
   * @param id        identificador del documento sobre el que se opera
   * @param etiqueta  etiqueta que se añade o retira
   * @return número de documentos modificados
   */
  public static long retirarEtiqueta21(MongoCollection<Producto> productos,
      ObjectId id, String etiqueta) {
    // TODO
    return 0;
  }

  /**
   * Asigna el estado recibido al pedido identificado mediante updateOne y set.
   *
   * @param pedidos colección tipada de pedidos
   * @param id      identificador del documento sobre el que se opera
   * @param estado  nuevo estado del pedido
   * @return número de documentos modificados
   */
  public static long cambiarEstadoPedido18(MongoCollection<Pedido> pedidos,
      ObjectId id, String estado) {
    // TODO
    return 0;
  }

  /**
   * Elimina todos los productos con stock cero e inactivos mediante deleteMany.
   *
   * @param productos colección tipada de productos
   * @return número de productos eliminados
   */
  public static long borrarAgotadosInactivos22(MongoCollection<Producto> productos) {
    // TODO
    return 0;
  }

}
