package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.swing.JFrame;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import graphicalUI.GraphicalUI;
import graphicalUI.PerspectiveView;

import static org.awaitility.Awaitility.await;

import static java.nio.charset.StandardCharsets.UTF_8;

import server.Domain;
import server.Sale;
import logger.ErrorLogMessage;
import logger.InternalLogMessage;
import logger.TokenInternalLogMessage;
import protocol.ClientProcessRequestsFromCentralServer;
import protocol.ClientSendRequestsToCentralServer;
import protocol.ProtocolCommand;
import protocol.Request;
import protocol.RequestDeserializer;

/**
 * @mainpage Homepage
 * 
 * @section introduction 1. Introduction
 * 
 * BazAds is a platform for exchanging goods and services (denoted as sales) between individuals. A server stores and manages the sales of multiple clients. Clients can create, update, and delete sales.
 * 
 * @section protocol 2. Protocol
 * 
 * @subsection requests_client_to_server 2.1 Request from clients to the central server
 * 
 * @subsubsection public_key_of_central_server 2.1.1 Get the public key of the central server
 * 
 * All exchanges between a client and the central server are encrypted. Both the client and server has a public and private key pair. After exchanging public keys and performing key agreement, a shared secret is derived and then used to encrypt all subsequent communications.
 *
 * `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER (client's public key)`
 * 
 * | Variable                  | Type         |
 * | :-----------------------: | :----------: |
 * | Client's public key       | PublicKey    |
 * 
 * @mermaid{request_public_key_of_central_server}
 * 
 * Expected responses: @ref request_public_key_of_central_server_ok "`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK`" or @ref public_key_of_central_server_ko "`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO`"
 * 
 * @subsubsection sign_up 2.1.2 Sign up
 * 
 * A client has to send its mail, name, and password to sign up.
 * 
 * `SIGN_UP (mail, name, password)`
 * 
 * | Variable    | Type     |
 * | :---------: | :------: |
 * | mail        | String   |
 * | name        | String   |
 * | password    | String   |
 * 
 * @mermaid{sign_up}
 * 
 * Expected responses: @ref sign_up_ok "`SIGN_UP_OK`" or @ref sign_up_ko "`SIGN_UP_KO`"
 * 
 * @subsubsection sign_in 2.1.3 Sign in
 * 
 * A client has to send its mail and password to sign in. In addition, there is a boolean value to determine if, in case of successful sign in, the server send automatically the list of the dommain sales.
 * 
 * `SIGN_IN (mail, password, sendDomainList)`
 * 
 * | Variable        | Type      |
 * | :-------------: | :-------: |
 * | mail            | String    |
 * | password        | String    |
 * | sendDomainList  | boolean   |
 * 
 * @mermaid{sign_in}
 * 
 * Expected responses: @ref sign_in_ok "`SIGN_IN_OK`" or @ref sign_in_ko "`SIGN_IN_KO`"
 * 
 * @subsubsection sign_out 2.1.4 Sign out
 * 
 * A client just have to notify the server that he logs out.
 * 
 * `SIGN_OUT`
 * 
 * @mermaid{sign_out}
 * 
 * Expected responses: @ref sign_out_ok "`SIGN_OUT_OK`" or @ref sign_out_ko "`SIGN_OUT_KO`"
 * 
 * @subsubsection create_sale 2.1.5 Create a sale
 * 
 * A client has to send the domain, title, content, and price of the new sale.
 * 
 * `CREATE_SALE (domain, title, content, price)`
 * 
 * | Variable    | Type      |
 * | :---------: | :-------: |
 * | domain      | Domain    |
 * | title       | String    |
 * | content     | String    |
 * | price       | int       |
 * 
 * @mermaid{create_sale}
 * 
 * Expected responses: @ref create_sale_ok "`CREATE_SALE_OK`" or @ref create_sale_ko "`CREATE_SALE_KO`"
 * 
 * @subsubsection update_sale 2.1.6 Update a sale
 * 
 * A client has to send the new title, new content, new price, and id of the sale being updated. The domain can't be updated. A client can only update its own sales.
 * 
 * `UPDATE_SALE (title, content, price, id)`
 * 
 * | Variable    | Type      |
 * | :---------: | :-------: |
 * | title       | String    |
 * | content     | String    |
 * | price       | int       |
 * | id          | int       |
 * 
 * @mermaid{update_sale}
 * 
 * Expected responses: @ref update_sale_ok "`UPDATE_SALE_OK`" or @ref update_sale_ko "`UPDATE_SALE_KO`"
 * 
 * @subsubsection remove_sale 2.1.7 Delete a sale
 * 
 * A client has to send the id of the sale to delete. A client can only delete its own sales.
 * 
 * `DELETE_SALE (id)`
 * 
 * | Variable    | Type      |
 * | :---------: | :-------: |
 * | id          | int       |
 * 
 * @mermaid{delete_sale}
 * 
 * Expected responses: @ref delete_sale_ok "`DELETE_SALE_OK`" or @ref delete_sale_ko "`DELETE_SALE_KO`"
 * 
 * @subsubsection domain_list 2.1.8 Get the list of domains sales
 * 
 * A client just have to notify the server to the server that he wants the list of the domains.
 *
 * `DOMAINS_LIST`
 *
 * @mermaid{domains_list}
 * 
 * Expected responses: @ref domain_list_ok "`DOMAINS_LIST_OK`" or @ref domain_list_ko "`DOMAINS_LIST_KO`"
 *
 * @subsubsection sales_from_domain 2.1.9 Get the sales from a domain
 * 
 * A client has to send the domain to get the list of sales.
 * 
 * `SALES_FROM_DOMAIN (domain)`
 * 
 * | Variable    | Type      |
 * | :---------: | :-------: |
 * | domain      | Domain    |
 * 
 * @mermaid{sales_from_domain}
 *  
 * @subsection requests_server_to_client 2.2 Request from the central server to clients
 * 
 * @subsubsection request_public_key_of_central_server_ok 2.2.1 Successful exchange of public key
 * 
 * The server send its public key to the client.
 * 
 * `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK (server's public key)`
 *
 * | Variable            | Type       |
 * | :-----------------: | :--------: |
 * | Server's public key | PublicKey  |
 * 
 * Answer to: @ref public_key_of_central_server "`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER`".
 * 
 * @subsubsection public_key_of_central_server_ko 2.2.2 Failed to exchange of public key
 * 
 * The server send an error message to the client.
 * 
 * `REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to: @ref public_key_of_central_server "`REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER`".
 * 
 * @subsubsection sign_up_ok 2.2.3 Successful sign up
 * 
 * The server send the confirmation of sign up with the mail and the name of the client.
 *
`* SIGN_UP_OK(mail, name)`
 *
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | mail        | String     |
 * | name        | String     |
 *
 * Answer to request @ref sign_up "`SIGN_UP`".
 * 
 * @subsubsection sign_up_ko 2.2.4 Failed to sign up
 * 
 * The server send an error message to the client.
 * 
 * `SIGN_UP_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * - The mail is already used by another client.
 * - The mail is not valid (e.g., doesn't contain '@' or '.').
 * - The name is not valid (e.g., forbidden names).
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref sign_up "`SIGN_UP`".
 * 
 * @subsubsection sign_in_ok 2.2.5 Successful sign in
 * 
 * The server send the confirmation of sign in with the name of the client.
 * 
 * `SIGN_IN_OK(name)`
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | name        | String     |
 * 
 * Answer to request @ref sign_in "`SIGN_IN`".
 * 
 * @subsubsection sign_in_ko 2.2.6 Failed to sign in
 * 
 * The server send an error message to the client.
 * 
 * `SIGN_IN_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * - The mail is not registered.
 * - The password is not correct.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 *
 * Answer to request @ref sign_in "`SIGN_IN`".  
 * 
 * @subsubsection sign_out_ok 2.2.7 Successful sign out
 * 
 * The server send the confirmation of sign out.
 * 
 * `SIGN_OUT_OK`
 * 
 * Answer to request @ref sign_out "`SIGN_OUT`".
 * 
 * @subsubsection sign_out_ko 2.2.8 Failed to sign out
 * 
 * The server send an error message to the client.
 * 
 * `SIGN_OUT_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref sign_out "`SIGN_OUT`".
 * 
 * @subsubsection create_sale_ok 2.2.9 Successful creation of a sale
 * 
 * The server send the confirmation of the creation of a sale with the title of the sale.
 * 
 * `CREATE_SALE_OK(title)`
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | title       | String     |
 * 
 * Answer to request @ref create_sale "`CREATE_SALE`".
 * 
 * @subsubsection create_sale_ko 2.2.10 Failed to creation of a sale
 * 
 * The server send an error message to the client.
 * 
 * `CREATE_SALE_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref create_sale "`CREATE_SALE`".
 * 
 * @subsubsection update_sale_ok 2.2.11 Successful update of a sale
 * 
 * The server send the confirmation of the update of the sale.
 * 
 * `UPDATE_SALE_OK`
 * 
 * Answer to request @ref update_sale "`UPDATE_SALE`".
 * 
 * @subsubsection update_sale_ko 2.2.12 Failed to update of a sale
 * 
 * The server send an error message to the client.
 * 
 * `UPDATE_SALE_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * - The client is not the owner of the sale.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref update_sale "`UPDATE_SALE`".
 * 
 * @subsubsection delete_sale_ok 2.2.13 Successful deletion of a sale
 * 
 * The server send the confirmation of the deletion of the sale.
 * 
 * `DELETE_SALE_OK`
 * 
 * Answer to request @ref remove_sale "`DELETE_SALE`".
 * 
 * @subsubsection delete_sale_ko 2.2.14 Failed to deletion of a sale
 * 
 * The server send an error message to the client.
 * 
 * `DELETE_SALE_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * - The client is not the owner of the sale.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref remove_sale "`DELETE_SALE`".
 * 
 * @subsubsection domain_list_ok 2.2.15 Successful get of the domains list
 * 
 * The server send the list of the domains.
 * 
 * `DOMAINS_LIST_OK(domains)`
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | domains     | Domain[]   |
 * 
 * Answer to request @ref domain_list "`DOMAINS_LIST`".
 * 
 * @subsubsection domain_list_ko 2.2.16 Failed to get of the domains list
 * 
 * The server send an error message to the client.
 * 
 * `DOMAINS_LIST_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref domain_list "`DOMAINS_LIST`".
 * 
 * @subsubsection sales_from_domain_ok 2.2.17 Successful get of the sales from a domain
 * 
 * The server send the list of the sales from a domain.
 * 
 * `SALES_FROM_DOMAIN_OK(sales)`
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | sales       | Sale[]     |
 * 
 * Answer to request @ref sales_from_domain "`SALES_FROM_DOMAIN`".
 * 
 * @subsubsection sales_from_domain_ko 2.2.18 Failed to get of the sales from a domain
 * 
 * The server send an error message to the client.
 * 
 * `SALES_FROM_DOMAIN_KO (message)`
 * 
 * Reason of failure:
 * - The server process the incoming request but don't answer to it.
 * 
 * | Variable    | Type       |
 * | :---------: | :--------: |
 * | message     | String     |
 * 
 * Answer to request @ref sales_from_domain "`SALES_FROM_DOMAIN`".
 *
 * @section next_steps 3. Next Steps
 * 
 * - Implement the UDP server of the client.
 */

