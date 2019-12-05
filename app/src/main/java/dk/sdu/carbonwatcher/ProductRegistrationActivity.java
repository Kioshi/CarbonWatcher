package dk.sdu.carbonwatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import dk.sdu.carbonwatcher.model.AppDatabase;
import dk.sdu.carbonwatcher.model.ProducingCarbonData;
import dk.sdu.carbonwatcher.model.ProductTypes;

public class ProductRegistrationActivity extends AppCompatActivity {

    private EditText barcodeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_registration);

        Intent intent = getIntent();
        String barcode = intent.getExtras().getString("barcode");
        if (barcode != null) {
            barcodeField = findViewById(R.id.registerProductBarcodeText);
            barcodeField.setText(barcode);
        }
    }

    public void scanClicked(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void submitClicked(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.Companion.invoke(ProductRegistrationActivity.this);

                TextView name = findViewById(R.id.registerProductNameLabel);
                TextView type = findViewById(R.id.registerProductTypeLabel);
                TextView barcode = findViewById(R.id.registerProductBarcodeLabel);
                TextView weight = findViewById(R.id.registerProductWeightLabel);
                TextView country = findViewById(R.id.registerProductCountryLabel);

                ProducingCarbonData producing = db.CarbonDao().findType("ES","Tomatoes");

                db.CarbonDao().insertAll(new ProductTypes(0, name.getText().toString(), barcode.getText().toString(), Long.parseLong(weight.getText().toString()), producing));


                Intent intent = new Intent(ProductRegistrationActivity.this, DetailedProductActivity.class);
                intent.putExtra("barcode", barcode.getText().toString());
                ProductRegistrationActivity.this.startActivity(intent);
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            String contents = result.getContents();
            if(contents != null)
            {
                barcodeField.setText(contents);
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
}
