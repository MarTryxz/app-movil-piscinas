package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothMacActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BT = 2;
    // Standard Serial Port Profile UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ListView deviceList;
    private TextView txtStatus, txtResultado;
    private Button btnGetMac, btnCopy;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket btSocket = null;
    private ArrayList<String> deviceNameList = new ArrayList<>();
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_mac);

        deviceList = findViewById(R.id.deviceList);
        txtStatus = findViewById(R.id.txtStatus);
        txtResultado = findViewById(R.id.txtResultado);
        btnGetMac = findViewById(R.id.btnGetMac);
        btnCopy = findViewById(R.id.btnCopy);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkPermissionsAndInit();
        setupBottomNavigation();

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = devices.get(position);
            connectToDevice(device);
        });

        btnGetMac.setOnClickListener(v -> getMacAddress());

        btnCopy.setOnClickListener(v -> {
            String text = txtResultado.getText().toString().replace("MAC recibida: ", "");
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("MAC Address", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkPermissionsAndInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_PERMISSION_BT);
                return;
            }
        }

        initBluetooth();
    }

    private void initBluetooth() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return; // Should be handled by onRequestPermissionsResult
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            listPairedDevices();
        }
    }

    private void listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceNameList.clear();
        devices.clear();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNameList.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
        } else {
            deviceNameList.add("No hay dispositivos emparejados");
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNameList);
        deviceList.setAdapter(adapter);
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        txtStatus.setText("Conectando...");

        new Thread(() -> {
            try {
                if (btSocket != null) {
                    btSocket.close();
                }
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();

                runOnUiThread(() -> {
                    txtStatus.setText("Conectado a: " + device.getName());
                    btnGetMac.setEnabled(true);
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    txtStatus.setText("Error de conexión");
                    Toast.makeText(BluetoothMacActivity.this, "No se pudo conectar", Toast.LENGTH_SHORT).show();
                });
                try {
                    btSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }).start();
    }

    private void getMacAddress() {
        if (btSocket != null && btSocket.isConnected()) {
            new Thread(() -> {
                try {
                    // 1. ENVIAR LA SOLICITUD ('M')
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write("M".getBytes()); // Enviamos la letra clave

                    // 2. ESCUCHAR LA RESPUESTA
                    InputStream inputStream = btSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytes;

                    // Esperamos un momento a que el Arduino procese y responda
                    Thread.sleep(200);

                    if (inputStream.available() > 0) {
                        bytes = inputStream.read(buffer);
                        final String respuestaArduino = new String(buffer, 0, bytes);

                        // 3. ACTUALIZAR LA UI (Siempre desde el hilo principal)
                        runOnUiThread(() -> {
                            txtResultado.setText("MAC recibida: " + respuestaArduino);
                            btnCopy.setVisibility(View.VISIBLE);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast
                            .makeText(BluetoothMacActivity.this, "Error al enviar/recibir datos", Toast.LENGTH_SHORT)
                            .show());
                }
            }).start();
        } else {
            Toast.makeText(this, "Bluetooth no conectado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBluetooth();
            } else {
                Toast.makeText(this, "Permisos de Bluetooth requeridos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
