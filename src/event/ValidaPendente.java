package event;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class ValidaPendente implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        BigDecimal nunico = vo.asBigDecimal("NUNICO");
        validarCabecalhoPendente(nunico);



    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        BigDecimal nunico = vo.asBigDecimal("NUNICO");
        validarCabecalhoPendente(nunico);
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

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


    private void validarCabecalhoPendente(BigDecimal nunico) throws Exception {

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        try {

            sql.appendSql(
                    " SELECT nvl(PENDENTE,'S') PENDENTE" +
                            "   FROM AD_ZBVCAB " +
                            "  WHERE NUNICO = :NUNICO "
            );

            sql.setNamedParameter("NUNICO", nunico);
            ResultSet rs = sql.executeQuery();

            try {

                if (rs.next()) {

                    String pendente = rs.getString("PENDENTE");

                    if ("N".equals(pendente)) {

                        throw new Exception(
                                "Este orçamento já foi processado e não permite alterações. Realize a reabertura e tente novamente!"
                        );

                    }
                }

            } finally {
                rs.close();
            }

        } finally {

            NativeSql.releaseResources(sql);
            jdbc.closeSession();

        }
    }
}
