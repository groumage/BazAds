package server;

public class Sale {
    private String owner; // the server store the mail of the owner within this variable, and when the sale is send to the client, it is replaced by the username
    private String title;
    private String content;
    private Domain domain;
    private int price;
    private int id;

    public Sale(String owner, Domain dom, String title, String content, int price, int id) {
        this.owner = owner;
        this.domain = dom;
        this.title = title;
        this.content = content;
        this.price = price;
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public String toString() {
        return "(" + this.owner + ", " + this.title + ", " + this.content + ", " + this.price + ", " + this.id + ")";
    }

    public String getOwner() {
        return this.owner;
    }

    public int getId() {
        return this.id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Domain getDomain() {
        return this.domain;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public int getPrice() {
        return this.price;
    }

    public void setDescriptif(String descriptif) {
        this.content = descriptif;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Sale))
            return false;
        Sale c = (Sale) o;
        return this.owner.equals(c.getOwner()) && this.title.equalsIgnoreCase(c.getTitle()) && this.content.equals(c.getContent()) && this.domain.equals(c.getDomain()) && this.price == c.getPrice() && this.id == c.getId();
    }
}
