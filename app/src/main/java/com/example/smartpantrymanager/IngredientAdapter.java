package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter: turns each Ingredient into one row of the RecyclerView. */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    /** PantryActivity implements this to react to taps. */
    public interface OnIngredientClickListener {
        void onIngredientClick(Ingredient ingredient);
        void onDeleteClick(Ingredient ingredient);
    }

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final OnIngredientClickListener listener;

    public IngredientAdapter(OnIngredientClickListener listener) {
        this.listener = listener;
    }

    public void setIngredients(List<Ingredient> newIngredients) {
        ingredients.clear();
        ingredients.addAll(newIngredients);
        notifyDataSetChanged();
    }

    /** Creates a new row view from item_ingredient.xml */
    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    /** Fills a row with the data at this position */
    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.textName.setText(ingredient.getName());
        holder.textQuantity.setText(ingredient.getQuantityText() + " " + ingredient.getUnit());

        holder.itemView.setOnClickListener(v -> listener.onIngredientClick(ingredient));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(ingredient));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /** Holds the views of one row so findViewById is only called once per row. */
    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textQuantity;
        final Button buttonDelete;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textIngredientName);
            textQuantity = itemView.findViewById(R.id.textIngredientQuantity);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteIngredient);
        }
    }
}
