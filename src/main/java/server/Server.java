package server;

import java.io.IOException;

import java.net.ServerSocket;

import java.nio.charset.StandardCharsets;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import client.IpAddressUDPServer;
import logger.InternalLogMessage;
import logger.TokenInternalLogMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @brief This class manages the sales. It is responsible for creating, updating, and deleting sales.
 * 
 * @details The sales manipulations are transmitted to the server via the ClientHandler class.
 * The passwords are stored in a hashed form.
 * The server can be initialized with some data for testing purposes.
 */
public class Server implements Runnable {
    private int clientHandlerId = 0;
    private int annonceId = 0;
    private KeyPair publicPrivateKey;
    private ServerSocket server;
    private ArrayList<ClientHandler> clientsHandler;
    private ArrayList<Client> clients;
    private ArrayList<Sale> sales;
    private ArrayList<Domain> domains;
    private boolean isRespondingToRequest;
    private boolean stop;
    private Logger logger;
    private FileHandler fh;
    private Map<String, IpAddressUDPServer> ipAddressesOfUDPServers;

    public Server(boolean isRespondingToRequest, boolean unitTest) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.initializePrivatePublicKey();
        this.server = new ServerSocket(4321);
        this.server.setReuseAddress(true);
        this.clientsHandler = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.sales = new ArrayList<>();
        this.domains = new ArrayList<>();
        this.ipAddressesOfUDPServers = new HashMap<>();
        this.isRespondingToRequest = isRespondingToRequest;
        this.stop = false;
        this.logger = Logger.getLogger("LogServer");
        fh = new FileHandler("LogServer.log");
        this.logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        this.logger.setUseParentHandlers(false); // do not print on console
        this.clientHandlerId = 0;
        this.annonceId = 0;
        this.domains.add(Domain.HOUSE);
        this.domains.add(Domain.CAR);
        if (unitTest) { // initialize the server with some data
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] pwdHash = digest.digest("test".getBytes(StandardCharsets.UTF_8));
            this.addClient("alice@gmail.com", "Alice", "test");
            this.addClient("bob@gmail.com", "Bob", Arrays.toString(pwdHash));
            this.sales.add(new Sale("alice@gmail.com", Domain.HOUSE, "Big House", "Content of the annonce", 1000, 0));
            this.sales.add(new Sale("bob@gmail.com", Domain.HOUSE, "Big House 2", "Content of the annonce 2", 2000, 1));
        }
    }

    /*
     * @brief Initialize the public and private key pair.
     */
    private void initializePrivatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        this.publicPrivateKey = kpg.generateKeyPair();
    }

    private String getEncryptedPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;
        byte[] saltBytes = salt.getBytes();
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);
        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
        byte[] encBytes = f.generateSecret(spec).getEncoded();
        return Arrays.toString(encBytes);
    }

    private String getSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Arrays.toString(salt);
    }

    public void closeConnection() throws IOException {
        this.server.close();
    }

    public void addClient(String mail, String name, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String salt = this.getSalt();
        String encryptedPassword = this.getEncryptedPassword(password, salt);
        this.clients.add(new Client(mail, name, encryptedPassword, salt));
        this.logger.info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_CREATED, mail, name).toString());
    }

    public boolean isMailRegister(String mail) {
        for (Client cs : this.clients)
            if (cs.getMail().equals(mail))
                return true;
        return false;
    }

    public Client getClientFromMail(String mail) {
        for (Client cs : this.clients)
            if (cs.getMail().equals(mail))
                return cs;
        return null;
    }

    public boolean isNameValid(String name) {
        String[] forbiddenName = {"Prout"};
        for (String s: forbiddenName)
            if (s.equals(name))
                return false;
        return true;
    }

    public boolean isMailValid(String mail) {
        String expression = "^[\\w.+\\-]+@gmail\\.com$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    public boolean isPasswordValid(String mail, String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Client cs = this.getClientFromMail(mail);
        String salt = cs.getSalt();
        String computedEncryptedPwd = this.getEncryptedPassword(pwd, salt);
        return computedEncryptedPwd.equals(cs.getPwd());
    }

    public void stopProcess() throws IOException {
        this.stop = true;
        this.server.close();
    }

    public synchronized void addSale(String mail, Domain domain, String title, String content, int price) {
        Sale newSale = new Sale(mail, domain, title, content, price, this.annonceId);
        this.sales.add(newSale);
        this.logger.info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CREATE_SALE, mail, title, content, domain, price, this.annonceId).toString());
        this.annonceId++;
    }

    public synchronized void updateSale(String title, String descriptif, int price, int id) {
        for (Sale sale: this.sales)
            if (sale.getId() == id) {
                sale.setTitle(title);
                sale.setDescriptif(descriptif);
                sale.setPrice(price);
                this.logger.info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_UPDATE_SALE, sale.getOwner(), sale.getTitle(), sale.getContent(), sale.getDomain(), sale.getPrice(), sale.getId()).toString());
                break;
            }
    }
    
    /**
     * @brief Delete a sale.
     * 
     * @param id The id of the sale to delete.
     */
     public synchronized void deleteSale(int id) {
        for (Sale a: this.sales)
            if (a.getId() == id)
                this.logger.info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_DELETE_SALE, a.getOwner(), a.getTitle(), a.getContent(), a.getDomain(), a.getPrice(), a.getId()).toString());
        this.sales.removeIf(a -> a.getId() == id);
    }

    public boolean existUPCoordinate(String mail) {
        return this.ipAddressesOfUDPServers.containsKey(mail);
    }

    /**
     * @brief Check if the user is the owner of the sale.
     
     * @param mail The mail of the user.
     * @param id The id of the sale.
     * @return True if the user is the owner of the sale, false otherwise.
     */
    public boolean isOwnerOfSale(String mail, int id) {
        for (Sale sale: this.sales)
            if (sale.getOwner().equals(mail) && sale.getId() == id)
                return true;
        return false;
    }

    public Sale[] getSalesOfDomain(Domain domain) {
        ArrayList<Sale> salesFiltered = new ArrayList<>();
        for (Sale sale: this.sales) {
           String nameFromMail = this.clients.stream().filter(cs -> cs.getMail().equals(sale.getOwner())).findFirst().map(Client::getName).orElse(null);
            salesFiltered.add(new Sale(nameFromMail, sale.getDomain(), sale.getTitle(), sale.getContent(), sale.getPrice(), sale.getId()));
        }
        salesFiltered.removeIf(a -> a.getDomain() != domain);
        return salesFiltered.toArray(new Sale[0]);
    }

    public synchronized void addUDPIpAddress(String mail, String addr, int port) {
        this.ipAddressesOfUDPServers.put(mail, new IpAddressUDPServer(addr, port));
    }

    public static void main(String[] arg) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeySpecException {
        new Thread(new Server(true, true)).start();
    }

    @Override
    public void run() {
        while (!this.stop) {
            ClientHandler ch = null;
            try {
                ch = new ClientHandler(this.server.accept(), this, this.clientHandlerId);
                this.clientHandlerId++;
            } catch (IOException e) {
                //this.stopProcess = true;
            }
            new Thread(ch).start();
            this.clientsHandler.add(ch);
        }
    }

    public Domain[] getDomainList() {
        return this.domains.toArray(new Domain[0]);
    }

    public PublicKey getPk() {
        return this.publicPrivateKey.getPublic();
    }

    public PrivateKey getPrivate() {
        return this.publicPrivateKey.getPrivate();
    }

    public IpAddressUDPServer getUDPCoordinateByMail(String mail) {
        return this.ipAddressesOfUDPServers.get(mail);
    }

    public Logger getLogger() {
        return logger;
    }
    
    public boolean isRespondingToRequest() {
        return this.isRespondingToRequest;
    }

    /**
     * @brief Set the ability of the server to respond to requests, mainly for testing purposes. 
     */
    public synchronized void setRespondingToRequest(boolean respondingToRequest) {
        this.isRespondingToRequest = respondingToRequest;
    }
}
