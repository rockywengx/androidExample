package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(with = OrderSerializer::class)
data class Order(
    var name: String,
    var price: Int,
    var quantity: Int,
    @Contextual @Serializable(with = LocalDateSerializer::class) var date: LocalDate? = null
)

class ObjectCloneTest {

    @Test
    fun testClone() {
        val order = Order("Order1", 100, 1)
        val clonedOrder = order.deepCopy()
        assertEquals(order, clonedOrder)
    }

    @Test
    fun testCloneWithDifferentValues() {
        val order = Order("Order1", 100, 1)
        val clonedOrder = order.deepCopy()
        clonedOrder.name = "Order2"
        clonedOrder.price = 200
        clonedOrder.quantity = 2
        assertNotEquals(order, clonedOrder)
    }

    @Test
    fun testCloneWithDate() {
        val order = Order("Order1", 100, 1, LocalDate.of(2023, 10, 1))
        val clonedOrder = order.deepCopy()
        assertEquals(order, clonedOrder)
    }

    @Test
    fun testCloneWithDuplicateProperties() {
        val order1 = Order("Order1", 100, 1)
        val order2 = order1.deepCopy()
        assertEquals(order1, order2)
    }
}

inline fun <reified T> T.deepCopy(): T {
    val jsonString = Json { encodeDefaults = true }.encodeToString(this)
    return Json.decodeFromString(jsonString)
}

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

object OrderSerializer : KSerializer<Order> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Order") {
        element<String>("name")
        element<Int>("price")
        element<Int>("quantity")
        element<LocalDate?>("date")
    }

    @ExperimentalSerializationApi
    override fun serialize(encoder: Encoder, value: Order) {
        val compositeEncoder = encoder.beginStructure(descriptor)
        compositeEncoder.encodeStringElement(descriptor, 0, value.name)
        compositeEncoder.encodeIntElement(descriptor, 1, value.price)
        compositeEncoder.encodeIntElement(descriptor, 2, value.quantity)
        compositeEncoder.encodeNullableSerializableElement(descriptor, 3, LocalDateSerializer, value.date)
        // 故意添加重複的屬性
        // compositeEncoder.encodeStringElement(descriptor, 0, value.name)
        compositeEncoder.endStructure(descriptor)
    }

    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): Order {
        val dec = decoder.beginStructure(descriptor)
        var name: String? = null
        var price: Int? = null
        var quantity: Int? = null
        var date: LocalDate? = null

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> name = dec.decodeStringElement(descriptor, 0)
                1 -> price = dec.decodeIntElement(descriptor, 1)
                2 -> quantity = dec.decodeIntElement(descriptor, 2)
                3 -> date = dec.decodeNullableSerializableElement(descriptor, 3, LocalDateSerializer)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unknown index $index")
            }
        }
        dec.endStructure(descriptor)

        return Order(
            name ?: throw SerializationException("Missing value for name"),
            price ?: throw SerializationException("Missing value for price"),
            quantity ?: throw SerializationException("Missing value for quantity"),
            date
        )
    }
}
