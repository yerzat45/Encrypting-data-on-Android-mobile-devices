package com.example.KYT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAactivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 123;
    private static final String KEY_FILE_NAME = "key.docx";

    private EditText editTextKey;
    private EditText EditTextPublicKey;
    private EditText EditTextPrivateKey;
    private EditText EditTextEncryptKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsaactivity);

        Button buttonEncrypt = findViewById(R.id.rsaEncryptFileBtn);
        Button buttonDecrypt = findViewById(R.id.rsaDecryptFileBtn);
        Button buttonGenerateKey = findViewById(R.id.buttonGenerateKey);
        Button buttonGenerateKeyRSA = findViewById(R.id.buttonGenerateKeyRSA);
        Button buttonEncryptText = findViewById(R.id.EncryptKeyBtn);
        Button buttonDecryptText = findViewById(R.id.DecryptKeyBtn);
        editTextKey = findViewById(R.id.editTextKey);
        EditTextPublicKey = findViewById(R.id.EditTextPublicKey);
        EditTextPrivateKey = findViewById(R.id.EditTextPrivateKey);
        EditTextEncryptKey = findViewById(R.id.EditTextEncryptKey);
        showInformationDialog();

        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = editTextKey.getText().toString();
                if (!key.isEmpty()) {
                    openFilePicker(FILE_PICKER_REQUEST_CODE, key);

                    // Save key to file
                    File keyFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), KEY_FILE_NAME);
                    try {
                        FileOutputStream keyOutputStream = new FileOutputStream(keyFile);
                        keyOutputStream.write(key.getBytes());
                        keyOutputStream.close();
                        Toast.makeText(RSAactivity.this, "Key saved to " + keyFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(RSAactivity.this, "Error saving key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RSAactivity.this, "Please enter a key", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = editTextKey.getText().toString();
                if (!key.isEmpty()) {
                    openFilePicker(FILE_PICKER_REQUEST_CODE + 1, key);
                } else {
                    Toast.makeText(RSAactivity.this, "Please enter a key", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String generatedKey = generateKey();
                editTextKey.setText(generatedKey);
            }
        });

        buttonGenerateKeyRSA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyPair keyPair = generateRSAKeyPair();
                if (keyPair != null) {
                    PublicKey publicKey = keyPair.getPublic();
                    PrivateKey privateKey = keyPair.getPrivate();

                    // Помещаем значения ключей в текстовые поля
                    EditTextPublicKey.setText(android.util.Base64.encodeToString(publicKey.getEncoded(), android.util.Base64.DEFAULT));
                    EditTextPrivateKey.setText(android.util.Base64.encodeToString(privateKey.getEncoded(), android.util.Base64.DEFAULT));

                    Toast.makeText(RSAactivity.this, "RSA keys generated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RSAactivity.this, "Error generating RSA key pair", Toast.LENGTH_SHORT).show();
                }
            }
        });


        buttonEncryptText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String publicKeyBase64 = EditTextPublicKey.getText().toString().trim();
                String textToEncrypt = editTextKey.getText().toString().trim();

                if (!publicKeyBase64.isEmpty() && !textToEncrypt.isEmpty()) {
                    try {
                        // Декодирование публичного ключа из Base64
                        byte[] publicKeyBytes = android.util.Base64.decode(publicKeyBase64, android.util.Base64.DEFAULT);
                        if (publicKeyBytes == null) {
                            throw new IllegalArgumentException("Invalid Base64 public key");
                        }

                        // Шифрование текста с помощью указанного публичного ключа
                        byte[] encryptedBytes = encryptTextWithPublicKey(textToEncrypt, publicKeyBytes);
                        if (encryptedBytes == null) {
                            throw new Exception("Encryption returned null");
                        }

                        String encryptedText = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT);
                        EditTextEncryptKey.setText(encryptedText);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RSAactivity.this, "Error encrypting text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RSAactivity.this, "Please enter both public key and text to encrypt", Toast.LENGTH_SHORT).show();
                }
            }
        });




        buttonDecryptText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String privateKeyBase64 = EditTextPrivateKey.getText().toString(); // Приватный ключ для дешифрования
                String textToDecrypt = EditTextEncryptKey.getText().toString(); // Зашифрованный текст

                if (!privateKeyBase64.isEmpty() && !textToDecrypt.isEmpty()) {
                    try {
                        // Дешифруем текст с помощью указанного приватного ключа
                        byte[] encryptedBytes = android.util.Base64.decode(textToDecrypt, android.util.Base64.DEFAULT);
                        byte[] privateKeyBytes = android.util.Base64.decode(privateKeyBase64, android.util.Base64.DEFAULT); // Преобразуем строку Base64 в массив байтов
                        String decryptedText = decryptTextWithPrivateKey(encryptedBytes, privateKeyBytes);
                        editTextKey.setText(decryptedText);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RSAactivity.this, "Error decrypting text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RSAactivity.this, "Please enter both private key and text to decrypt", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openFilePicker(int requestCode, String key) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
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
                    String key = editTextKey.getText().toString();
                    if (requestCode == FILE_PICKER_REQUEST_CODE) {
                        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "encrypted.docx");
                        AESFileEncryption.encrypt(inputFile, outputFile, key);
                        Toast.makeText(this, "File encrypted successfully", Toast.LENGTH_SHORT).show();
                    } else if (requestCode == FILE_PICKER_REQUEST_CODE + 1) {
                        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "decrypted.docx");
                        AESFileEncryption.decrypt(inputFile, outputFile, key);
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

    private KeyPair generateRSAKeyPair() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Указываем длину ключа, в данном случае 2048 бит
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Key Generation Error", "Error generating RSA key pair: " + e.getMessage());
        }
        return keyPair;
    }

    private byte[] encryptTextWithPublicKey(String text, byte[] publicKeyBytes) throws Exception {
        if (publicKeyBytes == null || publicKeyBytes.length == 0) {
            throw new IllegalArgumentException("Public key is null or empty");
        }

        Cipher cipher = Cipher.getInstance("RSA");
        PublicKey publicKey = getPublicKey(publicKeyBytes);
        if (publicKey == null) {
            throw new IllegalArgumentException("Invalid public key");
        }

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(text.getBytes());
    }


    private String decryptTextWithPrivateKey(byte[] encryptedBytes, byte[] privateKeyBytes) {
        String decryptedText = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            PrivateKey key = getPrivateKey(privateKeyBytes);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            decryptedText = new String(decryptedBytes);
            Log.d("Decryption", "Decrypted text: " + decryptedText); // добавляем эту строку для вывода результата дешифрования в лог
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Decryption Error", "Error decrypting text with RSA private key: " + e.getMessage());
        }
        return decryptedText;
    }





    private PublicKey getPublicKey(byte[] keyBytes) throws Exception {
        if (keyBytes == null || keyBytes.length == 0) {
            throw new IllegalArgumentException("Key bytes are null or empty");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return keyFactory.generatePublic(keySpec);
    }


    private PrivateKey getPrivateKey(byte[] keyBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
    private void showInformationDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Информация");
        builder.setMessage("В поле <Введите ключ> вводится ключ длиной 128 бит. Если у вас нет ключа, нажмите на кнопку 'Сгенерировать ключ', чтобы сгенерировать новый ключ. Затем выберите файл для шифрования или дешифрования, нажав соответствующую кнопку. Файл будет сохранен в папке Downloads.");

        builder.setPositiveButton("Понятно", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
