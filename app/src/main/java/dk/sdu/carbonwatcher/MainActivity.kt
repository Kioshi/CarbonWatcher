package dk.sdu.carbonwatcher

import android.content.Intent
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout.VERTICAL
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import dk.sdu.carbonwatcher.model.AppDatabase
import dk.sdu.carbonwatcher.model.ProducingCarbonData
import dk.sdu.carbonwatcher.model.ProductTypes
import dk.sdu.carbonwatcher.model.TransportationCarbon
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import java.util.*
import kotlin.math.max


class MainActivity : AppCompatActivity() {
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase(this)

        val countryName = resources.configuration.locale.displayCountry

        val locales = Locale.getAvailableLocales()
        val countries = ArrayList<Locale>()
        val countriesStr = ArrayList<String>()
        for (locale in locales) {
            val country = locale.displayCountry
            if (country.trim({ it <= ' ' }).length > 0 && !countriesStr.contains(country)) {
                countriesStr.add(country)
                countries.add(locale)
            }
        }
        Collections.sort(countriesStr)
        Collections.sort(countries, Comparator<Locale> { p0, p1 -> p0.displayCountry.compareTo(p1.displayCountry) })

        countrySpinner.adapter = LocaleAdapter(this, android.R.layout.simple_spinner_item, countries)
        countrySpinner.setSelection(max(0,countriesStr.indexOf(countryName)))

        GlobalScope.launch {
            db.clearAllTables()

            val denmarkTomato = ProducingCarbonData(0, "Tomato", "DK", 10.3)
            val spainTomato = ProducingCarbonData(0, "Tomato", "IT", 2.4)
            val denmarkOrange = ProducingCarbonData(0, "Orange", "DK", 17.3)
            val spainOrange = ProducingCarbonData(0, "Orange", "IT", 4.4)

            db.CarbonDao().insertAllData(denmarkTomato, spainTomato, denmarkOrange, spainOrange)
            db.CarbonDao().insertAll(ProductTypes(1, "Sunmatos", 123456, 1000, spainTomato),
                ProductTypes(2, "Rainmatos", 654321, 1000, denmarkTomato))

            db.CarbonDao().insertAllPath(TransportationCarbon(0, "DK", "DK", 0.8),
                TransportationCarbon(0, "IT", "DK", 17.3),
                TransportationCarbon(0, "DK", "IT", 17.3),
                TransportationCarbon(0, "IT", "CZ", 12.4),
                TransportationCarbon(0, "CZ", "IT", 12.4))
        }
    }


    fun findClick(v: View)
    {
        if (nameET.text.isBlank() && barcodeET.text.isBlank())
        {
            Toast.makeText(this, "Please fill product name or barcode!", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch {
            var item = db.CarbonDao().findByBarcode(barcodeET.text.toString())
            if (item == null)
            {
                item = db.CarbonDao().findByName(nameET.text.toString())
            }

            if (item == null)
            {
                val countryCodes = db.CarbonDao().getCountries()?.toMutableList() ?: listOf<String>()
                val locales = Locale.getAvailableLocales()
                val countries: ArrayList<Locale> = arrayListOf()
                val countriesStr = ArrayList<String>()
                for (locale in locales) {
                    if (countryCodes.contains(locale.country))
                    {
                        val country = locale.displayCountry
                        if (country.trim({ it <= ' ' }).length > 0 && !countriesStr.contains(country)) {
                            countriesStr.add(country)
                            countries.add(locale)
                        }
                    }
                }
                val typeOfProducts = db.CarbonDao().getTypes()?.toMutableList() ?: listOf<String>()
                GlobalScope.launch(Dispatchers.Main) {

                    alert {
                        title = "Could not find product!"
                        message = "To get carbon footprint please fill the informations bellow"

                        lateinit var name: EditText
                        lateinit var barcode: EditText
                        lateinit var weight: EditText
                        lateinit var country: Spinner
                        lateinit var type: Spinner
                        customView{
                            linearLayout {
                                padding = dip(20)
                                orientation  = VERTICAL
                                name = editText{
                                    hint = "Name of product"
                                    text = nameET.text
                                }
                                barcode = editText{
                                    hint = "Barcode number of product"
                                    text = barcodeET.text
                                    inputType = TYPE_CLASS_NUMBER
                                }
                                weight = editText{
                                    hint = "Weight in grams"
                                    inputType = TYPE_CLASS_NUMBER
                                }
                                country = spinner{
                                    adapter = LocaleAdapter(v.context, android.R.layout.simple_spinner_item, countries)
                                }
                                type = spinner{
                                    adapter = ArrayAdapter(v.context, android.R.layout.simple_spinner_item, typeOfProducts)
                                }


                            }
                        }

                        positiveButton("Calculate")
                        {
                            nameET.text = name.text
                            barcodeET.text = barcode.text
                            it.dismiss()
                            GlobalScope.launch {
                                val productType = db.CarbonDao().findType((country.selectedItem as Locale).country, type.selectedItem.toString());
                                if (productType != null)
                                {
                                    db.CarbonDao().insertAll(ProductTypes(0, name.text.toString(), barcode.text.toString().toLong(), weight.text.toString().toLong(), productType))

                                }
                                GlobalScope.launch(Dispatchers.Main) {
                                    if (productType != null)
                                    {
                                        findClick(v)
                                    }
                                    else
                                    {
                                        Toast.makeText(v.context,"Could not find data about product!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }

                        negativeButton("Cancel")
                        {
                            it.dismiss()
                        }

                    }.show()
                }
            }
            else
            {
                val transportCarbon = db.CarbonDao().findByPath((countrySpinner.selectedItem as Locale).country, item.productDataz.country)
                GlobalScope.launch(Dispatchers.Main) {
                    if (transportCarbon == null)
                    {
                        Toast.makeText(v.context,"Could not find data about product!", Toast.LENGTH_LONG).show()
                    }
                    else
                    {
                        alert {
                            title = "Carbon footprint for this item is:"
                            message = "${transportCarbon.carbonPerKilo + item.weight/1000.0*item.productDataz.carbonPerKilo}"

                            yesButton {

                            }
                        }.show()
                    }
                }
            }
        }
    }


    fun cameraClick(v: View)
    {
        run {
            IntentIntegrator(this@MainActivity).initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null)
        {

            if(result.contents != null)
            {
                barcodeET.setText(result.contents)
            } else
            {
                Toast.makeText(this, "Scan failed, please try again or fill the barcode number manually!", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
