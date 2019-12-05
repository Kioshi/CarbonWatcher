package dk.sdu.carbonwatcher;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.WorkSource;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.Locale;

import dk.sdu.carbonwatcher.model.AppDatabase;
import dk.sdu.carbonwatcher.model.AppDatabase_Impl;
import dk.sdu.carbonwatcher.model.ProducingCarbonData;
import dk.sdu.carbonwatcher.model.ProductTypes;
import dk.sdu.carbonwatcher.model.TransportationCarbon;

public class FirstActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        setupDatabase();

    }

    private void loadSearchActivity() {
        Intent intent = new Intent(this, SearchResultsActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    public void searchClicked(View view) {
        loadSearchActivity();
    }

    public void scanClicked(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            final String contents = result.getContents();
            if(contents != null)
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ProductTypes product = db.CarbonDao().findByBarcode(contents);

                        FirstActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (product == null) {
                                    Toast.makeText(FirstActivity.this, "Scan found no matching product: " + contents, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(FirstActivity.this, ProductRegistrationActivity.class);
                                    intent.putExtra("barcode", contents);
                                    FirstActivity.this.startActivity(intent);
                                } else {
                                    Toast.makeText(FirstActivity.this, "Scan found : " + product.getName() + "!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
                // Toast.makeText(this, "Scan found no matching product: " + contents, Toast.LENGTH_SHORT).show();

                // GOTO product view if existing
                // ELSE ask if user wants to register product
            }
            else
            {
                Toast.makeText(this, "Scan failed, please try again or fill the barcode number manually!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupDatabase() {
        db = AppDatabase.Companion.invoke(this);

        new Thread(new Runnable() {
            @Override
            public void run() {

                db.clearAllTables();

                ProducingCarbonData danishTomatoes = new ProducingCarbonData(1, "Tomato", "DK", 10.3);
                ProducingCarbonData spanishTomatoes = new ProducingCarbonData(2, "Tomato", "ES", 2.4);
                ProducingCarbonData danishOranges = new ProducingCarbonData(3, "Orange", "DK", 17.3);
                ProducingCarbonData spanishOranges = new ProducingCarbonData(4, "Orange", "ES", 4.4);

                db.CarbonDao().insertAllData(
                        danishTomatoes,
                        spanishTomatoes,
                        danishOranges,
                        spanishOranges
                );

                db.CarbonDao().insertAll(
                        new ProductTypes(1, "Sunmatoes", "5712872292616", 1000, spanishTomatoes),
                        new ProductTypes(2, "Rainmatoes", "7310616071244", 1000, danishTomatoes)
                );

                db.CarbonDao().insertAllPath(
                        new TransportationCarbon(1, "DK", "DK", 0.8),
                        new TransportationCarbon(2, "ES", "DK", 17.3),
                        new TransportationCarbon(3, "DK", "IT", 17.3),
                        new TransportationCarbon(4, "ES", "CZ", 12.4),
                        new TransportationCarbon(5, "CZ", "IT", 12.4)
                );
            }
        }).start();
    }

}
