package core;

import java.util.ArrayList;
import java.util.List;

import interfaces.IItem;

public class User implements IItem {
    private static int userCounter = 0;
    private final int userID;
    private String name;
    private String passwordHash;

    private List<Leilao> leiloes;
    private List<Lance> lances;

    public User(String name) {
        this.userID = userCounter++;
        this.name = name;
        this.leiloes = new ArrayList<>();
        this.lances = new ArrayList<>();
    }

    @Override
    public String toString(){
        String user = "";

        user+= "name: " + this.name + "\n";
        user+= "user id: " + this.userID + "\n";

        return user;
    }

    @Override
    public int getID() {
        return this.userID;
    }

    @Override
    public void update(IItem newItem) {
        this.name = ((User) newItem).name;
    }

}
