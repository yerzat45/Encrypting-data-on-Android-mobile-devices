package com.example.KYT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class yerz1 extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private EditText inputText;
    private TextView encryptedText;
    private Key secretKey;
    private ImageView imageView;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yerz1);

        inputText = findViewById(R.id.inputText);
        encryptedText = findViewById(R.id.encryptedText);
        Button encryptButton = findViewById(R.id.encryptButton);
        Button decryptButton = findViewById(R.id.decryptButton);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button decryptImageButton = findViewById(R.id.decryptImageButton);
        imageView = findViewById(R.id.imageView);

        try {
            secretKey = generateKey();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(yerz1.this, "Error generating key", Toast.LENGTH_SHORT).show();
            return;
        }

        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plainText = inputText.getText().toString();
                try {
                    byte[] encrypted = encrypt(plainText.getBytes(), secretKey);
                    String encryptedHex = bytesToHex(encrypted);
                    encryptedText.setText(encryptedHex);

                    if (selectedImage != null) {
                        String encryptedBase64 = Base64.encodeToString(encrypted, Base64.DEFAULT);
                        String binaryStr = base64ToBin(encryptedBase64);
                        Bitmap newBitmap = embedTextInImage(selectedImage, binaryStr);
                        imageView.setImageBitmap(newBitmap);

                        saveBitmap(newBitmap, new File(getExternalFilesDir(null), "encrypted_image.png"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(yerz1.this, "Encryption error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String encrypted = encryptedText.getText().toString();
                try {
                    byte[] decrypted = decrypt(hexToBytes(encrypted), secretKey);
                    inputText.setText(new String(decrypted));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(yerz1.this, "Decryption error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser(PICK_IMAGE);
            }
        });

        decryptImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    try {
                        String binaryStr = extractTextFromImage(selectedImage);
                        String base64Str = binToBase64(binaryStr);
                        byte[] encrypted = Base64.decode(base64Str, Base64.DEFAULT);
                        byte[] decrypted = decrypt(encrypted, secretKey);
                        inputText.setText(new String(decrypted));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(yerz1.this, "Image decryption error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(yerz1.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openImageChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    private Key generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // Use 256-bit AES
        return keyGenerator.generateKey();
    }

    private byte[] encrypt(byte[] plainText, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainText);
    }

    private byte[] decrypt(byte[] cipherText, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private String base64ToBin(String base64Str) {
        StringBuilder binaryStr = new StringBuilder();
        for (char c : base64Str.toCharArray()) {
            String bin = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binaryStr.append(bin);
        }
        return binaryStr.toString();
    }

    private String binToBase64(String binaryStr) {
        StringBuilder base64Str = new StringBuilder();
        for (int i = 0; i < binaryStr.length(); i += 8) {
            String byteStr = binaryStr.substring(i, Math.min(i + 8, binaryStr.length()));
            int charCode = Integer.parseInt(byteStr, 2);
            base64Str.append((char) charCode);
        }
        return base64Str.toString();
    }

    private Bitmap embedTextInImage(Bitmap bitmap, String binaryStr) {
        binaryStr = "11111111" + binaryStr + "00000000"; // Add markers

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= binaryStr.length()) break outerLoop;

                int pixel = newBitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                int newB = (b & 0xFE) | (binaryStr.charAt(bitIndex) - '0');
                int newPixel = (0xFF << 24) | (r << 16) | (g << 8) | newB;

                newBitmap.setPixel(x, y, newPixel);

                bitIndex++;
            }
        }
        return newBitmap;
    }

    private String extractTextFromImage(Bitmap bitmap) {
        StringBuilder binaryStr = new StringBuilder();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int b = pixel & 0xFF;

                binaryStr.append(b & 1);
                if (binaryStr.length() >= 16 && binaryStr.substring(binaryStr.length() - 8).equals("00000000")) {
                    break outerLoop;
                }
            }
        }
        // Removing start and end markers
        String binaryData = binaryStr.toString();
        if (binaryData.startsWith("11111111") && binaryData.endsWith("00000000")) {
            binaryData = binaryData.substring(8, binaryData.length() - 8);
        } else {
            throw new IllegalStateException("Markers not found");
        }
        return binaryData;
    }

    private void saveBitmap(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }
}
