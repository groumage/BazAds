package test;

import main.client.Client;
import main.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    Server server;
    Client alice;

    @Before
    public void initServerAndClient() throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException {
        this.server = new Server(true, false, true);
        this.alice = new Client("alice@gmail.com", "Alice", false, false);
        new Thread(this.server).start();
    }

    @After
    public void stopServerAndStopClient() throws IOException {
        this.alice.stopProcess();
        this.server.stopProcess();
    }
}