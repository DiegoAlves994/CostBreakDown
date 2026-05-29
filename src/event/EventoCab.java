package event;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.sql.ResultSet;

public class EventoCab implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        validaProduto(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        validaProduto(persistenceEvent);
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



    private void validaProduto(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        int nunico = cabVO.asInt("NUNICO");

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        ResultSet rs = null;

        try {

            String sqlValidaProduto =
                    "SELECT " +
                            "    CASE " +
                            "        WHEN EXISTS ( " +
                            "            SELECT 1 " +
                            "            FROM TPRLPA LPA " +
                            "            WHERE LPA.IDPROC = CAB.IDPROC " +
                            "              AND LPA.CODPRODPA = CAB.CODPROD " +
                            "        ) THEN 1 " +
                            "        ELSE 0 " +
                            "    END AS VALIDA, " +
                            "    CAB.CODPROD, " +
                            "    PRO.DESCRPROD, " +
                            "    PRC.IDPROC, " +
                            "    PRC.DESCRABREV, " +
                            "    PRC.VERSAO " +
                            "FROM AD_ZBVCAB CAB " +
                            "INNER JOIN TGFPRO PRO " +
                            "    ON PRO.CODPROD = CAB.CODPROD " +
                            "INNER JOIN TPRPRC PRC " +
                            "    ON PRC.IDPROC = CAB.IDPROC " +
                            "WHERE CAB.NUNICO = :NUNICO";

            sql.setNamedParameter("NUNICO", nunico);

            rs = sql.executeQuery(sqlValidaProduto);

            if (rs.next()) {

                int valida = rs.getInt("VALIDA");

                if (valida == 1) {
                    return;
                }

                int codProd = rs.getInt("CODPROD");
                String descrProd = rs.getString("DESCRPROD");
                int idProc = rs.getInt("IDPROC");
                String descrProcesso = rs.getString("DESCRABREV");
                int versao = rs.getInt("VERSAO");

                throw new Exception(
                        "Não é possível continuar." +
                                "O produto selecionado no orçamento: " +
                                codProd + " - " + descrProd +
                                " não existe no processo produtivo: " +
                                idProc + " - " + descrProcesso +
                                " (versão " + versao + ")." +
                                "Não foi possível determinar sua composição."
                );
            }

        } finally {

            if (rs != null) {
                rs.close();
            }
        }
    }



}