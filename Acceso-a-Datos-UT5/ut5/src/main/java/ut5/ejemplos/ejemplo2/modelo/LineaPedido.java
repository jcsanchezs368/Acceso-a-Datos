package ut5.ejemplos.ejemplo2.modelo;

import org.bson.types.ObjectId;

import java.math.BigDecimal;

public record LineaPedido(ObjectId productoId, String nombre, BigDecimal precioUnitario, int cantidad) {
    public LineaPedido {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");
    }

    // Conservamos el precio de la venta aunque cambie después en el catálogo.
    public LineaPedido(Producto producto, int cantidad) {
        this(producto.id(), producto.nombre(), producto.precio(), cantidad);
    }
}
