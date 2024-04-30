package client;

public class IpAddressUDPServer {
    private String addr;
    private int port;

    public IpAddressUDPServer(String addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public String getAddr() {
        return this.addr;
    }

    public int getPort() {
        return this.port;
    }
}
