package action;

import Helper.ComposicaoProduto;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import sqls.SQLRepository;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class Finalizar implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();

        if (linhas == null || linhas.length == 0) {
            throw new Exception("Nenhum orçamento selecionado.");
        }

        BigDecimal codusu = ctx.getUsuarioLogado();
        StringBuilder mensagens = new StringBuilder();

        for (Registro linha : linhas) {
            BigDecimal nunico = (BigDecimal) linha.getCampo("NUNICO");
            String pendente = (String) linha.getCampo("PENDENTE");

            if (!"S".equals(pendente)) {
                throw new Exception("Atenção: não é possível prosseguir. O orçamento número: " + nunico + " não está pendente");
            }

            BigDecimal precoSemImposto;
            BigDecimal precoComImposto;

            JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbc);
            try {
                sql.loadSql(SQLRepository.class, "GetTotal.sql");
                sql.setNamedParameter("P_NUNICO", nunico);

                try (ResultSet rsPreco = sql.executeQuery()) {

                    if (!rsPreco.next()) {
                        throw new Exception("Não foi possível calcular o preço do orçamento número: " + nunico + ". Nenhum resultado retornado.");
                    }

                    precoSemImposto = rsPreco.getBigDecimal("PRECOSEMIMPOSTO");
                    precoComImposto = rsPreco.getBigDecimal("PRECOCOMIMPOSTO");
                }

                if (precoSemImposto == null || precoComImposto == null) {
                    throw new Exception("Preço não calculado corretamente para o orçamento número: " + nunico);
                }

            } finally {
                NativeSql.releaseResources(sql);
                jdbc.closeSession();
            }

            JapeWrapper iteDAO = JapeFactory.dao("AD_ZBVCAB");
            iteDAO.prepareToUpdateByPK(new Object[]{nunico})
                    .set("PENDENTE", "N")
                    .set("CODUSUPENDENTE", codusu)
                    .set("DATAPENDENTE", new java.sql.Timestamp(System.currentTimeMillis()))
                    .set("PRECOSEMIMP", precoSemImposto)
                    .set("PRECOIMP", precoComImposto)
                    .update();



            mensagens.append("Orçamento ").append(nunico).append(" finalizado com sucesso!\n");
        }

        ctx.setMensagemRetorno(mensagens.toString().trim());
    }
}