package ut5.ejemplos.ejemplo2.modelo;

import org.bson.types.ObjectId;

import org.bson.codecs.pojo.annotations.BsonId;
import java.util.List;

public record Cliente(@BsonId ObjectId id, String nombre, String email, List<Direccion> direcciones) {
}
