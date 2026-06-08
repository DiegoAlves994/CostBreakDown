package Helper;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import sqls.SQLRepository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComposicaoProduto {

    public void getMpComposicao(ContextoAcao ctx, BigDecimal nunico) throws Exception {

        JdbcWrapper JDBC = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(JDBC);

        sql.loadSql(SQLRepository.class, "Composicao.sql");
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
                BigDecimal versao = rsComposicao.getBigDecimal("VERSAO_PROCESSO");
                BigDecimal qtdmistura = rsComposicao.getBigDecimal("QTDMISTURA");
                String atividade = rsComposicao.getString("ATIVIDADE");
                BigDecimal cusMedIcm = rsComposicao.getBigDecimal("CUSMEDICM");
                BigDecimal cusSemIcm = rsComposicao.getBigDecimal("CUSSEMICM");




                JapeWrapper insertMp = JapeFactory.dao("AD_ZBVITE");
                ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)insertMp.create()
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
                        .set("TIPO","I"))
                        .save();



            }
        }finally{


                rsComposicao.close();
                NativeSql.releaseResources(sql);
                JDBC.closeSession();
        }
    }

    public void validaComposicao( ContextoAcao ctx, BigDecimal nunico) throws Exception {

        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        sql.loadSql(SQLRepository.class, "ValidaComposicao.sql");
        sql.setNamedParameter("P_NUNICO", nunico);
        ResultSet rsValida = sql.executeQuery();

        try {
            while(rsValida.next()){

                int valida = rsValida.getInt("VALIDA");

                if (valida == 1) {
                    return;
                }

                int codProd = rsValida.getInt("CODPROD");
                String descrProd = rsValida.getString("DESCRPROD");
                int idProc = rsValida.getInt("IDPROC");
                String descrProcesso = rsValida.getString("DESCRABREV");
                int versao = rsValida.getInt("VERSAO");

                throw new Exception(
                        "Não é possível continuar." +
                                "O produto selecionado no orçamento: " +
                                codProd + " - " + descrProd +
                                ", não existe no processo produtivo: " +
                                idProc + " - " + descrProcesso +
                                " (versão " + versao + ")." +
                                "Não foi possível determinar sua composição."
                );


            }



        } finally {

            rsValida.close();
            NativeSql.releaseResources(sql);
            jdbc.closeSession();

        }
    }


}