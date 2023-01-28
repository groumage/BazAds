package test;

import main.protocol.Domain;
import main.server.Annonce;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ServerTest {

    public main.server.Server server;

    @Before
    public void initServerAndClient() throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException {
        this.server = new main.server.Server(true, false, true);
    }

    @Test
    public void getClientServerFromMail() {
        Assert.assertNull(this.server.getClientServerFromMail("toto"));
    }

    @Test
    public void getAnnonceListOfDomain() {
        Assert.assertArrayEquals(new Annonce[0], this.server.getAnnonceListOfDomain(Domain.HOUSE));
    }

    @Test
    public void updateAnnonce() {
        this.server.updateAnnonce("title", "descriptif", 1000, 0);
    }

    @After
    public void stopServerAndStopClient() throws IOException {
        this.server.stopProcess();
    }
}