package main.protocol;

public class UDPCoordinate {
    private String addr;
    private int port;

    public UDPCoordinate(String addr, int port) {
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
