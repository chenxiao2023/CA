package com.example.CA;

public class ListItem {
    private int imageResource;
    private String address, publickey, chain;
    private String RegpublicKey;
    private int keyIndex;
    private boolean isInitialized;


    private String username;

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public ListItem(int imageResource, String address, String publickey, String chain, int keyIndex, String username) {
        this.imageResource = imageResource;
        this.address = address;
        this.publickey = publickey;
        this.chain = chain;
        this.keyIndex = keyIndex;
        this.RegpublicKey="";
        this.username=username;
        this.isInitialized = false;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
