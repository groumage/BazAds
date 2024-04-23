package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import client.IpAddressUDPServer;
import server.Domain;
import server.Sale;

import java.lang.reflect.Type;

public class RequestDeserializer implements JsonDeserializer<Request> {
    @Override
    public Request deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ProtocolCommand command = ProtocolCommand.valueOf(jsonObject.get("command").getAsString());
        JsonObject hashMap = (JsonObject) jsonObject.get("param");
        switch (command) {
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER -> {
                byte[] publicKey = new Gson().fromJson(hashMap.get("PublicKey"), byte[].class);
                byte[] iv = new Gson().fromJson(hashMap.get("IV"), byte[].class);
                return new Request(command, publicKey, iv);
            }
            case REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_OK -> {
                byte[] publicKey = new Gson().fromJson(hashMap.get("PublicKey"), byte[].class);
                return new Request(command, new Object[] {publicKey});
            }
            case SIGN_UP -> {
                String mail = new Gson().fromJson(hashMap.get("Mail"), String.class);
                String name = new Gson().fromJson(hashMap.get("Name"), String.class);
                String pwd = new Gson().fromJson(hashMap.get("Pwd"), String.class);
                return new Request(command, mail, name, pwd);
            }
            case SIGN_UP_OK -> {
                String mail = new Gson().fromJson(hashMap.get("Mail"), String.class);
                String name = new Gson().fromJson(hashMap.get("Name"), String.class);
                return new Request(command, mail, name);
            }
            case SIGN_IN -> {
                String mail = new Gson().fromJson(hashMap.get("Mail"), String.class);
                String pwd = new Gson().fromJson(hashMap.get("Pwd"), String.class);
                boolean sendDomainList = new Gson().fromJson(hashMap.get("SendDomainList"), boolean.class);
                return new Request(command, mail, pwd, sendDomainList);
            }
            case SIGN_IN_OK -> {
                String name = new Gson().fromJson(hashMap.get("Name"), String.class);
                return new Request(command, name);
            }
            case SIGN_OUT, SIGN_OUT_OK, UPDATE_SALE_OK, UDP_SERVER_OK -> {
                return new Request(command);
            }
            case SIGN_UP_KO, SIGN_IN_KO, SIGN_OUT_KO, SALES_FROM_DOMAIN_KO, REQUEST_PUBLIC_KEY_OF_CENTRAL_SERVER_KO, CREATE_SALE_KO, UPDATE_SALE_KO, DOMAINS_LIST_KO, DELETE_SALE_KO, REQUEST_UDP_COORDINATES_KO -> {
                String error = new Gson().fromJson(hashMap.get("Error"), String.class);
                return new Request(command, error);
            }
            case CREATE_SALE -> {
                Domain dom = new Gson().fromJson(hashMap.get("Domain"), Domain.class);
                String title = new Gson().fromJson(hashMap.get("Title"), String.class);
                String descriptif = new Gson().fromJson(hashMap.get("Descriptif"), String.class);
                int price = new Gson().fromJson(hashMap.get("Price"), int.class);
                return new Request(command, dom, title, descriptif, price);
            }
            case CREATE_SALE_OK -> {
                String title = new Gson().fromJson(hashMap.get("Title"), String.class);
                return new Request(command, title);
            }
            case UPDATE_SALE -> {
                String title = new Gson().fromJson(hashMap.get("Title"), String.class);
                String descriptif = new Gson().fromJson(hashMap.get("Descriptif"), String.class);
                int price = new Gson().fromJson(hashMap.get("Price"), int.class);
                int id = new Gson().fromJson(hashMap.get("Id"), int.class);
                return new Request(command, title, descriptif, price, id);
            }
            case DELETE_SALE_OK -> {
                String title = new Gson().fromJson(hashMap.get("Title"), String.class);
                return new Request(command, title);
            }
            case DOMAINS_LIST_OK -> {
                Domain[] domains = new Gson().fromJson(hashMap.get("Domains"), Domain[].class);
                return new Request(command, (Object) domains);
            }
            case SALES_FROM_DOMAIN -> {
                Domain dom = new Gson().fromJson(hashMap.get("Domain"), Domain.class);
                return new Request(command, dom);
            }
            case SALES_FROM_DOMAIN_OK -> {
                Sale[] annonces = new Gson().fromJson(hashMap.get("AnnoncesFromDomain"), Sale[].class);
                return new Request(command, (Object) annonces);
            }
            case DOMAINS_LIST -> {
                return new Request(ProtocolCommand.DOMAINS_LIST);
            }
            case DELETE_SALE -> {
                int id = new Gson().fromJson(hashMap.get("Id"), int.class);
                return new Request(ProtocolCommand.DELETE_SALE, id);
            }
            case UDP_SERVER -> {
                String address = new Gson().fromJson(hashMap.get("Address"), String.class);
                int port = new Gson().fromJson(hashMap.get("Port"), int.class);
                return new Request(ProtocolCommand.UDP_SERVER, address, port);
            }
            case REQUEST_UDP_COORDINATES -> {
                String mail = new Gson().fromJson(hashMap.get("Mail"), String.class);
                return new Request(ProtocolCommand.REQUEST_UDP_COORDINATES, mail);
            }
            case REQUEST_UDP_COORDINATES_OK -> {
                String mail = new Gson().fromJson(hashMap.get("Mail"), String.class);
                IpAddressUDPServer coord = new Gson().fromJson(hashMap.get("Coord"), IpAddressUDPServer.class);
                return new Request(ProtocolCommand.REQUEST_UDP_COORDINATES_OK, mail, coord);
            }
            default -> {
                return null;
            }
        }
    }
}
