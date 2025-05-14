package com.bodakesatish.sandhyasbeautyservices.data.source.local.entity

// Define this data class in your data layer (e.g., within a 'models' or 'entities' package)
data class ServiceDetailWithServiceData(
    // Fields from ServiceDetail (assuming a ServiceDetail entity exists)
    val id: Int, // Assuming ServiceDetail has an ID
    val appointmentId: Int,
    val serviceId: Int,
    //val quantity: Int, // Example field from ServiceDetail
    val amount: Double, // Example field from ServiceDetail (price specifically for this appointment)

    // Fields from Service (assuming a Service entity exists)
    val serviceName: String,
    val normalPrice: Double // Example field from Service
    // Add any other fields from Service you select in the query
)