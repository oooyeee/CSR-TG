

import java.io.Serializable;
import java.util.Date;


public class Lance implements Serializable, Comparable<Lance> {
    private static final long serialVersionUID = 1L;

    private String itemId;
    private String usuario;
    private double valor;
    private Date timestamp;
    private byte[] assinaturaUsuario; // Assinatura digital para não-repúdio

    public Lance(String itemId, String usuario, double valor) {
        this.itemId = itemId;
        this.usuario = usuario;
        this.valor = valor;
        this.timestamp = new Date();
    }

    public String getItemId() {
        return itemId;
    }

    public String getUsuario() {
        return usuario;
    }

    public double getValor() {
        return valor;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public byte[] getAssinaturaUsuario() {
        return assinaturaUsuario;
    }

    public void setAssinaturaUsuario(byte[] assinaturaUsuario) {
        this.assinaturaUsuario = assinaturaUsuario;
    }


    public String getDadosParaAssinatura() {
        return itemId + usuario + valor + timestamp.getTime();
    }

    /**Compara lances: primeiro por valor (maior é melhor), depois por timestamp (mais cedo é melhor) */
    
    public int compareTo(Lance outro) {
        if (this.valor != outro.valor) {
            return Double.compare(outro.valor, this.valor); // Ordem decrescente de valor
        }
        return this.timestamp.compareTo(outro.timestamp); // Ordem crescente de tempo (mais cedo vence)
    }


    public String toString() {
        return "Lance{" +
                "itemId='" + itemId + '\'' +
                ", usuario='" + usuario + '\'' +
                ", valor=" + valor +
                ", timestamp=" + timestamp +
                '}';
    }
}