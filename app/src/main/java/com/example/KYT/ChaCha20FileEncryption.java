package com.example.KYT;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

public class ChaCha20FileEncryption {

    private static final int KEY_SIZE_BYTES = 32; // 256-bit key size
    private static final int NONCE_SIZE_BYTES = 12; // 96-bit nonce size
    private static final int BUFFER_SIZE = 4096; // 4 KB buffer size

    public static void encrypt(InputStream inputStream, OutputStream outputStream, byte[] key) throws IOException {
        byte[] nonce = generateNonce();
        StreamCipher cipher = createCipher(true, key, nonce);
        outputStream.write(nonce);
        processStream(inputStream, outputStream, cipher);
    }

    public static void decrypt(InputStream inputStream, OutputStream outputStream, byte[] key) throws IOException {
        byte[] nonce = new byte[NONCE_SIZE_BYTES];
        if (inputStream.read(nonce) != NONCE_SIZE_BYTES) {
            throw new IOException("Failed to read nonce");
        }
        StreamCipher cipher = createCipher(false, key, nonce);
        processStream(inputStream, outputStream, cipher);
    }

    private static StreamCipher createCipher(boolean forEncryption, byte[] key, byte[] nonce) {
        ChaCha7539Engine engine = new ChaCha7539Engine();
        CipherParameters parameters = new ParametersWithIV(new KeyParameter(key), nonce);
        engine.init(forEncryption, parameters);
        return engine;
    }

    private static void processStream(InputStream inputStream, OutputStream outputStream, StreamCipher cipher) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            cipher.processBytes(buffer, 0, bytesRead, buffer, 0);
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
    }

    private static byte[] generateNonce() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonce = new byte[NONCE_SIZE_BYTES];
        secureRandom.nextBytes(nonce);
        return nonce;
    }
}
