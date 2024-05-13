package service

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.KotlinModule
import entity.Coordinate
import java.io.IOException
import kotlin.jvm.Throws
/**
 * Custom key serializer for serializing Coordinate objects into a JSON-compatible String.
 *
 * This class uses Jackson ObjectMapper with KotlinModule for serialization.
 */
class CoordinateMapKeySerializer: JsonSerializer<Coordinate>() {
    private val kMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())


    //val serializedCoordinate = kMapper.writeValueAsString(coordinates)

    /**
     * Serializes a Coordinate object into a JSON representation and writes it to a JsonGenerator.
     *
     * @param value The Coordinate object to be serialized.
     * @param gen The JsonGenerator used for writing JSON content.
     * @param serializers The SerializerProvider for accessing additional serialization functionality.
     * @throws IOException If an I/O error occurs during serialization.
     */

    @Throws(IOException::class)
    override fun serialize(value: Coordinate?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.let { jGen ->
            value?.let { coordinate ->
                jGen.writeFieldName(kMapper.writeValueAsString(coordinate))
            } ?: jGen.writeNull()

        }
        //gen?.writeFieldName("${value?.row},${value?.column}")
    }

}