/**
 * @brief Client class.
 */
public class Client implements Runnable, ClientSendRequestsToCentralServer, ClientProcessRequestsFromCentralServer {
    private static final boolean TEST = true;
    private String mail;
    private String name;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ClientState state;
    private Thread receiverFromCentralServer;
    private Thread receiverUDPServer;
    private KeyPair publicPrivateKey;
    private SecretKey sharedSecret;
    private boolean stopReceiveFromCentralServer;
    private boolean stop;
    private Logger logger;
    private boolean hasUDPServer;
    private ProtocolCommand lastProcessedCommand;
    private JFrame frame;
    private GraphicalUI gui;
    private Sale[] annonces;
    private Domain[] domains;
    private HashMap<String, String> messages;
    private DatagramSocket serverUDP;
    private HashMap <String, IpAddressUDPServer> ipAddressesOfOtherUDPServers;

    /**
     * @brief Initialize all attributes of a Client. Generate also its the public and private key pair.
     * 
     * @param mail The mail of the client.
     * @param name The name of the client.
     * @param visibleFrame A boolean to set the visibility of the client application.
     * @param UDPServer A boolean to set if the client has an UDP server.
     */
    public Client(String mail, String name, boolean visibleFrame, boolean hasUDPServer) throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException {
        this.logger = Logger.getLogger("LogClient_" + mail);
        FileHandler fileHandler = new FileHandler("LogClient_" + mail + ".log");
        fileHandler.setFormatter(new SimpleFormatter());
        this.logger.addHandler(fileHandler);
        this.logger.setUseParentHandlers(false); // avoid printing to console        
        this.state = ClientState.DISCONNECTED;
        this.logger.info("[STATUS] " + this.state);
        this.hasUDPServer = hasUDPServer;
        this.ipAddressesOfOtherUDPServers = new HashMap<>();
        this.messages = new HashMap<>();
        this.messages.put("Console", "");
        this.initializePrivatePublicKey();
        this.stop = false;
        this.socket = null;
        this.frame = new JFrame("App");
        this.gui = new GraphicalUI(this);
        this.frame.setContentPane(this.gui.getPane());
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setVisible(visibleFrame);
    }

