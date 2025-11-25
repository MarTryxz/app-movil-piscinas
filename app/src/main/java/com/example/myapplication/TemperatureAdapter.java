package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TemperatureAdapter extends RecyclerView.Adapter<TemperatureAdapter.ViewHolder> {

    private List<TemperatureRecord> temperatureHistory;

    public TemperatureAdapter(List<TemperatureRecord> temperatureHistory) {
        this.temperatureHistory = temperatureHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temperature_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemperatureRecord record = temperatureHistory.get(position);

        // Format timestamp to date
        long timestamp = record.getTimestamp();
        String dateStr;
        if (timestamp > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault());
            dateStr = sdf.format(new java.util.Date(timestamp));
        } else {
            dateStr = "Desconocido";
        }

        holder.dateTextView.setText(dateStr);
        holder.tempAguaTextView.setText(String.format("Agua: %.1f°C", record.getTempAgua()));
        holder.tempAireTextView.setText(String.format("Aire: %.1f°C", record.getTempAire()));
        holder.humedadTextView.setText(String.format("Hum: %.1f%%", record.getHumedadAire()));
    }

    @Override
    public int getItemCount() {
        return temperatureHistory.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView tempAguaTextView;
        TextView tempAireTextView;
        TextView humedadTextView;

        ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            tempAguaTextView = itemView.findViewById(R.id.tempAguaTextView);
            tempAireTextView = itemView.findViewById(R.id.tempAireTextView);
            humedadTextView = itemView.findViewById(R.id.humedadTextView);
        }
    }
}
