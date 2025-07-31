package com.example.homecontrol;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;

public class DeviceHandler extends RecyclerView.Adapter<DeviceHandler.DeviceViewHolder> {
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        ImageView statusIndicator;
        TextView deviceName;
        TextView OperatingSystem;
        CheckBox deviceCheckbox;
        public DeviceViewHolder(View itemView) {
            super(itemView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceCheckbox = itemView.findViewById(R.id.deviceCheckbox);
            OperatingSystem = itemView.findViewById(R.id.OperatingSystem);
        }
    }
    private final List<Device> devices;
    private final List<Device> selectedDevices = new ArrayList<>();
    public DeviceHandler(List<Device> devices) {this.devices = devices;}

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.deviceName.setText(device.label);
        holder.OperatingSystem.setText(device.OS);

        // Status collor
        int colorRes = device.isOnline ?
                R.color.green :
                R.color.red;
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        circle.setSize(16, 16);
        holder.statusIndicator.setImageDrawable(circle);
        holder.deviceCheckbox.setOnCheckedChangeListener(null);
        holder.deviceCheckbox.setChecked(selectedDevices.contains(device));

        holder.deviceCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!selectedDevices.contains(device)) {
                        selectedDevices.add(device);
                    }
                } else {
                    selectedDevices.remove(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {return devices.size();}

    public List<Device> getSelectedDevices() {
        return selectedDevices;
    }

    public void setDevices(List<Device> newDevices) {
        devices.clear();
        devices.addAll(newDevices);
        notifyDataSetChanged();
    }



}