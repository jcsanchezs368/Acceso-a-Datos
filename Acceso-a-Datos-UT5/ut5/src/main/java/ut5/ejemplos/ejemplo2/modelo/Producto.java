package ut5.ejemplos.ejemplo2.modelo;

import org.bson.types.ObjectId;

import org.bson.codecs.pojo.annotations.BsonId;
import java.math.BigDecimal;
import java.util.List;

public record Producto(@BsonId ObjectId id, String nombre, String categoria, BigDecimal precio,
                       int stock, boolean activo, Fabricante fabricante, List<String> etiquetas) {
    public Producto(String nombre, String categoria, BigDecimal precio, int stock,
                    Fabricante fabricante, List<String> etiquetas) {
        this(new ObjectId(), nombre, categoria, precio, stock, stock > 0, fabricante, etiquetas);
    }
}