    /**
     * @brief Open the socket from the clients to the central server that store and manage the sales.
     * 
     * Default address is 127.0.0.1 and port is 1234.
     */
    public void openSocketToCentralServer() throws IOException {
        try {
            this.socket = new Socket("127.0.0.1", 4321);
        } catch (IOException e) {
            this.printMessageToLoggerAndClientConsole("Server unrechable");
        }
        if (this.socket != null) {
            this.dis = new DataInputStream(this.socket.getInputStream());
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            this.logger.info(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString());
            this.printMessageToClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_OPEN).toString());
            this.setState(ClientState.CONNECTED);
            this.getGui().updateViewGUI(PerspectiveView.CONNECTED);
            this.logger.info("[STATUS] " + this.state);
            this.receiverFromCentralServer = new Thread(() -> {
                while (!this.stopReceiveFromCentralServer) {
                    try {
                        Request request = receiveRequestCentralServer();
                        if (request == null) {
                            this.stopReceiveFromCentralServer = true;
                            continue;
                        }
                        processRequest(request);
                        
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                    ClassNotFoundException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                    IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            this.stopReceiveFromCentralServer = false;
            this.receiverFromCentralServer.start();
        }
    }
    
    /**
     * @brief Close the socket to the central server.
     */
    public void closeSocketToCentralServer() throws IOException, NoSuchAlgorithmException {
        if (this.socket != null) {
            this.sharedSecret = null;
            this.initializePrivatePublicKey(); // reinitialize to have different secret key at the next connection
            this.receiverFromCentralServer.interrupt();
            this.dis.close();
            this.dos.close();
            this.socket.close();
            this.logger.info(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_CLOSED).toString());
            this.printMessageToClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SOCKET_CLOSED).toString());
            this.state = ClientState.DISCONNECTED;
            this.logger.info("[STATUS] " + this.state);
        }
    }

    /**
     * @brief Read and deserialize a byte array (received from the central server) into a request object.
     */
    private Request receiveRequestCentralServer() throws IOException, ClassNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int inRequestBytesLength = this.dis.readInt();
        byte[] inRequestBytes = this.dis.readNBytes(inRequestBytesLength);
        String requestString = new String(inRequestBytes);
        Request inRequest;
        if (!requestString.contains("command")) { // a naive test to check if the message is encrypted
            byte[] initializationVector = Arrays.copyOfRange(inRequestBytes, 0, 12); // extract the initialization vector which is by convention the first 12 bytes
            requestString = new String(Arrays.copyOfRange(inRequestBytes, 12, inRequestBytes.length));
            String decryptedRequestStr = decrypt("AES/GCM/NoPadding", requestString, this.sharedSecret, initializationVector);
            Gson gsonRequest = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            inRequest = gsonRequest.fromJson(decryptedRequestStr, Request.class);
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            inRequest = gson.fromJson(requestString, Request.class);
        }
        this.logger.info("[RECEIVE] " + inRequest);
        return inRequest;
    }

    /**
     * @brief Serialize a request object into a byte array and send it to the central server.
     */
    private void sendRequestCentralServer(Request outRequest) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (this.socket == null)
            this.openSocketToCentralServer();
        else {
            this.logger.info("[SEND] " + outRequest);
            if ((outRequest.getCommand() != ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK) && (outRequest.getCommand() != ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER)) {
                // encrypt any time expect for the request to exchange public key (because the shared secret is not yet established)
                byte[] initializationVector = this.generate12Bytes();
                String outRequestEncrypted = encrypt("AES/GCM/NoPadding", new Gson().toJson(outRequest), this.sharedSecret, initializationVector);
                this.dos.writeInt(initializationVector.length + outRequestEncrypted.getBytes().length);
                this.dos.write(initializationVector);
                this.dos.writeBytes(outRequestEncrypted);
            } else {
                this.dos.writeInt(new Gson().toJson(outRequest).getBytes().length);
                this.dos.writeBytes(new Gson().toJson(outRequest));
            }
            this.dos.flush();
        }
    }
    
    /** 
     * @brief Read and deserialize a byte array (received on the UDP server of the client) into a request object.
     */
    private Request receiveUDPRequest() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] inRequestBytes = new byte[1024];
        DatagramPacket inRequestDatagram = new DatagramPacket(inRequestBytes, inRequestBytes.length);
        this.serverUDP.receive(inRequestDatagram);
        int inRequestLength = inRequestDatagram.getLength();
        String inRequestString = new String(inRequestDatagram.getData(), 0, inRequestLength);
        Request inRequest;
        if (!inRequestString.contains("command")) {
            byte[] iv = Arrays.copyOfRange(inRequestBytes, 0, 12);
            String s = decrypt("AES/GCM/NoPadding", inRequestString, this.sharedSecret, iv);
            Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            inRequest = gson.fromJson(s, Request.class);
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            inRequest = gson.fromJson(inRequestString, Request.class);
        }
        return inRequest;
    }

    /*
     * @brief Serialize a request object into a byte array and send it to the UDP server of another client.
     */
    public void sendUDPRequest(String dest, String msg) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (!this.ipAddressesOfOtherUDPServers.containsKey(dest)) {
            this.requestUDPCoordinate(dest);
            await().until(() -> this.ipAddressesOfOtherUDPServers.containsKey(dest));
        }
        Request outRequest = new Request(ProtocolCommand.UDP_MSG, dest, msg);
        this.logger.info("[SEND] " + outRequest);
        byte[] initializationVector = this.generate12Bytes();
        String outRequestEncrypted = encrypt("AES/GCM/NoPadding", new Gson().toJson(outRequest), this.sharedSecret, initializationVector);
        byte[] outRequestEncryptedBytes = new byte[initializationVector.length + outRequestEncrypted.getBytes().length];
        System.arraycopy(initializationVector, 0, outRequestEncryptedBytes, 0, initializationVector.length);
        System.arraycopy(outRequestEncrypted.getBytes(), 0, outRequestEncryptedBytes, initializationVector.length, outRequestEncrypted.getBytes().length);
        this.serverUDP.send(new DatagramPacket(outRequestEncryptedBytes, outRequestEncryptedBytes.length, InetAddress.getByName(this.ipAddressesOfOtherUDPServers.get(dest).getAddr()), this.ipAddressesOfOtherUDPServers.get(dest).getPort()));
    }
    
    /**
     * @brief Call the appropriate method to process a request received from the central server.
     */
    public void processRequest(Request inRequest) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        switch (inRequest.getCommand()) {
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO -> this.requestPublicKeyKo(inRequest);
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK -> this.requestPublicKeyOk(inRequest);
            case SIGN_UP_OK -> this.signUpOk(inRequest);
            case SIGN_UP_KO -> this.signUpKo(inRequest);
            case SIGN_IN_OK -> this.signInOk(inRequest);
            case SIGN_IN_KO -> this.signInKo(inRequest);
            case SIGN_OUT_OK -> this.signOutOk();
            case SIGN_OUT_KO -> this.signOutKo();
            case CREATE_SALE_OK -> this.createAnnonceOk(inRequest);
            case CREATE_SALE_KO -> this.createAnnonceKo(inRequest);
            case UPDATE_SALE_OK -> this.updateAnonceOk(inRequest);
            case UPDATE_SALE_KO -> this.updateAnonceKo(inRequest);
            case DOMAINS_LIST_OK -> this.domainListOk(inRequest);
            case DOMAINS_LIST_KO -> this.domainListKo(inRequest);
            case SALES_FROM_DOMAIN_OK -> this.annonceFromDomainOk(inRequest);
            case SALES_FROM_DOMAIN_KO -> this.annonceFromDomainKo(inRequest);
            case DELETE_SALE_OK -> this.removeAnnonceOk(inRequest);
            case DELETE_SALE_KO -> this.removeAnnonceKo(inRequest);
            case UDP_SERVER_OK -> this.udpServerOk(inRequest);
            case UDP_SERVER_KO -> this.udpServerKo(inRequest);
            default -> throw new UnsupportedOperationException("Unimplemented case");
        }
        this.lastProcessedCommand = inRequest.command; // store the last processed command for testing purposes
    }

    /**
     * @brief Encrypt a string.
     */
    private String encrypt(String algorithm, String plain, SecretKey key, byte[] initializationVector) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, initializationVector));
        cipher.updateAAD("v1".getBytes(UTF_8));
        byte[] cipherText = cipher.doFinal(plain.getBytes());
        return new String(Base64.encodeBase64(cipherText));
    }

    /**
     * @brief Decrypt a string.
     */
    private String decrypt(String algorithm, String cipherText, SecretKey key, byte[] initializationVector) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, initializationVector));
        cipher.updateAAD("v1".getBytes(UTF_8));
        byte[] plainText = cipher.doFinal(java.util.Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    /*
     * @brief Initialize the public and private key pair.
     */
    private void initializePrivatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        this.publicPrivateKey = kpg.generateKeyPair();
    }

    /**
     * @brief Perform the key agreement with the public key received from the server.
     */
    private void performKeyAgreement(Request req) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] otherPublicKeyBytes = (byte[]) req.getParams().get("PublicKey");
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey otherPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(otherPublicKeyBytes));
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(this.publicPrivateKey.getPrivate());
        keyAgreement.doPhase(otherPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(this.publicPrivateKey.getPublic().getEncoded()), ByteBuffer.wrap(otherPublicKeyBytes));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));
        byte[] derivedKey = hash.digest(); // derived key from the shared secret
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(encodeHexString(derivedKey).toCharArray(), "key".getBytes(), 65536, 256);
        this.sharedSecret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /*
     * @brief Convert a byte to a hexadecimal string.
     */
    private String byteToHex(byte b) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((b >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((b & 0xF), 16);
        return new String(hexDigits);
    }

    /*
     * @brief Convert a byte array to a hexadecimal string.
     */
    private String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray)
            hexStringBuffer.append(byteToHex(b));
        return hexStringBuffer.toString();
    }

    /*
     * @brief Generate random 12 bytes.
     */
    public byte[] generate12Bytes() throws NoSuchAlgorithmException, IOException {
        if (TEST) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] pwdHash = digest.digest("initVect".getBytes(StandardCharsets.UTF_8)); // the input is hardcoded for unit testing
            return Arrays.copyOfRange(pwdHash, 0, 12);
        }
        else {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            return iv;
        }
    }

    public void printMessageToClientConsole(String msg) {
        msg = msg.substring(11); // remove "[INTERNAL]" which is used only in the logger
        if (this.messages.get("Console").equals(""))
            this.messages.put("Console", msg);
        else
            this.messages.put("Console", this.messages.get("Console") + "\n" + msg);
        this.gui.highlightDst("Console");
    }
    
    private void printMessageToLoggerAndClientConsole(String msg) {
        this.logger.info(msg);
        this.printMessageToClientConsole(msg);
    }
    
    /*
     * @brief Create a request to exchange the public key with the central server.
     */
    @Override
    public void requestPublicKeyOfCentralServer() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER, this.publicPrivateKey.getPublic().getEncoded()));
    }

    /*
     * @brief Create a request to sign up.
     */
    @Override
    public void signUp(String mail, String name, String pwd) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.SIGN_UP, mail, name, pwd));
    }

    /*
     * @brief Create a request to sign in.
     */
    @Override
    public void signIn(String mail, String pwd, boolean sendDomainList) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException { 
        this.mail = mail;
        this.sendRequestCentralServer(new Request(ProtocolCommand.SIGN_IN, mail, pwd, sendDomainList));
    }

    /*
     * @brief Create a request to sign out.
     */
    @Override
    public void signOut() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.SIGN_OUT));
    }

    /*
     * @brief Create a request to create a sale.
     */
    @Override
    public void createSale(Domain dom, String title, String descriptif, int price) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.CREATE_SALE, dom, title, descriptif, price));
    }

    /*
     * @brief Create a request to update a sale.
     */
    @Override
    public void updateAnnonce(String title, String descriptif, int price, int id) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.UPDATE_SALE, title, descriptif, price, id));
    }

    /*
     * @brief Create a request to remove a sale.
     */
    @Override
    public void removeAnnonce(int id) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.DELETE_SALE, id));
    }
    
    /*
     * @brief Create a request to get the domain list.
     */
    @Override
    public void domainList() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.DOMAINS_LIST));
    }

    /*
     * @brief Create a request to get the sales from a domain.
     */
    @Override
    public void salesFromDomain(Domain domain) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.SALES_FROM_DOMAIN, domain));
    }

    /*
     * @brief Create a request to get the coordinates of the UDP server of another client.
     */
    @Override
    public void requestUDPCoordinate(String mail) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.sendRequestCentralServer(new Request(ProtocolCommand.REQUEST_UDP_COORDINATES, mail));
    }

    /*
     * @brief Process the successfull exchange of public key with the central server.
     */
    @Override
    public void requestPublicKeyOk(Request req) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        this.performKeyAgreement(req);
    }  

    /*
     * @brief Process the Failed to exchange of public key with the central server.
     */
    @Override
    public void requestPublicKeyKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REQUEST_PUBLIC_KEY_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the successfull sign up.
     */
    @Override
    public void signUpOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_OK, req.getParams().get("Mail"), req.getParams().get("Name")).toString());
    }

    /*
     * @brief Process the Failed to sign up.
     */
    @Override
    public void signUpKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_UP_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the successfull sign in.
     */
    @Override
    public void signInOk(Request req) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.name = (String) req.getParams().get("Name");
        if (this.hasUDPServer) {
            if (TEST) {
                if (this.name.equals("Alice"))
                    this.serverUDP = new DatagramSocket(12341);
                else
                    this.serverUDP = new DatagramSocket(12342);
            } else
                this.serverUDP = new DatagramSocket();
            this.receiverUDPServer = new Thread(() -> {
                while (!this.stopReceiveFromCentralServer) {
                    try {
                        Request request = receiveUDPRequest();
                        processRequest(request);
                    } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                             IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                             InvalidKeyException | InvalidKeySpecException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            this.receiverUDPServer.start();
            Request udpRequest = new Request(ProtocolCommand.UDP_SERVER, InetAddress.getLocalHost().getHostAddress(), this.serverUDP.getLocalPort());
            this.sendRequestCentralServer(udpRequest);
        }
        this.gui.updateViewGUI(PerspectiveView.LOGGED);
        this.setState(ClientState.LOGGED);
        this.logger.info("[STATUS] " + this.state);
        this.logger.info(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, this.mail).toString());
        this.printMessageToClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_OK, this.mail).toString());
    }

    /*
     * @brief Process the Failed to sign in.
     */
    @Override
    public void signInKo(Request req) {
        this.mail = "";
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_IN_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the successfull sign out.
     */
    @Override
    public void signOutOk() {
        this.mail = "";
        this.name = "";
        this.setState(ClientState.CONNECTED);
        this.logger.info("[STATUS] " + this.state);
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_OUT_OK).toString());
    }

    /*
     * @brief Process the Failed to sign out.
     */
    @Override
    public void signOutKo() {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_SIGN_OUT_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()).toString());
    }

    /*
     * @brief Process the successfull creation of a sale.
     */
    @Override
    public void createAnnonceOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_OK, req.getParams().get("Title")).toString());
    }

    /*
     * @brief Process the Failed to creation of a sale.
     */
    @Override
    public void createAnnonceKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_CREATED_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the successfull update of a sale.
     */
    @Override
    public void updateAnonceOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_UPDATED_OK).toString());
    }

    /*
     * @brief Process the Failed to update of a sale.
     */
    @Override
    public void updateAnonceKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_UPDATED_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the succesfull request to get the domain list.
     */
    @Override
    public void domainListOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_OK, Arrays.toString((Domain[]) req.getParams().get("Domains"))).toString());
        this.gui.setDomainList((Domain[]) req.getParams().get("Domains"));
    }

    /*
     * @brief Process the Failed to request to get the domain list.
     */
    @Override
    public void domainListKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_DOMAIN_LIST_KO, req.getParams().get("Error")).toString());
    }

    /*
     * @brief Process the successfull request to get the sales from a domain.
     */
    @Override
    public void annonceFromDomainOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_FROM_DOMAIN_OK, Arrays.toString((Sale[]) req.getParams().get("AnnoncesFromDomain"))).toString());
        this.annonces = (Sale[]) req.getParams().get("AnnoncesFromDomain");
        this.gui.updateAnnonceList();
    }

    /*
     * @brief Process the Failed to request to get the sales from a domain.
     */
    @Override
    public void annonceFromDomainKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_ANNONCE_FROM_DOMAIN_KO, req.getParams().get("Error")).toString());
        this.annonces = new Sale[]{}; // empty the list of annonces
        this.gui.updateAnnonceList();
    }

    /*
     * @brief Process the successfull request to remove a sale.
     */
    @Override
    public void removeAnnonceOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REMOVE_ANNONCE_OK).toString());
    }

    /*
     * @brief Process the Failed to request to remove a sale.
     */
    @Override
    public void removeAnnonceKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_REMOVE_ANNONCE_KO, req.getParams().get("Error")).toString());
    }

    @Override
    public void udpServerOk(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_UDP_SERVER_OK).toString());
    }

    @Override
    public void udpServerKo(Request req) {
        this.printMessageToLoggerAndClientConsole(new InternalLogMessage(TokenInternalLogMessage.CLIENT_LOG_UDP_SERVER_KO, req.getParams().get("Error")).toString());
    }

    @Override
    public void requestUDPServerOk(Request req) {
        this.ipAddressesOfOtherUDPServers.put((String) req.getParams().get("Mail"), (IpAddressUDPServer) req.getParams().get("Coord"));
    }

    @Override
    public void requestUDPServerKO(Request req) {
        //TODO
    }

    @Override
    public void notHandledRequest() {
        //TODO
    }

    public static void main (String[] args) throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InterruptedException {
        new Thread(new Client("alice@gmail.com", "Alice", true, false)).start();
    }

    @Override
    public void run() {
        while (!this.stop) {
            
        }
    }
    
    public void setDomainsList(Domain[] domains) {
        this.domains = domains;
    }

    public void setState(ClientState state) {
        this.state = state;
        this.gui.setStatusGUI(state);
    }
    
    public void stop() {
        this.stop = true;
    }
    public String getMail() {
        return this.mail;
    }
    
    public String getName() {
        return this.name;
    }

    public GraphicalUI getGui() {
        return this.gui;
    }

    public Sale[] getAnnonces() {
        return this.annonces;
    }
    
    public PublicKey getPublicKey() {
        return this.publicPrivateKey.getPublic();
    }
    
    public HashMap<String, String> getMessages() {
        return this.messages;
    }
    
    public String getMessage(String dst) {
        return this.messages.get(dst);
    }
    
    public Domain[] getDomains() {
        return this.domains;
    }
    
    /**
     * @brief Get the last processed command for testing purposes.
     */
    public ProtocolCommand getLastProcessedCommand() {
        return this.lastProcessedCommand;
    }
}    