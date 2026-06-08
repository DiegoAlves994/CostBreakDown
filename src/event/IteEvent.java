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
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class IteEvent implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        processarInclusao(persistenceEvent);
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        processarAtualizacao(persistenceEvent);
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    private static final String TIPO_INCLUSAO = "I";
    private static final String TIPO_DIGITADO = "D";

    private void processarInclusao(PersistenceEvent evt) throws Exception {

        DynamicVO vo = (DynamicVO) evt.getVo();

        String tipo = vo.asString("TIPO");

        if (!TIPO_INCLUSAO.equals(tipo)) {
            atualizarTipo(
                    vo.asInt("NUNICO"),
                    vo.asInt("SEQ"),
                    TIPO_DIGITADO
            );
        }
    }

    private void processarAtualizacao(PersistenceEvent evt) throws Exception {

        DynamicVO vo = (DynamicVO) evt.getVo();

        String tipo = vo.asString("TIPO");

        if (!TIPO_DIGITADO.equals(tipo)) {
            atualizarTipo(
                    vo.asInt("NUNICO"),
                    vo.asInt("SEQ"),
                    TIPO_DIGITADO
            );
        }
    }

    private void atualizarTipo(int nunico, int seq, String tipo) throws Exception {

        JapeWrapper iteDAO = JapeFactory.dao("AD_ZBVITE");

        ((FluidUpdateVO) iteDAO.prepareToUpdateByPK(
                        new Object[]{nunico, seq})
                .set("TIPO", tipo))
                .update();
    }
}
