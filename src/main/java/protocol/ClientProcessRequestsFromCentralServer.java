package protocol;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface ClientProcessRequestsFromCentralServer {
    void requestPublicKeyOk(Request r) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException;
    void requestPublicKeyKo(Request r);
    void signUpOk(Request r);
    void signUpKo(Request r);
    void signInOk(Request r) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
    void signInKo(Request r);
    void signOutOk();
    void signOutKo();
    void createSaleOk(Request r);
    void createSaleKo(Request r);
    void updateAnonceOk(Request r);
    void updateAnonceKo(Request r);
    void domainListOk(Request r);
    void domainListKo(Request req);
    void salesFromDomainOk(Request r);
    void salesFromDomainKo(Request r);
    void deleteSaleOk(Request r);
    void deleteSaleKo(Request r);
    void udpServerOk(Request r);
    void udpServerKo(Request r);
    void requestUDPServerOk(Request r);
    void requestUDPServerKO(Request r);
    void notHandledRequest();
}
