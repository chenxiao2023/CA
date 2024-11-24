package com.example.CA;

public class ListItem {
    private int imageResource;
    private String address, publickey, chain;
    private String RegpublicKey;
    private int keyIndex;

    public ListItem(int imageResource, String address, String publickey, String chain, int keyIndex) {
        this.imageResource = imageResource;
        this.address = address;
        this.publickey = publickey;
        this.chain = chain;
        this.keyIndex = keyIndex;
        this.RegpublicKey="";
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getaddress() {
        return address;
    }

    public String getpublickey() {
        return publickey;
    }

    public String getchain() {
        return chain;
    }

    public int getkeyIndex() {
        return keyIndex;
    }

    public String getRegpublicKey() {
        return RegpublicKey;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public void setRegpublicKey(String regpublicKey) {
        RegpublicKey = regpublicKey;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }
}
