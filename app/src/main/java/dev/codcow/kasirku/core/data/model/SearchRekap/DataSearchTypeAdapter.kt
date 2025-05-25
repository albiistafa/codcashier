//package dev.codcow.kasirku.core.data.model.SearchRekap
//
//import com.google.gson.Gson
//import com.google.gson.TypeAdapter
//import com.google.gson.stream.JsonReader
//import com.google.gson.stream.JsonWriter
//import com.google.gson.stream.JsonToken
//import com.google.gson.reflect.TypeToken
//
///**
// * Custom Gson TypeAdapter for DataSearch to handle cases where 'data' field
// * is an empty array [] instead of an object {} when no data is found.
// */
//class DataSearchTypeAdapter(private val gson: Gson) : TypeAdapter<DataSearch>() {
//
//    // We need the default TypeAdapter for DataSearch to handle the standard case
//    // We get a delegate adapter from the Gson instance provided in the constructor.
//    private val defaultAdapter: TypeAdapter<DataSearch> =
//        gson.getDelegateAdapter(null, object : TypeToken<DataSearch>() {}.type)
//
//    // This method is used to serialize (Kotlin object to JSON) - not needed for this specific error
//    // We delegate the writing process to the default adapter.
//    override fun write(out: JsonWriter, value: DataSearch?) {
//        defaultAdapter.write(out, value)
//    }
//
//    // This method is used to deserialize (JSON to Kotlin object)
//    // This is where the custom parsing logic resides.
//    override fun read(reader: JsonReader): DataSearch? {
//        // Check the next token in the JSON stream without consuming it.
//        val peekedToken = reader.peek()
//
//        // If the next token is BEGIN_ARRAY, it means the API returned [] for 'data'.
//        if (peekedToken == JsonToken.BEGIN_ARRAY) {
//            // Consume the empty array token from the stream.
//            reader.beginArray()
//            reader.endArray()
//            // Return a default DataSearch object.
//            // This object represents the "no data found" state in a format
//            // that the rest of the application expects (an object, not an array).
//            return DataSearch(
//                pagination = Pagination(0, 0, "0", 0), // Provide default/empty pagination data
//                transactions = emptyList() // Provide an empty list of transactions
//            )
//        }
//
//        // If the next token is BEGIN_OBJECT, it means the API returned a standard object {...}.
//        // Use the default adapter to parse the object as usual.
//        if (peekedToken == JsonToken.BEGIN_OBJECT) {
//            return defaultAdapter.read(reader)
//        }
//
//        // Handle the case where the value is NULL.
//        if (peekedToken == JsonToken.NULL) {
//            reader.nextNull() // Consume the null token.
//            return null // Return null as the DataSearch object.
//        }
//
//        // For any other unexpected token, we can try to use the default adapter
//        // or throw a specific exception. Delegating to the default adapter
//        // might still result in an error if the token is truly unexpected,
//        // which is often desired behavior to indicate an API issue.
//        return defaultAdapter.read(reader) // Attempt to use default adapter for other cases
//    }
//}
