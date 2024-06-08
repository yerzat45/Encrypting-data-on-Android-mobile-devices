package com.example.KYT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;

public class TwofishActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 123;
    private static final String KEY_FILE_NAME = "keyChaCha20.docx";
    private EditText editTextKey;
    private static final String TAG = "TwofishActivity";
    private String key; // Объявление переменной key как член класса

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twofish);

        Button buttonEncrypt = findViewById(R.id.EncryptFileBtn);
        Button buttonDecrypt = findViewById(R.id.DecryptFileBtn);
        Button buttonGenerateKey = findViewById(R.id.buttonGenerateKey);
        editTextKey = findViewById(R.id.editTextKey);
        showInformationDialog();

        buttonEncrypt.setOnClickListener(v -> {
            key = editTextKey.getText().toString(); // Присвоение значения переменной key
            if (!key.isEmpty()) {
                openFilePicker(FILE_PICKER_REQUEST_CODE);
            } else {
                Toast.makeText(TwofishActivity.this, "Please enter a key", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDecrypt.setOnClickListener(v -> {
            key = editTextKey.getText().toString(); // Присвоение значения переменной key
            if (!key.isEmpty()) {
                openFilePicker(FILE_PICKER_REQUEST_CODE + 1);
            } else {
                Toast.makeText(TwofishActivity.this, "Please enter a key", Toast.LENGTH_SHORT).show();
            }
        });

        buttonGenerateKey.setOnClickListener(v -> {
            key = generateKey(); // Сохраняем сгенерированный ключ
            editTextKey.setText(key);
            saveKeyToFile(key); // Сохраняем ключ в файл
        });
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (requestCode == FILE_PICKER_REQUEST_CODE) {
                    encryptFile(uri);
                } else if (requestCode == FILE_PICKER_REQUEST_CODE + 1) {
                    decryptFile(uri);
                }
            }
        }
    }

    private void encryptFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "encryptedChaCha20.docx");
            ChaCha20FileEncryption.encrypt(inputStream, new FileOutputStream(outputFile), hexStringToByteArray(key));
            Toast.makeText(this, "File encrypted successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "decryptedChaCha20.docx");
            ChaCha20FileEncryption.decrypt(inputStream, new FileOutputStream(outputFile), hexStringToByteArray(key));
            Toast.makeText(this, "File decrypted successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private String generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256-bit key
        secureRandom.nextBytes(keyBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : keyBytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Информация");
        builder.setMessage("В поле <Введите ключ> " +
                "вводится ключ длиной 256 бит " +
                "Если у вас нет ключа, нажмите " +
                "на кнопку 'Сгенерировать ключ', " +
                "чтобы сгенерировать новый ключ. " +
                "Затем выберите файл для шифрования " +
                "или дешифрования, нажав соответствующую " +
                "кнопку. Файл будет сохранен в папке " +
                "Downloads.");
        builder.setPositiveButton("Понятно", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveKeyToFile(String key) {
        try {
            File keyFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), KEY_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(keyFile);
            fos.write(key.getBytes());
            fos.close();
            Toast.makeText(this, "Key saved to " + keyFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving key to file: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving key to file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
