package com.example.KYT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Shorthand extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_IMAGE_WITH_MESSAGE = 2;
    private EditText editTextMessage;
    private ImageView imageView;
    private TextView textViewDecryptedMessage;
    private Bitmap selectedImage;
    private String key = "1234567890123456"; // 16-значный ключ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shorthand);

        editTextMessage = findViewById(R.id.editTextMessage);
        imageView = findViewById(R.id.imageView);
        textViewDecryptedMessage = findViewById(R.id.textViewDecryptedMessage);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button buttonEncrypt = findViewById(R.id.buttonEncrypt);
        Button buttonSelectImageWithMessage = findViewById(R.id.buttonSelectImageWithMessage);
        showInformationDialog();

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser(PICK_IMAGE);
            }
        });

        buttonEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    try {
                        String message = editTextMessage.getText().toString();
                        String encryptedText = encryptText(message, key);
                        String binaryStr = base64ToBin(encryptedText);

                        Bitmap newBitmap = embedTextInImage(selectedImage, binaryStr);

                        imageView.setImageBitmap(newBitmap);

                        saveBitmap(newBitmap, new File(getExternalFilesDir(null), "output_image.png"));

                        // Save encrypted image to device
                        saveEncryptedImage(newBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            private void saveEncryptedImage(Bitmap bitmap) {
                try {
                    // Получаем каталог общедоступных картинок
                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    // Создаем каталог, если он не существует
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    // Создаем файл изображения
                    File file = new File(directory, "encrypted_image.png");
                    // Создаем поток для записи изображения в файл
                    FileOutputStream out = new FileOutputStream(file);
                    // Сохраняем изображение в формате PNG с максимальным качеством
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    // Принудительно очищаем буфер и закрываем поток
                    out.flush();
                    out.close();
                    // Обновляем галерею, чтобы изображение было видно в ней
                    MediaScannerConnection.scanFile(Shorthand.this,
                            new String[]{file.getAbsolutePath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    // Сканирование завершено
                                }
                            });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        });

        buttonSelectImageWithMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser(PICK_IMAGE_WITH_MESSAGE);
            }
        });
    }

    private void openImageChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE || requestCode == PICK_IMAGE_WITH_MESSAGE) && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);

                if (requestCode == PICK_IMAGE_WITH_MESSAGE) {
                    String binaryStr = extractTextFromImage(selectedImage);
                    String base64Str = binToBase64(binaryStr);
                    String decryptedText = decryptText(base64Str, key);
                    textViewDecryptedMessage.setText(decryptedText);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String encryptText(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    private String decryptText(String base64Str, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");
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
        binaryStr = "11111111" + binaryStr + "00000000"; // Добавляем завершающие строки

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
        }
        return binaryData;
    }

    private void saveBitmap(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
    }
    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Инструкция по использованию");
        builder.setMessage("Для использования этого приложения выполните следующие шаги:\n\n" +
                "1. Нажмите кнопку 'Выбрать изображение', чтобы выбрать изображение из галереи.\n\n" +
                "2. Введите ваше сообщение в текстовое поле.\n\n" +
                "3. Нажмите кнопку 'Зашифровать', чтобы зашифровать ваше сообщение и встроить его в изображение.\n\n" +
                "4. Зашифрованное изображение будет отображено в предварительном просмотре.\n\n" +
                "5. Нажмите кнопку 'Выбрать изображение с сообщением', чтобы выбрать изображение с зашифрованным сообщением.\n\n" +
                "6. Вы можете увидеть расшифрованное сообщение ниже выбранного изображения.");

        builder.setPositiveButton("Понятно", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
