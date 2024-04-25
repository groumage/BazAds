package server;

import com.google.gson.*;

import logger.ErrorLogMessage;
import logger.InternalLogMessage;
import logger.TokenInternalLogMessage;
import protocol.ProtocolCommand;
import protocol.Request;
import protocol.RequestDeserializer;
import protocol.ServerProcessRequestsFromClient;

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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @@brief This class handles the communication between the server and the client.
 * 
 * @details A thread of ClientHandler is created for each client. This thread communicates with the server to process the requests from the client.
 */
public class ClientHandler implements Runnable, ServerProcessRequestsFromClient {
    private Socket socket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private Server server = null;
    private SecretKey sk = null;
    private boolean stopReceiveCommand = false;
    private int id;
    private String mail;

    public ClientHandler(Socket socket, Server server, int id) throws IOException {
        this.socket = socket;
        this.dis = new DataInputStream(this.socket.getInputStream());
        this.dos = new DataOutputStream(this.socket.getOutputStream());
        this.server = server;
        this.id = id;
        this.server.getLogger().info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER_CREATED, this.id).toString());
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    private String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    private void performKeyAgreement(Request req) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] otherPk = (byte[]) req.getParams().get("PublicKey");
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(otherPk);
        PublicKey otherPublicKey = kf.generatePublic(pkSpec);

        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(this.server.getPrivate());
        ka.doPhase(otherPublicKey, true);
        byte[] sharedSecret = ka.generateSecret();

        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(this.server.getPk().getEncoded()), ByteBuffer.wrap(otherPk));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        byte[] derivedKey = hash.digest();
        this.sk = this.getKeyFromSecret(encodeHexString(derivedKey), "key");
    }

    @Override
    public void run() {
        while (!this.stopReceiveCommand) {
            try {
                Request req = receiveRequest();
                if (req == null) {
                    this.stopReceiveCommand = true;
                    continue;
                }
                processInput(req);
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException |
                     ClassNotFoundException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                     IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processInput(Request inRequest) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        switch (inRequest.getCommand()) {
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER -> this.responseToRequestPublicKey(inRequest);
            case SIGN_UP -> this.responseToSignUp(inRequest);
            case SIGN_IN -> this.responseToSignIn(inRequest);
            case SIGN_OUT -> this.responseToSignOut(inRequest);
            case CREATE_SALE -> this.responseToCreateSale(inRequest);
            case UPDATE_SALE -> this.responseToUpdateSale(inRequest);
            case DELETE_SALE -> this.responseDeleteSale(inRequest);
            case SALES_FROM_DOMAIN -> this.responseAnnonceFromDomain(inRequest);
            case DOMAINS_LIST -> this.responseDomainsList();
            default -> throw new UnsupportedOperationException("Unimplemented case");
        }
    }

    private Request receiveRequest() throws IOException, ClassNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        int json_len = 0;
        try {
            json_len = this.dis.readInt();
        } catch (IOException ignored) {
            this.server.getLogger().info(new InternalLogMessage(TokenInternalLogMessage.SERVER_LOG_CLIENT_HANDLER, this.id).toString());
            return null;
        }
        byte[] json_bytes = this.dis.readNBytes(json_len);
        String json_str = new String(json_bytes);
        Request request;
        if (!json_str.contains("command")) {
            // encrypted content: extract iv and re-compute json string
            byte[] iv = Arrays.copyOfRange(json_bytes, 0, 12);
            json_str = new String(Arrays.copyOfRange(json_bytes, 12, json_bytes.length));
            String s = decrypt("AES/GCM/NoPadding", json_str, this.sk, iv);

            Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            request = gson.fromJson(s, Request.class);
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(Request.class, new RequestDeserializer()).create();
            request = gson.fromJson(json_str, Request.class);
        }
        return request;
    }
    
    public void sendRequest(Request request) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (request.getCommand() != ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO && request.getCommand() != ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK && request.getCommand() != ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER) {
            byte[] initializationVector = this.generateIv();
            String s = encrypt("AES/GCM/NoPadding", new Gson().toJson(request), this.sk, initializationVector);
            this.dos.writeInt(initializationVector.length + s.getBytes().length);
            this.dos.write(initializationVector);
            this.dos.writeBytes(s);
        } else {
            this.dos.writeInt(new Gson().toJson(request).getBytes().length);
            this.dos.writeBytes(new Gson().toJson(request));
        }
        this.dos.flush();
    }
    
    @Override
    public void responseToRequestPublicKey(Request inRequest) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Request outRequest;
        if (this.server.isRespondingToRequest()) {
            this.performKeyAgreement(inRequest);
            outRequest = new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK, this.server.getPk().getEncoded());
        } else
            outRequest = new Request(ProtocolCommand.REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseToSignUp(Request inRequest) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        String mailFromReq = (String) inRequest.getParams().get("Mail");
        String nameFromReq = (String) inRequest.getParams().get("Name");
        if (this.server.isRespondingToRequest())
            // TODO: first check the format of the mail and then check if it is already taken
            if (!this.server.isMailRegister(mailFromReq))
                if (this.server.isMailValid(mailFromReq))
                    if (this.server.isNameValid(nameFromReq)) {
                        this.server.addClient(mailFromReq, nameFromReq, (String) inRequest.getParams().get("Pwd"));
                        outRequest = new Request(ProtocolCommand.SIGN_UP_OK, mailFromReq, nameFromReq);
                    } else
                        outRequest = new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.NAME_NOT_VALID.getContent());
                else
                    outRequest = new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.MAIL_NOT_VALID.getContent());
            else
                outRequest = new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.MAIL_ALREADY_TAKEN.getContent());
        else
            outRequest = new Request(ProtocolCommand.SIGN_UP_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseToSignIn(Request inRequest) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        String mailFromReq = (String) inRequest.getParams().get("Mail");
        String pwdFromReq = (String) inRequest.getParams().get("Pwd");
        boolean sendDomainList = (boolean) inRequest.getParams().get("SendDomainList");
        //TODO: check if the mail is valid
        if (this.server.isRespondingToRequest())
            if (this.server.isMailRegister(mailFromReq))
                if (this.server.isPasswordValid(mailFromReq, pwdFromReq)) {
                    this.mail = mailFromReq;
                    if (sendDomainList)
                        this.responseDomainsList();
                    // the name is returned
                    outRequest = new Request(ProtocolCommand.SIGN_IN_OK, this.server.getClientFromMail((String) inRequest.getParams().get("Mail")).getName());
                    //request = new Request(ProtocolCommand.DOMAINS_LIST, (Object) this.server.getDomainList());
                } else
                    outRequest = new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent());
            else
                outRequest = new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.COMBINATION_MAIL_PWD_INVALID.getContent());
        else
            outRequest = new Request(ProtocolCommand.SIGN_IN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseToSignOut(Request inRequest) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        if (this.server.isRespondingToRequest())
            outRequest = new Request(ProtocolCommand.SIGN_OUT_OK);
        else
            outRequest = new Request(ProtocolCommand.SIGN_OUT_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseToCreateSale(Request inRequest) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        if (this.server.isRespondingToRequest()) {
            this.server.addSale(this.mail, (Domain) inRequest.getParams().get("Domain"), (String) inRequest.getParams().get("Title"), (String) inRequest.getParams().get("Descriptif"), (int) inRequest.getParams().get("Price"));
            outRequest = new Request(ProtocolCommand.CREATE_SALE_OK, inRequest.getParams().get("Title"));
        } else
            outRequest = new Request(ProtocolCommand.CREATE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseToUpdateSale(Request inRequest) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        if (this.server.isRespondingToRequest()) {
            if (this.server.isOwnerOfSale(this.mail, (int) inRequest.getParams().get("Id"))) {
                this.server.updateSale((String) inRequest.getParams().get("Title"), (String) inRequest.getParams().get("Descriptif"), (int) inRequest.getParams().get("Price"), (int) inRequest.getParams().get("Id"));
                outRequest = new Request(ProtocolCommand.UPDATE_SALE_OK);
            } else
            outRequest = new Request(ProtocolCommand.UPDATE_SALE_KO, ErrorLogMessage.NOT_OWNER.getContent());
        } else
        outRequest = new Request(ProtocolCommand.UPDATE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }
    
    @Override
    public void responseDeleteSale(Request inRequest) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        int idFromReq = (int) inRequest.getParams().get("Id");
        if (this.server.isRespondingToRequest()) {
            if (this.server.isOwnerOfSale(this.mail, idFromReq)) {
                this.server.deleteSale(idFromReq);
                outRequest = new Request(ProtocolCommand.DELETE_SALE_OK);
            } else
                outRequest = new Request(ProtocolCommand.DELETE_SALE_KO, ErrorLogMessage.NOT_OWNER.getContent());
        } else
            outRequest = new Request(ProtocolCommand.DELETE_SALE_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }
    
    @Override
    public void responseDomainsList() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request outRequest;
        if (this.server.isRespondingToRequest()) {
            outRequest = new Request(ProtocolCommand.DOMAINS_LIST_OK, new Object[] {this.server.getDomainList()});
        } else
            outRequest = new Request(ProtocolCommand.DOMAINS_LIST_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(outRequest);
    }

    @Override
    public void responseAnnonceFromDomain(Request inRequest) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request request;
        Domain domainFromReq = (Domain) inRequest.getParams().get("Domain");
        if (this.server.isRespondingToRequest()) {
            Sale[] annonces = this.server.getSalesOfDomain(domainFromReq);
            if (annonces.length != 0)
                request = new Request(ProtocolCommand.SALES_FROM_DOMAIN_OK, (Object) annonces);
            else
                request = new Request(ProtocolCommand.SALES_FROM_DOMAIN_KO, ErrorLogMessage.NO_SALES_IN_THAT_DOMAIN.getContent());
        } else {
            request = new Request(ProtocolCommand.SALES_FROM_DOMAIN_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        }
        this.sendRequest(request);
    }

    @Override
    public void responseUDPServerInsertion(Request req) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request request;
        String addrFromRequest = (String) req.getParams().get("Address");
        int portFromRequest = (int) req.getParams().get("Port");
        if (this.server.isRespondingToRequest()) {
            this.server.addUDPIpAddress(this.mail, addrFromRequest, portFromRequest);
            request = new Request(ProtocolCommand.UDP_SERVER_OK);
        } else
            request = new Request(ProtocolCommand.UDP_SERVER_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(request);
    }

    @Override
    public void responseUDPRequestCoordinate(Request req) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        Request request;
        String mail = (String) req.getParams().get("Mail");
        if (this.server.isRespondingToRequest())
            if (this.server.existUPCoordinate(mail))
                request = new Request(ProtocolCommand.REQUEST_UDP_COORDINATES_OK, mail, this.server.getUDPCoordinateByMail(mail));
            else
                request = new Request(ProtocolCommand.REQUEST_UDP_COORDINATES_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent()); //todo: change error content
        else
            request = new Request(ProtocolCommand.UDP_SERVER_KO, ErrorLogMessage.NOT_RESPONDING_TO_REQUEST.getContent());
        this.sendRequest(request);
    }


    public SecretKey getKeyFromSecret(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public byte[] generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public String encrypt(String algorithm, String input, SecretKey key, byte[] iv) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD("v1".getBytes(UTF_8));
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return new String(Base64.encodeBase64(cipherText));
    }

    public String decrypt(String algorithm, String cipherText, SecretKey key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        cipher.updateAAD("v1".getBytes(UTF_8));
        byte[] plainText = cipher.doFinal(java.util.Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
