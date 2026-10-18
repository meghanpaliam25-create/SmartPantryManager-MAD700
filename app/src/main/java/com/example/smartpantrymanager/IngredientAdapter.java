
package com.example.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class IngredientAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> ingredients;

    public IngredientAdapter(
            Context context,
            ArrayList<String> ingredients
    ) {
        super(context, 0, ingredients);

        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {

        View itemView = convertView;

        // Create the ingredient card layout
        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                    .inflate(
                            R.layout.ingredient_item,
                            parent,
                            false
                    );
        }

        // Connect the TextViews
        TextView txtIngredientName =
                itemView.findViewById(R.id.txtIngredientName);

        TextView txtIngredientQuantity =
                itemView.findViewById(R.id.txtIngredientQuantity);

        TextView txtIngredientExpiry =
                itemView.findViewById(R.id.txtIngredientExpiry);

        // Get ingredient information
        String ingredientInfo = ingredients.get(position);

        // The existing MainActivity stores ingredient
        // information as multiple lines of text.
        String[] lines = ingredientInfo.split("\n");

        // Display ingredient name
        if (lines.length > 0) {
            txtIngredientName.setText(lines[0]);
        } else {
            txtIngredientName.setText("Unknown Ingredient");
        }

        // Display quantity
        if (lines.length > 1) {
            txtIngredientQuantity.setText(lines[1]);
            txtIngredientQuantity.setVisibility(View.VISIBLE);
        } else {
            txtIngredientQuantity.setText("");
            txtIngredientQuantity.setVisibility(View.GONE);
        }

        // Display expiry date
        if (lines.length > 2) {
            txtIngredientExpiry.setText(lines[2]);
        } else {
            txtIngredientExpiry.setText(
                    "No expiry date specified"
            );
        }

        return itemView;
    }
}
