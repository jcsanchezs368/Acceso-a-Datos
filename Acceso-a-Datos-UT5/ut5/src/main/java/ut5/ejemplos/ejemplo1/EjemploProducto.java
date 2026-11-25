package ut5.ejemplos.ejemplo1;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/** Primer acceso a MongoDB: una colección y documentos con campos simples. */
public class EjemploProducto {
  public static void main(String[] args) {
    // 1. Crear conexión con el servidor
    MongoClient conexion = crearConexion("localhost", 27017);
    // 2. Obtener la referencia a la BD por su nombre (no la crea)
    MongoDatabase db = conexion.getDatabase("ad");
    // 3. Comprobación de la conexión
    
    // 4. Obtener la referencia a la colección "productos" (no la crea)
    MongoCollection productos = db.getCollection("productos");
    // 5. Insertar un producto
    ObjectId idProducto = insertarProducto(productos, "Ratón Gaming Pro Speed","perifericos", new BigDecimal("79.99"), 1,true, "Logitech", "China");


    // 6. Consultar lista de productos completa
    System.out.println(consultarProductos(productos));
    
    // 7. Consultar producto por ID
    Document producto = consultarProducto(productos, idProducto);

    System.out.println(producto.getString("nombre"));
    System.out.println(producto.get("precio"));
    System.out.println(producto.get("fabricante"));

    // 8. Actualizar stock de un producto por ID
    long numProductosActualizados = actualizarStock(productos, idProducto, 17);
    System.out.println("PRODUCTOS ACTUALIZADOS: " + numProductosActualizados);
    // 9. Eliminar producto por ID
    long numProductosEliminados = borrarProducto(productos, idProducto);
    System.out.println("PRODUCTOS ELIMINADOS: " + numProductosEliminados);
  }
  /**
   * Crea un cliente para acceder al servidor MongoDB indicado. La conexión efectiva se comprueba al ejecutar una operación; quien recibe el cliente debe cerrarlo.
   *
   * @param direccion dirección del servidor MongoDB
   * @param puerto puerto del servidor MongoDB
   * @return cliente configurado para acceder al servidor
   */
  public static MongoClient crearConexion(String direccion, int puerto) {
    
    return MongoClients.create("mongodb://"+direccion+":"+puerto);
  }

  //insertOne()
  /**
   * Construye un Document con los valores recibidos y lo inserta mediante insertOne. El precio se almacena como Decimal128, el fabricante como documento embebido y el driver genera el campo _id.
   *
   * @param productos colección de documentos de productos
   * @param nombre nombre del producto
   * @param categoria categoría del producto
   * @param precio precio decimal del producto
   * @param stock unidades disponibles
   * @param activo indica si el producto está activo
   * @param nombreFabricante nombre del fabricante embebido
   * @param paisFabricante país del fabricante embebido
   * @return identificador ObjectId del documento insertado
   */
  public static ObjectId insertarProducto(MongoCollection<Document> productos,
      String nombre, String categoria, BigDecimal precio, int stock, boolean activo,
      String nombreFabricante, String paisFabricante) {
        Document fabricante = new Document("nombre", nombreFabricante).append("pais", paisFabricante);

    Document producto = new Document("nombre", nombre)
    .append("categoria", categoria)
    .append("precio",new Decimal128(precio))
    .append("stock", stock)
    .append("activo",activo)
    .append("fabricante", fabricante);
    // insertamos el nuevo documento
    productos.insertOne(producto);
    // retornamos el valor del id del nuevo producto
    return producto.getObjectId("_id");
  }

  // find()
  /**
   * Consulta todos los productos con find y consume los resultados mediante into para almacenarlos en una lista.
   *
   * @param productos colección de documentos de productos
   * @return lista de documentos; vacía si la colección no contiene productos
   */
  public static List<Document> consultarProductos(MongoCollection<Document> productos) {
    FindIterable<Document> resultados = productos.find();
    //into() extrae los documentos del FindIterable y los mete en un array list
    List<Document> resultadosList = resultados.into(new ArrayList<Document>());
    return resultadosList;
  }

  // find().first()
  /**
   * Busca por _id utilizando un filtro de igualdad y obtiene el primer resultado mediante first.
   *
   * @param productos colección de documentos de productos
   * @param id identificador del producto buscado
   * @return documento encontrado, o null si no existe
   */
  public static Document consultarProducto(MongoCollection<Document> productos, ObjectId id) {
    // eq() - Operador "="
    // first() - consulta solo el primer resultado
    Document resultado = productos.find(eq("_id", id)).first();
    return resultado;
  }

  // updateOne()
  /**
   * Asigna el stock recibido al producto identificado mediante updateOne y set. No devuelve el documento actualizado.
   *
   * @param productos colección de documentos de productos
   * @param id identificador del producto que se modifica
   * @param stock nuevo valor del stock
   * @return número de documentos modificados: 0 o 1; puede ser 0 si no hay coincidencias o el stock ya tenía ese valor
   */
  public static long actualizarStock(MongoCollection<Document> productos, ObjectId id, int stock) {
    
    UpdateResult resultado = productos.updateOne(
      eq("_id", id),
      set("stock", stock)
    );

    return resultado.getModifiedCount();
  }

  // deleteOne()
  /**
   * Elimina como máximo un producto mediante deleteOne y un filtro de igualdad por _id. Conserva la colección y sus índices.
   *
   * @param productos colección de documentos de productos
   * @param id identificador del producto que se elimina
   * @return número de documentos eliminados: 0 o 1
   */
  public static long borrarProducto(MongoCollection<Document> productos, ObjectId id) {
    DeleteResult resultado = productos.deleteOne(eq("_id", id));
    return resultado.getDeletedCount();
  }
}