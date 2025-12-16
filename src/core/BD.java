package core;

public class BD {

    public final Repositorio<User> users;
    public final Repositorio<Leilao> leiloes;
    public final Repositorio<Lance> lances;

    private static BD instance = null;

    private BD() {
        this.users = new Repositorio<User>();
        this.leiloes = new Repositorio<Leilao>();
        this.lances = new Repositorio<Lance>();

        addTestRecords();
    }

    public static BD getInstance() {
        if (instance == null) {
            instance = new BD();
        }

        return instance;
    }

    private void addTestRecords() {
        users.add(new User("Joao"));
        users.add(new User("Maria"));
        users.add(new User("Horacio"));
        users.add(new User("Conceicao"));

        leiloes.add(new Leilao(0, "Microondas", 5000, "novo"));
        leiloes.add(new Leilao(0, "Torradeira", 4000, "bom"));

        leiloes.add(new Leilao(1, "Computador", 50000, "usado"));
        leiloes.add(new Leilao(1, "Telemovel", 10000, "bom"));

        leiloes.add(new Leilao(2, "Carro", 300000, "usado"));
        leiloes.add(new Leilao(2, "Meias", 99, "mau"));

        leiloes.add(new Leilao(3, "Batatas", 123, "bom"));
        leiloes.add(new Leilao(3, "Flores", 321, "novo"));

        Leilao microondas = leiloes.filteredSearch((item)->item.description.equals("Microondas")).getFirst();
        lances.add(new Lance(0, 0, 5100, microondas.getSha256Hash(), null));
    }

    public void createUser(User user) {
        this.users.add(user);
    }

    public boolean updateUser(int userID, User user) {
        User temp = this.users.get(userID);
        if (temp == null) {
            return false;
        }

        temp.update(user);
        return true;
    }

    public boolean deleteUser(int userID) {
        User temp = this.users.get(userID);
        if (temp == null) {
            return false;
        }

        for (Leilao l : this.leiloes.getAll()) {
            if (l.ownerID == userID) {
                this.leiloes.remove(l.getID());
                for (Lance bid : this.lances.getAll()) {
                    if (bid.leilaoID == l.getID()) {
                        this.lances.remove(bid.leilaoID);
                    }
                }
            }
        }

        return this.users.remove(userID);
    }

    public Leilao createLeilao(int ownerID, String estado, String description, int startPrice) {
        Leilao leilao;
        if (this.users.get(ownerID) == null) {
            return null;
        }
        leilao = new Leilao(ownerID, description, startPrice, estado);
        this.leiloes.add(leilao);

        return leilao;
    }

    public boolean updateLeilao(int leilaoID, Leilao leilao) {
        Leilao temp = this.leiloes.get(leilaoID);
        if (temp == null) {
            return false;
        }
        temp.update(leilao);
        return true;
    }

}
