package server;

public class Client {
    private String mail;
    private String name;
    private String pwd;
    private String salt;

    public Client(String mail, String name, String pwd, String salt) {
        this.mail = mail;
        this.name = name;
        this.pwd = pwd; // encrypted password
        this.salt = salt;
    }

    public String getName() {
        return this.name;
    }

    public String getMail() {
        return this.mail;
    }

    public String getSalt() {
        return this.salt;
    }

    public String getPwd() {
        return this.pwd;
    }
}
