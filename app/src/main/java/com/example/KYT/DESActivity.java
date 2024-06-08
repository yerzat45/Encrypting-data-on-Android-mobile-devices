package com.example.KYT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

public class DESActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 123;
    private static final String KEY_FILE_NAME = "keyDES.docx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desactivity);

        Button buttonEncrypt = findViewById(R.id.desEncryptFileBtn);
        Button buttonDecrypt = findViewById(R.id.desDecryptFileBtn);
        Button buttonGenerateKey = findViewById(R.id.buttonGenerateKey);
        EditText editTextKey = findViewById(R.id.editTextKey);
        showInformationDialog();

        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker(FILE_PICKER_REQUEST_CODE);
                saveKeyToFile(editTextKey);
            }
        });

        buttonDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker(FILE_PICKER_REQUEST_CODE + 1);
            }
        });

        buttonGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedKey = generateKey();
                editTextKey.setText(generatedKey);
            }
        });
    }

    private void openFilePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }

    private void saveKeyToFile(EditText editTextKey) {
        String key = editTextKey.getText().toString();
        if (!key.isEmpty()) {
            File keyFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), KEY_FILE_NAME);
            try {
                FileOutputStream keyOutputStream = new FileOutputStream(keyFile);
                keyOutputStream.write(key.getBytes());
                keyOutputStream.close();
                Toast.makeText(DESActivity.this, "Key saved to " + keyFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DESActivity.this, "Error saving key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DESActivity.this, "Please enter a key", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    File inputFile = File.createTempFile("input", ".docx", getCacheDir());
                    FileOutputStream outputStream = new FileOutputStream(inputFile);
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();

                    File outputFile;
                    String key = ((EditText) findViewById(R.id.editTextKey)).getText().toString();
                    if (requestCode == FILE_PICKER_REQUEST_CODE) {
                        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "encryptedDES.docx");
                        DESFileEncryption.encrypt(inputFile, outputFile, key);
                        Toast.makeText(this, "File encrypted successfully", Toast.LENGTH_SHORT).show();
                    } else if (requestCode == FILE_PICKER_REQUEST_CODE + 1) {
                        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "decryptedDES.docx");
                        DESFileEncryption.decrypt(inputFile, outputFile, key);
                        Toast.makeText(this, "File decrypted successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16]; // 128-bit key
        secureRandom.nextBytes(key);
        return android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT);
    }

    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Информация");
        builder.setMessage("В поле <Введите ключ> " +
                "вводится ключ длиной 128 бит " +
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
}
