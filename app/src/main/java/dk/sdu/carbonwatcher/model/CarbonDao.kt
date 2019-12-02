package dk.sdu.carbonwatcher.model

import androidx.room.*


@Dao
interface CarbonDao {

    // Countries

    @Query("SELECT DISTINCT country FROM ProducingCarbonData")
    fun getCountries(): Array<String>?

    @Query("SELECT DISTINCT type FROM ProducingCarbonData")
    fun getTypes(): Array<String>?

    // Data

    @Insert
    fun insertAllData(vararg data: ProducingCarbonData)

    @Delete
    fun deleteData(data: ProducingCarbonData)

    @Query("SELECT * FROM ProducingCarbonData WHERE country = :country AND type = :type")
    fun findType(country: String, type: String): ProducingCarbonData?

    //Types

    @Query("SELECT * FROM ProductTypes WHERE name LIKE :name")
    fun findByName(name: String): ProductTypes?

    @Query("SELECT * FROM ProductTypes WHERE barcode LIKE :barcode")
    fun findByBarcode(barcode: String): ProductTypes?

    @Insert
    fun insertAll(vararg data: ProductTypes)

    @Delete
    fun delete(data: ProductTypes)

    @Update
    fun update(vararg data: ProductTypes)


    // Path

    @Insert
    fun insertAllPath(vararg data: TransportationCarbon)

    @Delete
    fun deletePath(data: TransportationCarbon)

    @Update
    fun updatePath(vararg data: TransportationCarbon)

    @Query("SELECT * FROM TransportationCarbon WHERE countryFrom LIKE :countryFrom AND countryTo LIKE :countryTo")
    fun findByPath(countryFrom: String, countryTo: String): TransportationCarbon?
}