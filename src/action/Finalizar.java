package action;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

public class Finalizar implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();
        BigDecimal codusu = ctx.getUsuarioLogado();
        BigDecimal nunico = null;

        for (Registro linha : linhas) {
            nunico = ((BigDecimal) linha.getCampo("NUNICO"));
            String pendente = ((String) linha.getCampo("PENDENTE"));


            if ("N".equals(pendente)) {
                throw new Exception("Atenção: não é possível prosseguir. O orçamento número: "+ nunico +" não está pendente");
            } else {


                JapeWrapper iteDAO = JapeFactory.dao("AD_ZBVCAB");
                iteDAO.prepareToUpdateByPK(new Object[]{nunico})
                        .set("PENDENTE", "N")
                        .set("CODUSUPENDENTE", codusu)
                        .set("DATAPENDENTE", new java.sql.Timestamp(System.currentTimeMillis()))
                        .update();

                ctx.setMensagemRetorno("Orçamento finalizado com sucesso!");
            }
        }
    }
}
