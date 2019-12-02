package dk.sdu.carbonwatcher.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProducingCarbonData(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: String,
    val country: String,
    val carbonPerKilo: Double
)

@Entity
data class TransportationCarbon(
    @PrimaryKey(autoGenerate = true)
    val transport_id: Long,
    val countryFrom: String,
    val countryTo: String,
    val carbonPerKilo: Double
)


@Entity
data class ProductTypes(
    @PrimaryKey(autoGenerate = true)
    val type_id: Long,
    val name: String,
    val barcode: Long,
    val weight: Long,
    @Embedded
    val productDataz: ProducingCarbonData
)
