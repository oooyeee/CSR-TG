package core;

import java.util.HashMap;
import java.util.Map;

import interfaces.IApp;

public class AppFactory {

    private Map<String, IApp> serverApps;

    public AppFactory() {
        this.serverApps = new HashMap<>();
        this.serverApps.put("LEILOES", new AppLeiloes("Servidor de Leiloes"));
        this.serverApps.put("AUTHORIDADE", new AppCA("Servidor de Autoridade"));
    }

    public IApp getServer(String serverName){
        return this.serverApps.get(serverName);
    }
}
