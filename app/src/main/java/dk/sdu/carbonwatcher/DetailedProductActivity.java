package dk.sdu.carbonwatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

import dk.sdu.carbonwatcher.model.AppDatabase;
import dk.sdu.carbonwatcher.model.ProductTypes;

public class DetailedProductActivity extends AppCompatActivity {

    private AppDatabase db;

    private TextView productName;
    private TextView productScore;
    private TextView productScoreLetter;

    private String[] letters = { "A", "B", "C", "D", "E", "F" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_product);

        productName = findViewById(R.id.detailedProductName);
        productScore = findViewById(R.id.overallScoreValueLabel);
        productScoreLetter = findViewById(R.id.overallScoreLetterLabel);


        final String barcode = getIntent().getExtras().getString("barcode");
        db = AppDatabase.Companion.invoke(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                ProductTypes product = db.CarbonDao().findByBarcode(barcode);
                productName.setText(product.getName());

                Random rng = new Random();
                int score = rng.nextInt(10000) + 1000;
                int letterIndex = (int)((score / 11000f) * letters.length - 1);

                productScore.setText("" + score);
                productScoreLetter.setText(letters[letterIndex]);
            }
        }).start();

    }
}
