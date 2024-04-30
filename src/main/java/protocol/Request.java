package protocol;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Request {

    public ProtocolCommand command;
    public Map<String, Object> param;

    public Request (ProtocolCommand command, Object... params) {
        this.command = command;
        this.param = new HashMap<>();
        switch (command) {
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER -> this.param.put("PublicKey", params[0]);
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK -> this.param.put("PublicKey", params[0]);
            case SIGN_UP -> {
                this.param.put("Mail", params[0]);
                this.param.put("Name", params[1]);
                this.param.put("Pwd", params[2]);
            }
            case SIGN_UP_OK -> {
                this.param.put("Mail", params[0]);
                this.param.put("Name", params[1]);
            }
            case SIGN_IN -> {
                this.param.put("Mail", params[0]);
                this.param.put("Pwd", params[1]);
                this.param.put("SendDomainList", params[2]);
            }
            case SIGN_IN_OK -> this.param.put("Name", params[0]);
            case SIGN_IN_KO, SIGN_UP_KO, SIGN_OUT_KO, DOMAINS_LIST_KO, SALES_FROM_DOMAIN_KO, REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO, CREATE_SALE_KO, UPDATE_SALE_KO, DELETE_SALE_KO, UDP_SERVER_KO, REQUEST_UDP_COORDINATES_KO -> {
                this.param.put("Error", params[0]);
            }
            case CREATE_SALE -> {
                this.param.put("Domain", params[0]);
                this.param.put("Title", params[1]);
                this.param.put("Descriptif", params[2]);
                this.param.put("Price", params[3]);
            }
            case UDP_SERVER -> {
                this.param.put("Address", params[0]);
                this.param.put("Port", params[1]);
            }
            case CREATE_SALE_OK -> this.param.put("Title", params[0]);
            case UPDATE_SALE -> {
                this.param.put("Title", params[0]);
                this.param.put("Descriptif", params[1]);
                this.param.put("Price", params[2]);
                this.param.put("Id", params[3]);
            }
            case DOMAINS_LIST_OK -> this.param.put("Domains", params[0]);
            case REQUEST_UDP_COORDINATES -> this.param.put("Mail", params[0]);
            case REQUEST_UDP_COORDINATES_OK -> {
                this.param.put("Mail", params[0]);
                this.param.put("Coord", params[1]);
            }
            case SALES_FROM_DOMAIN -> this.param.put("Domain", params[0]);
            case SALES_FROM_DOMAIN_OK -> this.param.put("AnnoncesFromDomain", params[0]);
            case DELETE_SALE -> this.param.put("Id", params[0]);
            case DOMAINS_LIST, UPDATE_SALE_OK, DELETE_SALE_OK, SIGN_OUT_OK, SIGN_OUT, UDP_SERVER_OK -> {}
            default -> throw new UnsupportedOperationException("Unimplemented case");
        }
    }

    public ProtocolCommand getCommand() {
        return command;
    }

    public Map<String, Object> getParams() {
        return this.param;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
