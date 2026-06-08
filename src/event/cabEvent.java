package event;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class cabEvent implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {


    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        validaPendente(persistenceEvent);
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        validaPendente(persistenceEvent);

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        dadosInclusao(persistenceEvent);
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }



    private void dadosInclusao (PersistenceEvent ctx) throws Exception {

        JdbcWrapper jdbc = JapeFactory.getEntityFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        DynamicVO cabVo = (DynamicVO) ctx.getVo();

        BigDecimal nunico = cabVo.asBigDecimal("NUNICO");

        String getCodusu = "SELECT STP_GET_CODUSULOGADO CODUSU FROM DUAL WHERE 1=1";
        ResultSet rs = sql.executeQuery(getCodusu);
        BigDecimal newCodusu = null;

        if (rs.next()){

            newCodusu = rs.getBigDecimal("CODUSU");

            JapeWrapper iteDAO = JapeFactory.dao("AD_ZBVCAB");

            ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO) iteDAO.prepareToUpdateByPK(
                            new Object[]{nunico})
                    .set("PENDENTE", "S"))
                    .set("CODUSUINC",newCodusu))
                    .set("DATA", new java.sql.Timestamp(System.currentTimeMillis())))                     .update();

        }



    }

    private void validaPendente (PersistenceEvent ctx) throws Exception{

        DynamicVO oldVO = (DynamicVO) ctx.getOldVO();
        DynamicVO newVO = (DynamicVO) ctx.getVo();

        String newPendente = newVO.asString("PENDENTE");
        String oldPendente = oldVO.asString("PENDENTE");

        if("N".equals(newPendente) && "N".equals(oldPendente)){
            throw new Exception(
                    "Este orçamento já foi processado e não permite alterações. Realize a reabertura e tente novamente!"
            );


        }

    }

}
