package protocol;

import client.Client;
import client.ClientState;
import logger.ErrorLogMessage;
import logger.InternalLogMessage;
import logger.TokenInternalLogMessage;
import server.Domain;
import server.Sale;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.LogManager;

import static org.awaitility.Awaitility.*;

public class ProtocolTest {

	Server server;
	Client alice;
	Client bob;
	String hashPwdClient;

	@Rule
	public TestName testName = new TestName();
	
	@Before
	public void initServerAndClient() throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException, InvalidKeySpecException {
		System.out.println("[TEST] " + testName.getMethodName());
		this.server = new Server(true, false);
		this.alice = new Client("alice@gmail.com", "Alice", false, false);
        new Thread(this.server).start();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashPwdClient = digest.digest("test".getBytes(StandardCharsets.UTF_8));
        this.hashPwdClient = Arrays.toString(hashPwdClient);
	}

	/**
	 * Test a successful requestPublicKeyOfCentralServcer request.
	 */ 
	@Test
    public void requestPublicKeyOfCentralServerOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		// the lastProcessedCommand is initialized to null so until the first command is processed, the await() will throw a NullPointerException
		// this is why we ignore the exception until the first command is processed

		Object[] expectedLog = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, this.server.getPk().getEncoded()),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLog);
    }

	/**
	 * Test a failed requestPublicKeyOfCentralServer request.
	 */
    @Test
    public void requestPublicKeOfCentralServeryKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		this.alice.openSocketToCentralServer();
		this.server.setRespondingToRequest(false);
		this.alice.requestPublicKeyOfCentralServer();
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO);
		
		Object[] expectedLogClient = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REQUEST_PUBLIC_KEY_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogClient);
    }
    
    /**
     * Test a successful signUp request.
     */
    @Test
    public void signUpOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK); 

		Object[] expectedLogClient = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
        };        
        Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogClient);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

	/**
	 * Test a failed signUp request.
	 */
	@Test
    public void signUpKo() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
		this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@toto.com", "Alice", this.hashPwdClient); // mail not valid
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.alice.signUp("alice@gmail.com", "Prout", this.hashPwdClient); // name not valid
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.alice.signUp("alice@toto.com", "Prout", this.hashPwdClient); // if both mail and name are not valid, mail is checked first
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
        this.bob = new Client("bob@gmail.com", "Bob", false, false);
		this.bob.openSocketToCentralServer();
        this.bob.requestPublicKeyOfCentralServer();
        given().ignoreExceptions().await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.bob.signUp("alice@gmail.com", "Bob", this.hashPwdClient); // bob takes alice's mail, thus alice can't sign up
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient); // mail already taken
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);
		this.server.setRespondingToRequest(false);
        this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient); // server not responding
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_KO);

        Object[] expectedLogAlice = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@toto.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.MAIL_NOT_VALID.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, ErrorLogMessage.MAIL_NOT_VALID.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Prout", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.NAME_NOT_VALID.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, ErrorLogMessage.NAME_NOT_VALID.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_UP, "alice@toto.com", "Prout", this.hashPwdClient), // invalidity of mail is checked first
			new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.MAIL_NOT_VALID.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, ErrorLogMessage.MAIL_NOT_VALID.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.MAIL_ALREADY_TAKEN.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, ErrorLogMessage.MAIL_ALREADY_TAKEN.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogBob = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.bob.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Bob", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Bob"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Bob").toString(),
		};
		Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 1).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Bob").toString(),
		};
		assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
		assert (new LogParser()).checkLogFile("LogClient_bob@gmail.com.log", expectedLogBob);
		assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
	}

	/**
	 * Test a successful signIn request.
	 */
	@Test
	public void signInOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);

		Object[] expectedLogAlice = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
			new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
			ClientState.LOGGED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
		};
		Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
		};
		assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
		assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
	}
	
	/**
	 * Test a failed signIn request.
	 */
    @Test
    public void signInKo() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.signIn("alice@toto.com", this.hashPwdClient, true);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);
        this.alice.signIn("alice@gmail.com", "test000", true);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);
        this.server.setRespondingToRequest(false);
        this.alice.signIn("alice@gmail.com", this.hashPwdClient, true);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_KO);

        Object[] expectedLogAlice = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@toto.com", this.hashPwdClient, true),
			new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", "test000", true),
			new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent()).toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, true),
			new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

	/**
	 * Test a successful signOut request.
	 */
    @Test
    public void signOutOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.signOut();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_OUT_OK);

        Object[] expectedLogClient = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
			new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
			ClientState.LOGGED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
			new Request(ProtocolCommand.SIGN_OUT),
			new Request(ProtocolCommand.SIGN_OUT_OK),
			ClientState.CONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_OUT_OK).toString()
        };
        Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogClient);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }
    
	/**
	 * Test a failed signOut request.
	 */
    @Test
    public void signOutKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.signOut();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_OUT_KO);

        Object[] expectedLogClient = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
			new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
			ClientState.LOGGED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
			new Request(ProtocolCommand.SIGN_OUT),
			new Request(ProtocolCommand.SIGN_OUT_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_OUT_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString()
        };
        Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogClient);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    
	}

    /**
     * Test a successful createSale request.
     */
	@Test
    public void createSaleOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
		final String title = "House";
		final String content = "Big house";
		final int price = 1000;
		final int id = 0;
		
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
		this.alice.domainList();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DOMAINS_LIST_OK);
        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);

        Object[] expectedLogAlice = {
			ClientState.DISCONNECTED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
			ClientState.CONNECTED,
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
			new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
			new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
			new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
			new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
			new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
			ClientState.LOGGED,
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
			new Request(ProtocolCommand.DOMAINS_LIST),
			new Request(ProtocolCommand.DOMAINS_LIST_OK, new Object[] {new Domain[] {Domain.HOUSE, Domain.CAR}}),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_OK, Arrays.toString(new Domain[] {Domain.HOUSE, Domain.CAR})).toString(),
			new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
			new Request(ProtocolCommand.CREATE_SALE_OK, title),
			new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, title).toString(),
        };
        Object[] expectedLogServer = {
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
			new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", title, content, Domain.HOUSE, price, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a failed createSale request.
     */
    @Test
    public void createSaleKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        final String title = "House";
        final String content = "Big house";
        final int price = 1000;

        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.domainList();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DOMAINS_LIST_OK);
        this.server.setRespondingToRequest(false);
        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_KO);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.DOMAINS_LIST),
            new Request(ProtocolCommand.DOMAINS_LIST_OK, (Object) new Domain[]{Domain.HOUSE, Domain.CAR}),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_OK, Arrays.toString(new Domain[]{Domain.HOUSE, Domain.CAR})).toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
            new Request(ProtocolCommand.CREATE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a successful updateSale request.
     */
    @Test
    public void updateSaleOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        final String title = "House";
        final String titleUpdated = "Beautiful House";
        final String content = "Big house";
        final String contentUpdated = "Very big house";
        final int price = 1000;
        final int priceUpdated = 1200;
        final int id = 0;

        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.updateAnnonce(titleUpdated, contentUpdated, priceUpdated, id);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.UPDATE_SALE_OK);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
            new Request(ProtocolCommand.CREATE_SALE_OK, title),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, title).toString(),
            new Request(ProtocolCommand.UPDATE_SALE, titleUpdated, contentUpdated, priceUpdated, id),
            new Request(ProtocolCommand.UPDATE_SALE_OK),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_UPDATED_OK).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", title, content, Domain.HOUSE, price, id).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_UPDATE_ANNONCE, "alice@gmail.com", titleUpdated, contentUpdated, Domain.HOUSE, priceUpdated, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a failed updateSale request.
     */
    @Test
    public void updateSaleKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        final String title = "House";
        final String titleUpdated = "Beautiful House";
        final String content = "Big house";
        final String contentUpdated = "Very big house";
        final int price = 1000;
        final int priceUpdated = 1200;
        final int id = 0;
        
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        
        this.bob = new Client("bob@gmail.com", "Bob", false, false);
        this.bob.openSocketToCentralServer();
        this.bob.requestPublicKeyOfCentralServer();
        given().ignoreExceptions().await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.bob.signUp("bob@gmail.com", "Bob", this.hashPwdClient);
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.bob.signIn("bob@gmail.com", this.hashPwdClient, false);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);

        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.bob.updateAnnonce(titleUpdated, contentUpdated, priceUpdated, id);
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.UPDATE_SALE_KO);
        this.server.setRespondingToRequest(false);
        this.alice.updateAnnonce(titleUpdated, contentUpdated, priceUpdated, id);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.UPDATE_SALE_KO);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
            new Request(ProtocolCommand.CREATE_SALE_OK, title),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, title).toString(),
            new Request(ProtocolCommand.UPDATE_SALE, titleUpdated, contentUpdated, priceUpdated, id),
            new Request(ProtocolCommand.UPDATE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_UPDATED_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogBob = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.bob.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "bob@gmail.com", "Bob", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "bob@gmail.com", "Bob"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "bob@gmail.com", "Bob").toString(),
            new Request(ProtocolCommand.SIGN_IN, "bob@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Bob"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "bob@gmail.com").toString(),
            new Request(ProtocolCommand.UPDATE_SALE, titleUpdated, contentUpdated, priceUpdated, id),
            new Request(ProtocolCommand.UPDATE_SALE_KO, ErrorLogMessage.NOT_OWNER.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_UPDATED_KO, ErrorLogMessage.NOT_OWNER.getContent()).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 1).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "bob@gmail.com", "Bob").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", title, content, Domain.HOUSE, price, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogClient_bob@gmail.com.log", expectedLogBob);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a successful deleteSale request.
     */
    @Test
    public void deleteSaleOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        final String titlle = "House";
        final String content = "Big house";
        final int price = 1000;
        final int id = 0;

        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.createSale(Domain.HOUSE, titlle, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.removeAnnonce(id);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DELETE_SALE_OK);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, titlle, content, price),
            new Request(ProtocolCommand.CREATE_SALE_OK, titlle),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, titlle).toString(),
            new Request(ProtocolCommand.DELETE_SALE, id),
            new Request(ProtocolCommand.DELETE_SALE_OK),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REMOVE_ANNONCE_OK).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", titlle, content, Domain.HOUSE, price, id).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_REMOVE_ANNONCE, "alice@gmail.com", titlle, content, Domain.HOUSE, price, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a failed deleteSale request.
     */
    @Test
    public void deleteSaleKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        final String title = "House";
        final String content = "Big house";
        final int price = 1000;
        final int id = 0;

        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        
        this.bob = new Client("bob@gmail.com", "Bob", false, false);
        this.bob.openSocketToCentralServer();
        this.bob.requestPublicKeyOfCentralServer();
        given().ignoreExceptions().await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.bob.signUp("bob@gmail.com", "Bob", this.hashPwdClient);
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.bob.signIn("bob@gmail.com", this.hashPwdClient, false);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);

        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.bob.removeAnnonce(id);
        await().until(() -> this.bob.getLastProcessedCommand() == ProtocolCommand.DELETE_SALE_KO);
        this.server.setRespondingToRequest(false);
        this.alice.removeAnnonce(id);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DELETE_SALE_KO);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
            new Request(ProtocolCommand.CREATE_SALE_OK, title),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, title).toString(),
            new Request(ProtocolCommand.DELETE_SALE, id),
            new Request(ProtocolCommand.DELETE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REMOVE_ANNONCE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogBob = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.bob.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "bob@gmail.com", "Bob", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "bob@gmail.com", "Bob"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "bob@gmail.com", "Bob").toString(),
            new Request(ProtocolCommand.SIGN_IN, "bob@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Bob"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "bob@gmail.com").toString(),
            new Request(ProtocolCommand.DELETE_SALE, id),
            new Request(ProtocolCommand.DELETE_SALE_KO, ErrorLogMessage.NOT_OWNER.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REMOVE_ANNONCE_KO, ErrorLogMessage.NOT_OWNER.getContent()).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 1).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "bob@gmail.com", "Bob").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", title, content, Domain.HOUSE, price, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogClient_bob@gmail.com.log", expectedLogBob);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a successful getDomainsList request.
     */
    @Test
    public void domainsListOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.domainList();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DOMAINS_LIST_OK);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[]{this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.DOMAINS_LIST),
            new Request(ProtocolCommand.DOMAINS_LIST_OK, (Object) new Domain[]{Domain.HOUSE, Domain.CAR}),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_OK, Arrays.toString(new Domain[]{Domain.HOUSE, Domain.CAR})).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    /**
     * Test a failed getDomainsList request.
     */
    @Test
    public void domainListKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
		this.alice.requestPublicKeyOfCentralServer();
		given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
		this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
		this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
		await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.server.setRespondingToRequest(false);
        this.alice.domainList();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DOMAINS_LIST_KO);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[]{this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.DOMAINS_LIST),
            new Request(ProtocolCommand.DOMAINS_LIST_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogServer = {
                new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
                new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        LogParser lp = new LogParser();
        assert lp.checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert lp.checkLogFile("LogServer.log", expectedLogServer);
    }

    @Test
    public void salesFromDomainOk() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        final String title = "House";
        final String content = "Big house";
        final int price = 1000;
        final int id = 0;

        this.alice.openSocketToCentralServer();
        this.alice.requestPublicKeyOfCentralServer();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.domainList();
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.DOMAINS_LIST_OK);
        this.alice.createSale(Domain.HOUSE, title, content, price);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.CREATE_SALE_OK);
        this.alice.salesFromDomain(Domain.HOUSE);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_OK);

        Object[] expectedLogAlice = {
            ClientState.DISCONNECTED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
            ClientState.CONNECTED,
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
            new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
            new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
            new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
            new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
            new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
            ClientState.LOGGED,
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
            new Request(ProtocolCommand.DOMAINS_LIST),
            new Request(ProtocolCommand.DOMAINS_LIST_OK, new Object[] {new Domain[] {Domain.HOUSE, Domain.CAR}}),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_OK, Arrays.toString(new Domain[]{Domain.HOUSE, Domain.CAR})).toString(),
            new Request(ProtocolCommand.CREATE_SALE, Domain.HOUSE, title, content, price),
            new Request(ProtocolCommand.CREATE_SALE_OK, title),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, title).toString(),
            new Request(ProtocolCommand.SALES_FROM_DOMAIN, Domain.HOUSE),
            new Request(ProtocolCommand.SALES_FROM_DOMAIN_OK, new Object[] {new Sale[] {new Sale("Alice", Domain.HOUSE, title, content, price, id)}}),
            new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_FROM_DOMAIN_OK, Arrays.toString(new Sale[]{new Sale("Alice", Domain.HOUSE, title, content, price, id)})).toString(),
        };
        Object[] expectedLogServer = {
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
            new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_ANNONCE, "alice@gmail.com", title, content, Domain.HOUSE, price, id).toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
    }

    @Test
    public void salesFromDomainKo() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.alice.openSocketToCentralServer();
        this.alice.requestPublicKeyOfCentralServer();
        given().ignoreExceptions().await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK);
        this.alice.signUp("alice@gmail.com", "Alice", this.hashPwdClient);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_UP_OK);
        this.alice.signIn("alice@gmail.com", this.hashPwdClient, false);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SIGN_IN_OK);
        this.alice.salesFromDomain(Domain.HOUSE);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_KO);
        this.server.setRespondingToRequest(false);
        this.alice.salesFromDomain(Domain.HOUSE);
        await().until(() -> this.alice.getLastProcessedCommand() == ProtocolCommand.SALES_FROM_DOMAIN_KO);

        Object[] expectedLogAlice = {
                ClientState.DISCONNECTED,
                new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString(),
                ClientState.CONNECTED,
                new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.alice.getPublicKey().getEncoded()),
                new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, new Object[] {this.server.getPk().getEncoded()}),
                new Request(ProtocolCommand.SIGN_UP, "alice@gmail.com", "Alice", this.hashPwdClient),
                new Request(ProtocolCommand.SIGN_UP_OK, "alice@gmail.com", "Alice"),
                new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, "alice@gmail.com", "Alice").toString(),
                new Request(ProtocolCommand.SIGN_IN, "alice@gmail.com", this.hashPwdClient, false),
                new Request(ProtocolCommand.SIGN_IN_OK, "Alice"),
                ClientState.LOGGED,
                new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, "alice@gmail.com").toString(),
                new Request(ProtocolCommand.SALES_FROM_DOMAIN, Domain.HOUSE),
                new Request(ProtocolCommand.SALES_FROM_DOMAIN_KO, ErrorLogMessage.NO_SALES_IN_THAT_DOMAIN.getContent()),
                new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_FROM_DOMAIN_KO, ErrorLogMessage.NO_SALES_IN_THAT_DOMAIN.getContent()).toString(),
                new Request(ProtocolCommand.SALES_FROM_DOMAIN, Domain.HOUSE),
                new Request(ProtocolCommand.SALES_FROM_DOMAIN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()),
                new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_FROM_DOMAIN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString(),
        };
        Object[] expectedLogServer = {
                new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, 0).toString(),
                new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, "alice@gmail.com", "Alice").toString(),
        };
        assert (new LogParser()).checkLogFile("LogClient_alice@gmail.com.log", expectedLogAlice);
        assert (new LogParser()).checkLogFile("LogServer.log", expectedLogServer);
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