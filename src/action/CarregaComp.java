package action;

import Helper.ComposicaoProduto;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class CarregaComp implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao arg0) throws Exception {

        JdbcWrapper JDBC = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(JDBC);


        Registro[] linhas = arg0.getLinhas();
        for (Registro linha : linhas) {

            BigDecimal nunico = ((BigDecimal) linha.getCampo("NUNICO"));



            ComposicaoProduto composicaoProduto = new ComposicaoProduto();

            composicaoProduto.validaComposicao(arg0, nunico);
            composicaoProduto.getMpComposicao(arg0, nunico);

            arg0.setMensagemRetorno("Carga de dados concluída com sucesso!");


        }


    }
}
