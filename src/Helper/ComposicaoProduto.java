package Helper;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComposicaoProduto {

    public void getMpComposicao(ContextoAcao ctx, BigDecimal nunico) throws Exception {

        JdbcWrapper JDBC = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(JDBC);

        sql.loadSql(getClass(), "Composicao.sql");
        sql.setNamedParameter("P_NUNICO", nunico);
        ResultSet rsComposicao = sql.executeQuery();

        try {
            while (rsComposicao.next()) {


                BigDecimal seqMp = rsComposicao.getBigDecimal("SEQMP");
                BigDecimal codProdPa = rsComposicao.getBigDecimal("CODPRODPA");
                BigDecimal codProdMp = rsComposicao.getBigDecimal("CODPRODMP");
                BigDecimal codPrc = rsComposicao.getBigDecimal("CODPRC");
                String descrAbrev = rsComposicao.getString("DESCRABREV");
                String codvol = rsComposicao.getString("CODVOL");
                BigDecimal idefx = rsComposicao.getBigDecimal("ID_ATIVIDADE");
                BigDecimal versao = rsComposicao.getBigDecimal("VERSAO");
                BigDecimal qtdmistura = rsComposicao.getBigDecimal("QTDMISTURA");
                String atividade = rsComposicao.getString("ATIVIDADE");
                BigDecimal cusMedIcm = rsComposicao.getBigDecimal("CUSMEDICM");
                BigDecimal cusSemIcm = rsComposicao.getBigDecimal("CUSSEMICM");



                JapeWrapper insertMp = JapeFactory.dao("AD_ZBVITE");
                ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)insertTerc.create()
                        .set("NUNICO", nunico))
                        .set("SEQ",  seqMp))
                        .set("CODPROD", codProdMp))
                        .set("CODVOL", codvol))
                        .set("DESCRATIVIDADE", descrAbrev))
                        .set("IDEFX", idefx))
                        .set("QTDBRUTO", qtdmistura))
                        .set("VLRUNIT",cusMedIcm))
                        .set("VLRUNITSEMICMS",cusSemIcm))
                        .set("QTDLIQUIDO",qtdmistura))
                        .save();



            }
        }finally{


                rsComposicao.close();
                NativeSql.releaseResources(sql);
                JDBC.closeSession();
        }
    }
}