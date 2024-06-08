package com.example.KYT;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonAES = findViewById(R.id.buttonAES); // Получаем ссылку на кнопку AES
        Button buttonDES = findViewById(R.id.buttonDES); // Получаем ссылку на кнопку DES
        Button buttonRSA = findViewById(R.id.buttonRSA); // Получаем ссылку на кнопку RSA
        Button buttonTwofish = findViewById(R.id.buttonTwofish); // Получаем ссылку на кнопку Twofish
        Button buttonShorthand = findViewById(R.id.buttonShorthand);
        Button buttonyerz1 = findViewById(R.id.buttonyerz1);

        buttonAES.setOnClickListener(v -> openAESActivity()); // Метод для открытия AES
        buttonDES.setOnClickListener(v -> openDESActivity()); // Метод для открытия DES
        buttonRSA.setOnClickListener(v -> openRSAActivity()); // Метод для открытия RSA
        buttonTwofish.setOnClickListener(v -> openTwofishActivity()); // Метод для открытия Twofish
        buttonShorthand.setOnClickListener(v -> openShorthand());
        buttonyerz1.setOnClickListener(v -> openShorthand());
        showInformationDialog();
    }

    private void openAESActivity() {
        Intent intent = new Intent(MainActivity.this, AESActivity.class);
        startActivity(intent);
    }

    private void openDESActivity() {
        Intent intent = new Intent(MainActivity.this, DESActivity.class);
        startActivity(intent);
    }

    private void openRSAActivity() {
        Intent intent = new Intent(MainActivity.this, RSAactivity.class);
        startActivity(intent);
    }

    private void openTwofishActivity() {
        Intent intent = new Intent(MainActivity.this, TwofishActivity.class);
        startActivity(intent);
    }

    private void openShorthand() {
        Intent intent = new Intent(MainActivity.this, Shorthand.class);
        startActivity(intent);
    }

    private void openyerz1() {
        Intent intent = new Intent(MainActivity.this, yerz1.class);
        startActivity(intent);
    }
    // Метод для отображения всплывающего окна с информацией
    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Тема дипломного проекта");
        builder.setMessage("Анализ и разработка мобильного приложения с поддержкой шифрования для защиты данных на смартфонах на базе Android\n\n" +
                "Выполнил: Кабидуллов Е.Т.\n" +
                "Руководитель: Зуева Е.А.\n" +
                "Алматы г.2024");

        builder.setPositiveButton("Понятно", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
