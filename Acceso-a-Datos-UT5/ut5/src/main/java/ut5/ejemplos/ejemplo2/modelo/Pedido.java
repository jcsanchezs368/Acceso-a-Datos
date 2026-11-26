package ut5.ejemplos.ejemplo2.modelo;

import org.bson.types.ObjectId;

import org.bson.codecs.pojo.annotations.BsonId;
import java.util.List;
import java.util.Date;

public record Pedido(@BsonId ObjectId id, ObjectId clienteId, Date fecha, String estado, List<LineaPedido> lineas) {
    public Pedido {
        if (lineas.isEmpty()) throw new IllegalArgumentException("El pedido debe tener líneas");
    }
}
