package graphicalUI;

import client.Client;
import protocol.ProtocolCommand;
import server.Domain;
import server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.LogManager;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;

public class guiTest {

    private Server server;
    private Client alice;
    private Client bob;
    private final String mailAlice = "alice@gmail.com";
    private final String pwdAlice = "test";
    private final String wrongPwdAlice = "test000";
    private final String usernameAlice = "Alice";
    private final String mailBob = "bob@gmail.com";
    private final String pwdBob = "test2";
    private final String usernameBob = "Bob";
    private final String wrongMailAlice = "alice@toto.com";
    private final String wrongUsernameAlice = "Prout";
    private final String title = "Beautiful house";
    private final String titleUpdated = "Very beautiful house";
    private final String content = "Very big house";
    private final String contentUpdated = "Very much big house";
    private final Domain domain = Domain.HOUSE;
    private final int price = 1000;
    private final int priceUpdated = 10000;

	@Rule
	public TestName testName = new TestName();
    
    @Before
    public void initServerAndClient() throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException {
        System.out.println("[TEST] " + testName.getMethodName());
        this.server = new Server(true, false);
        this.alice = new Client(mailAlice, usernameAlice, false, false);
        new Thread(this.server).start();
    }

    @Test
    public void signUpOk() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
    }

    @Test
    public void signUpKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InterruptedException {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.wrongMailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.wrongUsernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.bob = new Client(mailBob, usernameBob, false, false);
        this.bob.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.bob.getGui().getMail().setText(this.mailAlice);
        this.bob.getGui().getPassword().setText(this.pwdBob);
        this.bob.getGui().getUser().setText(this.usernameBob);
        this.bob.getGui().clickSignUp();
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
    }

    @Test
    public void signInOk() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
    }

    @Test
    public void signInKo() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.wrongMailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.wrongPwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);
    }

    @Test
    public void signOutOk() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickSignOut();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_OUT_OK);
    }

    @Test
    public void signOutKo() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().clickSignOut();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_OUT_KO);
    }

    @Test
    public void createSaleOk() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.getGui().clickCreateCheckBox();
    }

    @Test
    public void createSaleKo() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_KO);
        this.alice.getGui().clickCreateCheckBox();
    }

    @Test
    public void updateSaleOk() throws InterruptedException {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().clickOnDomain(this.domain);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_OK);
        this.alice.getGui().clickOnAnnonce(this.title);
        this.alice.getGui().clickUpdateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.titleUpdated);
        this.alice.getGui().getContentAnnonce().setText(this.contentUpdated);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.priceUpdated));
        this.alice.getGui().clickUpdateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.UPDATE_SALE_OK);
        this.alice.getGui().clickUpdateCheckBox();
    }

    @Test
    public void updateSaleKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InterruptedException {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().clickOnDomain(this.domain);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().clickOnAnnonce(this.title);
        this.alice.getGui().clickUpdateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.titleUpdated);
        this.alice.getGui().getContentAnnonce().setText(this.contentUpdated);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.priceUpdated));
        this.alice.getGui().clickUpdateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.UPDATE_SALE_KO);
        this.alice.getGui().clickUpdateCheckBox();
    }

    @Test
    public void deleteSaleOk() {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().clickOnDomain(this.domain);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_OK);
        this.alice.getGui().clickOnAnnonce(this.title);
        this.alice.getGui().clickRemoveAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DELETE_SALE_OK);
    }

    @Test
    public void deleteSaleKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException, InterruptedException {
        this.alice.getGui().clickConnect();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.getGui().getMail().setText(this.mailAlice);
        this.alice.getGui().getPassword().setText(this.pwdAlice);
        this.alice.getGui().getUser().setText(this.usernameAlice);
        this.alice.getGui().clickSignUp();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.getGui().setMail(this.mailAlice);
        this.alice.getGui().setPwd(this.pwdAlice);
        this.alice.getGui().clickSignIn();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().getTitleAnnonceField().setText(this.title);
        this.alice.getGui().getDomainsComboBox().setSelectedItem(this.domain);
        this.alice.getGui().getContentAnnonce().setText(this.content);
        this.alice.getGui().getPriceField().setText(String.valueOf(this.price));
        this.alice.getGui().clickCreateAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.getGui().clickCreateCheckBox();
        this.alice.getGui().clickOnDomain(this.domain);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.getGui().clickOnAnnonce(this.title);
        this.alice.getGui().clickRemoveAnnonce();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DELETE_SALE_KO);
    }

    @After
    public void stopServerAndStopClient() throws IOException {
        this.alice.stop();
        this.alice = null;
        this.server.stopProcess();
        if (this.bob != null) {
            this.bob.stop();
            this.bob = null;
        }
        LogManager logManager = LogManager.getLogManager();
        logManager.reset();
    }
}
