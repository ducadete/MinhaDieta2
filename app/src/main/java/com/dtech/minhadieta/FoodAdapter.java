package com.dtech.minhadieta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodEntity> foodList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodEntity food);
    }

    public FoodAdapter(List<FoodEntity> foodList, OnItemClickListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        holder.bind(foodList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateData(List<FoodEntity> newFoodList) {
        this.foodList = newFoodList;
        notifyDataSetChanged();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFoodName;
        private final TextView tvFoodDetails;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDetails = itemView.findViewById(R.id.tvFoodDetails);
        }

        public void bind(final FoodEntity food, final OnItemClickListener listener) {
            tvFoodName.setText(food.name);
            // LINHA CORRIGIDA: Agora mostra um texto padrão, pois não temos mais a informação de "porção".
            tvFoodDetails.setText(String.format("%d kcal por 100g", food.calories));
            itemView.setOnClickListener(v -> listener.onItemClick(food));
        }
    }
}