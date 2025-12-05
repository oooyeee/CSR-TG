package Leiloes;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

    public class Leilao {
        private String id;
        private String descricao;
        private Date dataConclusao;
        private double valorMinimo;
        private String vendedor;
        private Date dataCriacao;
        private static final long serialVersionUID = 1L;
        private byte[] assinaturaVendedor;

        public ItemLeilao(String descricao, Date dataConclusao, double valorMinimo, String vendedor) {
            this.id = UUID.randomUUID().toString();
            this.descricao = descricao;
            this.dataConclusao = dataConclusao;
            this.valorMinimo = valorMinimo;
            this.vendedor = vendedor;
            this.dataCriacao = new Date();
        }

        public String getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }

        public Date getDataConclusao() {
            return dataConclusao;
        }

        public double getValorMinimo() {
            return valorMinimo;
        }

        public String getVendedor() {
            return vendedor;
        }

        public Date getDataCriacao() {
            return dataCriacao;
        }

         public byte[] getAssinaturaVendedor() {
        return assinaturaVendedor;
        }

        public void setAssinaturaVendedor(byte[] assinaturaVendedor) {
        this.assinaturaVendedor = assinaturaVendedor;
        }
        
        /**
         * Retorna os dados que devem ser assinados para garantir não-repúdio
         */
        public String getDadosParaAssinatura() {
            return id + descricao + dataConclusao.getTime() + valorMinimo + vendedor + dataCriacao.getTime();
        }

        public boolean isEncerrado() {
            return new Date().after(dataConclusao);
        }

        @Override
        public String toString() {
            return "ItemLeilao{" +
                    "id='" + id + '\'' +
                    ", descricao='" + descricao + '\'' +
                    ", dataConclusao=" + dataConclusao +
                    ", valorMinimo=" + valorMinimo +
                    ", vendedor='" + vendedor + '\'' +
                    '}';
        }
    }


