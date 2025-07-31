package com.example.homecontrol;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private TextView serverText;
    private int switchcount;
    private LinearLayout homeContent;
    private LinearLayout addDeviceLayout;
    private LinearLayout pcStatusContent;
    private RecyclerView pcStatusRecyclerView;
    private List<Device> devicesList;
    private DeviceHandler devHandler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TabLayout tabLayout;
        setContentView(R.layout.activity_main);

        Button btnWake = findViewById(R.id.wakeButton);
        Button echoButton = findViewById(R.id.echoButton);
        Button restartButton = findViewById(R.id.restartButton);
        Button shutdownButton = findViewById(R.id.shutdownButton);
        Button restoreButton = findViewById(R.id.restoreButton);
        Button addDeviceButton = findViewById(R.id.addDeviceButton);
        Button deleteDeviceButton = findViewById(R.id.deleteDeviceButton);
        Button submitDeviceButton = findViewById(R.id.submitDeviceButton);

        EditText deviceLabelInput = findViewById(R.id.deviceLabelInput);
        EditText deviceMacInput = findViewById(R.id.deviceMacInput);
        EditText deviceIPInput = findViewById(R.id.deviceIPInput);

        serverText = findViewById(R.id.ServerText);
        tabLayout = findViewById(R.id.tabLayout);
        homeContent = findViewById(R.id.homeContent);
        addDeviceLayout = findViewById(R.id.addDeviceLayout);
        pcStatusContent = findViewById(R.id.pcStatusContent);
        pcStatusRecyclerView = findViewById(R.id.pcStatusRecyclerView);
        pcStatusRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (devicesList == null) {
            devicesList = new ArrayList<>();
        }

        devicesList = loadDevicesFromFile(this);

        devHandler = new DeviceHandler(devicesList);

        addDeviceButton.setOnClickListener( v-> {
                pcStatusContent.setVisibility(LinearLayout.GONE);
                addDeviceLayout.setVisibility(LinearLayout.VISIBLE);
            }
        );

        deleteDeviceButton.setOnClickListener(v -> {
            Delete ();
        });

        submitDeviceButton.setOnClickListener( v ->{
            String label = deviceLabelInput.getText().toString().trim();
            String mac = deviceMacInput.getText().toString().trim();
            String ip = deviceIPInput.getText().toString().trim();

            if (!label.isEmpty() || !mac.isEmpty() || !ip.isEmpty()){
                Device newDevice = new Device(label, mac, ip);
                devicesList.add(newDevice);
                // Save to file
                saveDevicesToFile(this, newDevice);
            }

            addDeviceLayout.setVisibility(LinearLayout.GONE);
            pcStatusContent.setVisibility(LinearLayout.VISIBLE);
            devHandler.notifyDataSetChanged();
            pcStatusContent.requestLayout();
            pcStatusContent.invalidate();
            pcStatusContent.setVisibility(LinearLayout.GONE);
            pcStatusContent.setVisibility(LinearLayout.VISIBLE);
        });

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        switch (tab.getPosition()) {
                            case 0:
                                // Commands tab
                                homeContent.setVisibility(LinearLayout.VISIBLE);
                                pcStatusContent.setVisibility(LinearLayout.GONE);
                                break;
                            case 1:
                                switchcount++;
                                // PC Status tab
                                for (Device device : devicesList) {
                                    CheckDeviceStatus(device);
                                }

                                try {
                                    Thread.sleep(35);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                if (switchcount == 1) {
                                    pcStatusRecyclerView.setAdapter(devHandler);
                                }
                                devHandler.setDevices(new ArrayList<>(devicesList));
                                devHandler.notifyDataSetChanged();

                                pcStatusContent.setVisibility(View.VISIBLE);
                                homeContent.setVisibility(View.GONE);
                                break;
                        }
                    }
                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                }
        );

        btnWake.setOnClickListener(v -> {
            serverText.setText("");
            serverText.append("Wake on Lan:\n");
            serverText.append("------------\n");
            List<Device> devicesList = devHandler.getSelectedDevices();
            for (Device device : devicesList) {
                SendWakeOnLan(device);
                runOnUiThread(() -> serverText.append("Woke up " + device.label));
            }
        });

        echoButton.setOnClickListener (v -> {
            serverText.setText("");
            serverText.append("Echo:\n");
            serverText.append("-----\n");
            List<Device> devicesList = devHandler.getSelectedDevices();
            for (Device device : devicesList) {
                ServerComm("Echo", device);
            }
        });

        restartButton.setOnClickListener (v -> {
            serverText.setText("");
            serverText.append("Restart:\n");
            serverText.append("--------\n");
            List<Device> devicesList = devHandler.getSelectedDevices();
            for (Device device : devicesList ) {
                ServerComm("Restart", device);
            }
        });

        shutdownButton.setOnClickListener (v -> {
            serverText.setText("");
            serverText.append("Shutdown:\n");
            serverText.append("---------\n");
            List<Device> devicesList = devHandler.getSelectedDevices();
            for (Device device : devicesList ) {
                ServerComm("Shutdown", device);
            }
        });

        restoreButton.setOnClickListener (v ->{
            serverText.setText("");
            serverText.append("Restore:\n");
            serverText.append("--------\n");
            List<Device> devicesList = devHandler.getSelectedDevices();
            for (Device device : devicesList ) {
                ServerComm("Restore", device);
            }
        });
    }
    private void Delete () {
        List<Device> devicesList = devHandler.getSelectedDevices();
        for (Device device : devicesList) {
            deleteDeviceByLabel(this, device.label);
            removeDeviceByLabel(device.label);
        }
        devHandler.notifyDataSetChanged();
        pcStatusContent.requestLayout();
        pcStatusContent.invalidate();
        pcStatusContent.setVisibility(LinearLayout.GONE);
        pcStatusContent.setVisibility(LinearLayout.VISIBLE);
    }
    private void CheckDeviceStatus(Device device) {
        new Thread(() -> {
            Socket clientSocket = new Socket();
            try {
                clientSocket.connect(new InetSocketAddress(device.ipAddress, 41007), 350);

                // Send To Server
                OutputStream os = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(os, true);
                writer.println("Echo Silent");

                // Read From Server
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String Response = in.readLine();

                clientSocket.close();

                String[] parts = Response.split(" - ");
                device.isOnline = true;
                device.OS = parts[1];
            } catch (Exception e) {
                device.isOnline = false;
            }
        }).start();
    }

    protected void ServerComm (String Choice, Device device) {
        new Thread(()-> {
            try {
                Socket clientSocket = new Socket(device.ipAddress, 41007);

                // Send To Server
                OutputStream os = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(os, true);
                writer.println(Choice);

                // Read From Server
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String Response = in.readLine();
                runOnUiThread(() -> serverText.append(Response + "\n"));
                clientSocket.close();

                if (Choice.equals("Restore")){
                    String cleanedResponse = Response.replace(" - Restoring", "").trim();
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e){
                        runOnUiThread(() -> serverText.append("Restore sleep interrupted" + e));
                        Thread.currentThread().interrupt();
                    }

                    runOnUiThread(() -> serverText.append(cleanedResponse + " - Restored\n"));
                    SendWakeOnLan(device);
                }
            } catch (Exception e) {
                device.isOnline = false;
            }
        }).start();
    }

    private void SendWakeOnLan(Device device)  {
        new Thread(() -> {
            try {
                byte[] macBytes = getMacBytes(device.macAddress);
                byte[] bytes = new byte[6 + 16 * macBytes.length];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xFF;
                }
                for (int i = 6; i < bytes.length; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }

                InetAddress address = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            } catch (Exception e) {
                runOnUiThread(() -> serverText.append("Failed to wake " + device.label +" up"));
            }
        }).start();
    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("[:\\-]");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    public ArrayList<Device> loadDevicesFromFile(Context context) {
        ArrayList<Device> devicesList = new ArrayList<>();
        File file = new File(context.getFilesDir(), "devices.json");

        try {
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream fos = context.openFileOutput("devices.json", Context.MODE_PRIVATE);
                fos.write("[]".getBytes());
                fos.close();
            }

            FileInputStream fis = context.openFileInput("devices.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            isr.close();
            fis.close();

            JSONArray jsonArray = new JSONArray(sb.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String label = obj.getString("label");
                String mac = obj.getString("mac");
                String ip = obj.getString("ip");

                devicesList.add(new Device(label, mac, ip));
            }
        } catch (Exception e) {
            serverText.setText("Failed to load Devices On startup");
        }

        return devicesList;
    }
    public void saveDevicesToFile(Context context, Device device) {
        JSONArray jsonArray;

        File file = new File(context.getFilesDir(), "devices.json");

        try {
            if (file.exists()) {
                FileInputStream fis = context.openFileInput("devices.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                fis.close();

                jsonArray = new JSONArray(sb.toString());
            } else {
                jsonArray = new JSONArray();
            }

            JSONObject deviceJson = new JSONObject();
            deviceJson.put("label", device.getLabel());
            deviceJson.put("mac", device.getMac());
            deviceJson.put("ip", device.getIp());
            jsonArray.put(deviceJson);

            FileOutputStream fos = context.openFileOutput("devices.json", Context.MODE_PRIVATE);
            fos.write(jsonArray.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            serverText.setText("Failed to Save Device");
        }
    }

    public void deleteDeviceByLabel(Context context, String labelToDelete) {
        File file = new File(context.getFilesDir(), "devices.json");

        if (!file.exists()) {
            return;
        }

        try {
            FileInputStream fis = context.openFileInput("devices.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            fis.close();

            JSONArray jsonArray = new JSONArray(sb.toString());
            JSONArray updatedArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (!obj.getString("label").equalsIgnoreCase(labelToDelete)) {
                    updatedArray.put(obj);
                }
            }

            FileOutputStream fos = context.openFileOutput("devices.json", Context.MODE_PRIVATE);
            fos.write(updatedArray.toString().getBytes());
            fos.close();


        } catch (Exception e) {
            serverText.setText("Failed to Delete Devices");
        }
    }
    public void removeDeviceByLabel(String labelToRemove) {
        devicesList.removeIf(device -> device.getLabel().equalsIgnoreCase(labelToRemove));
    }
}
