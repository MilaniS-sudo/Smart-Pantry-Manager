
package com.example.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class RecipesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView message = new TextView(this);
        message.setText("Suggested Recipes\n\nRecipes will appear here.");
        message.setTextSize(22);
        message.setGravity(Gravity.CENTER);

        setContentView(message);
    }
}
