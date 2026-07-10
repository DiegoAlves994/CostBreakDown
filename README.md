# CostBreakDown - Análise de Custos ZBV Metalúrgica

## 📋 Visão Geral

**CostBreakDown** é uma solução Java desenvolvida para a **ZBV Metalúrgica** que realiza análise e decomposição detalhada de custos de produção. O sistema integra-se a um ERP para calcular custos de orçamentos considerando múltiplas variáveis como composição de produtos, tarifas de CIP (Custo Indireto de Produção), despesas operacionais, impostos e margens comerciais.

### Objetivo Principal
Automatizar o cálculo de custos e preços de produtos através de orçamentos, considerando:
- Composição de matérias-primas
- Custos indiretos de produção (CIP)
- Atividades produtivas
- Margens comerciais e impostos
- Impactos de perdas (scrap) e fretes

---

## 🏗️ Arquitetura e Estrutura do Projeto

### Estrutura de Diretórios

```
src/
├── Helper/              # Classes utilitárias e lógica de negócio
│   └── ComposicaoProduto.java
├── action/              # Ações e rotinas disparáveis pelo usuário
│   ├── CarregaComp.java
│   ├── Finalizar.java
│   └── Reabrir.java
├── event/               # Eventos programáveis (listeners de BD)
│   ├── ValidaAtualizacao.java
│   ├── ValidaPendente.java
│   └── cabEvent.java
└── sqls/                # Repositório e scripts SQL
    ├── CipAtividade.sql
    ├── CipProcesso.sql
    ├── Composicao.sql
    ├── GetTotal.sql
    ├── ValidaComposicao.sql
    └── SQLRepository.java

Metadados/              # Metadados do projeto
Relatórios Formatados/  # Saídas e relatórios gerados
```

### Stack Tecnológico

- **Linguagem:** Java (100%)
- **Framework/Framework de Integração:** Sankhya ERP (JAPE - Java Access to Persistent Entities)
- **Banco de Dados:** Oracle Database
- **IDE:** IntelliJ IDEA
- **Bibliotecas Principais:**
  - `br.com.sankhya.extensions` - Extensões do ERP
  - `br.com.sankhya.jape` - Acesso a dados e persistência
  - `br.com.sankhya.modelcore` - Core do modelo de dados

---

## 📦 Módulos e Responsabilidades

### 1. **Helper Package** - Lógica de Negócio
Arquivo: `src/Helper/ComposicaoProduto.java`

#### Classe: `ComposicaoProduto`
Responsável pelos cálculos e operações principais de composição de custos.

**Métodos Principais:**

| Método | Responsabilidade |
|--------|------------------|
| `getMpComposicao()` | Carrega composição de matérias-primas (MP) para um orçamento |
| `getTarifaCipAtv()` | Calcula tarifas CIP por atividade produtiva |
| `getTarifaCipPrc()` | Calcula tarifas CIP por processo |
| `validaComposicao()` | Valida se a composição do produto existe no processo |

**Fluxo de Dados:**
```
Orçamento (NUNICO)
    ↓
[Busca dados via SQL]
    ↓
[Processa composição de MP e CIP]
    ↓
[Insere registros em tabelas auxiliares]
    ↓
Dados processados para cálculo de preço
```

**Estrutura de Dados Utilizadas:**
- `AD_ZBVITE` - Itens de matérias-primas
- `AD_ZBVCIP` - Custos indiretos de produção
- `AD_ZBVCAB` - Cabeçalho do orçamento

---

### 2. **Action Package** - Ações Disparáveis

#### a) `Finalizar.java`
**Objetivo:** Finalizar um orçamento e calcular preços finais

**Funcionalidades:**
- Valida se o orçamento está em status "pendente"
- Executa SQL para calcular preço com e sem impostos
- Atualiza campos no banco de dados
- Registra usuário que finalizou e data

**Fluxo:**
```
Usuário seleciona orçamento(s)
    ↓
[Valida status "pendente"]
    ↓
[Executa GetTotal.sql]
    ↓
[Calcula PRECOSEMIMP e PRECOIMP]
    ↓
[Atualiza AD_ZBVCAB com novos preços]
    ↓
Orçamento finalizado
```

**Campos Atualizados:**
- `PENDENTE` = "N"
- `CODUSUPENDENTE` = Código do usuário
- `DATAPENDENTE` = Timestamp atual
- `PRECOSEMIMP` = Preço sem impostos
- `PRECOIMP` = Preço com impostos

---

#### b) `CarregaComp.java`
**Objetivo:** Carrega a composição de um orçamento

Integra a classe `ComposicaoProduto` para processar dados.

---

#### c) `Reabrir.java`
**Objetivo:** Reabrir um orçamento finalizado para edição

Permite reverter orçamentos já finalizados.

---

### 3. **Event Package** - Listeners de Banco de Dados

#### `cabEvent.java`
**Tipo:** Event listener para tabela `AD_ZBVCAB` (cabeçalho de orçamentos)

**Implementa:** `EventoProgramavelJava`

**Métodos:**

| Evento | Ação |
|--------|------|
| `beforeInsert()` | - (vazio) |
| `beforeUpdate()` | Valida se orçamento já foi finalizado |
| `beforeDelete()` | Valida se orçamento pode ser deletado |
| `afterInsert()` | Marca orçamento como "pendente" e registra usuário/data |
| `afterUpdate()` | - (vazio) |
| `afterDelete()` | - (vazio) |

**Validações:**
```java
// Impede alteração de orçamentos finalizados (PENDENTE = "N")
if ("N".equals(newPendente) && "N".equals(oldPendente)) {
    throw new Exception("Não é possível alterar orçamento finalizado");
}

// Impede deleção de orçamentos finalizados
if ("N".equals(oldPendente)) {
    throw new Exception("Não é possível deletar orçamento finalizado");
}
```

---

#### `ValidaPendente.java`
**Objetivo:** Validações específicas de status pendente

---

#### `ValidaAtualizacao.java`
**Objetivo:** Validações ao atualizar orçamento

---

### 4. **SQL Package** - Queries e Repositório

#### `SQLRepository.java`
Classe de repositório para carregar arquivos SQL.

#### Scripts SQL Principais:

##### **GetTotal.sql** - Query Principal de Cálculo
Calcula o preço final do orçamento usando lógica CTEs (Common Table Expressions).

**Estrutura:**
```sql
WITH MKP AS (...)           -- Markup configuration
MP AS (...)                 -- Total de matérias-primas
CIP AS (...)                -- Total de CIP (atividades + processos)
OUTR AS (...)               -- Outros custos
FRE AS (...)                -- Fretes
AMO AS (...)                -- Amortizações/Investimentos
CALCULOS_BASE AS (...)      -- Base de cálculos
RESULTADOS AS (...)         -- Resultados intermediários
SELECT ...                  -- Resultado final
```

**Resultado:**
- `PRECOSEMIMPOSTO` - Preço base sem impostos
- `PRECOCOMIMPOSTO` - Preço final com todos os impostos

**Cálculo de Preços:**
1. **Custo Base:** MP + CIP + Outros + Scrap + Frete + Amortização
2. **Com Margens:** Custo Base + SGA% + Lucro%
3. **Sem Impostos:** Custo com margens
4. **Com Impostos:** Aplicar ICMS, PIS/COFINS, IRPJ/CSLL, Outros

---

##### **Composicao.sql**
Obtém a composição de matérias-primas para um orçamento

```sql
SELECT 
    SEQMP, CODPRODPA, CODPRODMP, CODPRC, 
    DESCRPROD, CODVOL, ID_ATIVIDADE,
    VERSAO_PROCESSO, QTDMISTURA, ATIVIDADE,
    CUSMEDICM, CUSSEMICM
FROM ...
WHERE NUNICO = :P_NUNICO
```

---

##### **CipAtividade.sql** e **CipProcesso.sql**
Obtêm tarifas de CIP por atividade e por processo respectivamente.

---

##### **ValidaComposicao.sql**
Valida se um produto possui composição válida para o processo produtivo.

---

## 🔄 Fluxo de Funcionamento End-to-End

### Cenário: Finalizar um Orçamento

```
┌─────────────────────────────────────────────────────────────┐
│ USUÁRIO: Clica em "Finalizar Orçamento"                     │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ Finalizar.doAction()                                         │
│ - Obtém orçamentos selecionados                             │
│ - Valida status "pendente"                                  │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ Executa GetTotal.sql                                        │
│ - Calcula PRECOSEMIMPOSTO                                  │
│ - Calcula PRECOCOMIMPOSTO                                  │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ JapeWrapper.prepareToUpdateByPK()                           │
│ - Atualiza AD_ZBVCAB com novos preços                      │
│ - Marca PENDENTE = "N"                                      │
│ - Registra usuário e data                                   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ cabEvent.beforeUpdate()                                      │
│ - Valida se orçamento pode ser alterado                     │
│ - Bloqueia se já foi finalizado anteriormente              │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ RESULTADO: Orçamento finalizado com sucesso                │
│ Mensagem: "Orçamento [NUNICO] finalizado com sucesso!"     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Modelo de Dados

### Tabelas Principais

| Tabela | Descrição |
|--------|-----------|
| `AD_ZBVCAB` | Cabeçalho do orçamento (NUNICO, PENDENTE, preços) |
| `AD_ZBVITE` | Itens - Matérias-primas (QTDBRUTO, VLRUNIT, etc) |
| `AD_ZBVCIP` | Custos indiretos de produção (VLRCUSTOPROCESSO, QTD) |
| `AD_ZBVMKP` | Configuração de markup (ICMS, SGA, LUCRO, etc) |
| `AD_ZBVOUT` | Outros custos (QUANTIDADE, VLRUNIT, VLRFRETE) |
| `AD_ZBVFRE` | Fretes (DISTANCIA, CUSTOKM, QTDPECAS) |
| `AD_ZBVINV` | Investimentos/Amortizações (QUANTIDADE, CUSTO, VOLUME) |

### Campos Críticos

- **NUNICO** - Número único do orçamento (identificador)
- **PENDENTE** - Status: "S" (pendente), "N" (finalizado)
- **PRECOSEMIMP** - Preço sem impostos
- **PRECOIMP** - Preço com impostos
- **SCRAP** - Percentual de perda na produção
- **VOLUME** - Volume total do lote
- **CODUSUINC** - Código do usuário que criou o orçamento

---

## 🛠️ Como Desenvolver

### Pré-requisitos
- Java JDK 8+
- IntelliJ IDEA
- Sankhya ERP instalado e configurado
- Acesso ao banco Oracle Database
- Conhecimento de SQL e JAPE framework

### Configuração Inicial

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/DiegoAlves994/CostBreakDown.git
   cd CostBreakDown
   ```

2. **Abra em IntelliJ IDEA:**
   - File → Open → Selecione a pasta do projeto
   - IntelliJ detectará automaticamente `CostBreakDown.iml`

3. **Configure dependências Sankhya:**
   - Adicione as bibliotecas Sankhya ao classpath
   - Verifique em: File → Project Structure → Libraries

4. **Compilação:**
   ```bash
   javac -d out/production/CostBreakDown src/**/*.java
   ```

### Adicionando Nova Funcionalidade

#### Exemplo: Novo método em ComposicaoProduto

```java
public void getMpEspecial(ContextoAcao ctx, BigDecimal nunico) throws Exception {
    // 1. Obter JDBC
    JdbcWrapper JDBC = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
    NativeSql sql = new NativeSql(JDBC);
    
    // 2. Carregar SQL
    sql.loadSql(SQLRepository.class, "MeuScript.sql");
    sql.setNamedParameter("P_NUNICO", nunico);
    ResultSet rs = sql.executeQuery();
    
    // 3. Processar resultado
    try {
        while (rs.next()) {
            // Processar dados...
        }
    } finally {
        rs.close();
        NativeSql.releaseResources(sql);
        JDBC.closeSession();
    }
}
```

#### Exemplo: Novo SQL Script

1. Crie `src/sqls/MeuScript.sql`
2. Use named parameters (`:P_NUNICO`)
3. Reference em Java via `SQLRepository.class`

### Testes

Atualmente não há testes unitários configurados. Para testar:

1. **Teste Manual via ERP:**
   - Acesse o módulo de orçamentos
   - Selecione um orçamento
   - Execute a ação "Finalizar"
   - Verifique logs e mensagens de erro

2. **Verifique logs:**
   - Sankhya logs geralmente em: `$SANKHYA_HOME/logs/`

---

## 🐛 Tratamento de Erros Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| `Nenhum orçamento selecionado` | Nenhuma linha selecionada | Selecione pelo menos um orçamento |
| `Não está pendente` | Orçamento já foi finalizado | Use "Reabrir" primeiro |
| `Não foi possível calcular preço` | SQL retorna vazio | Verifique se dados estão completos |
| `Orçamento já foi marcado como não pendente` | Tenta-se alterar finalizado | Não é permitido, reabra primeiro |
| `Não existe no processo produtivo` | Composição inválida | Verifique dados de produto e processo |

---

## 📝 Convenções de Código

### Naming Conventions
- **Classes:** PascalCase (`ComposicaoProduto`, `Finalizar`)
- **Métodos:** camelCase (`getMpComposicao`, `validaComposicao`)
- **Constantes:** UPPER_SNAKE_CASE
- **Variáveis locais:** camelCase (`nunico`, `precoSemImposto`)

### Estrutura de Métodos
```java
public void meuMetodo(ContextoAcao ctx, BigDecimal parametro) throws Exception {
    // 1. Validações iniciais
    
    // 2. Obter recursos (JDBC, SQL)
    
    // 3. Executar lógica principal
    
    // 4. Tratar exceções e liberar recursos (finally)
    
    // 5. Retornar resultado ou lançar exceção
}
```

### Comentários
- Documente o **propósito** do método
- Documente **parâmetros** de entrada
- Documente **exceções** que podem ser lançadas
- Use comentários inline para lógica complexa

---

## 🔐 Segurança

### Pontos de Atenção

1. **Injeção SQL:**
   - Sempre use `setNamedParameter()` em vez de concatenação
   - ✅ Correto: `sql.setNamedParameter("P_NUNICO", nunico)`
   - ❌ Errado: `"WHERE NUNICO = " + nunico`

2. **Validações de Negócio:**
   - Sempre validar status antes de permitir operações
   - Verificar permissões do usuário

3. **Recursos de Banco:**
   - Sempre fechar ResultSet em `finally`
   - Liberar NativeSql com `releaseResources()`
   - Fechar JDBC session

---

## 📊 Exemplos de Uso

### Calcular Composição de um Orçamento

```java
ComposicaoProduto composicao = new ComposicaoProduto();
BigDecimal numeroOrcamento = new BigDecimal(12345);

// Carrega matérias-primas
composicao.getMpComposicao(ctx, numeroOrcamento);

// Carrega tarifas CIP
composicao.getTarifaCipAtv(ctx, numeroOrcamento);
composicao.getTarifaCipPrc(ctx, numeroOrcamento);

// Valida composição
composicao.validaComposicao(ctx, numeroOrcamento);
```

### Finalizar Múltiplos Orçamentos

```java
Finalizar finalizador = new Finalizar();
Registro[] orcamentos = ctx.getLinhas();

// O método itera automaticamente sobre todos
finalizador.doAction(ctx);
```

---

## 📚 Referências

### Documentação Sankhya JAPE
- [JAPE API Documentation](https://sankhya.com.br/)
- [EventoProgramavelJava Interface](https://dev.sankhya.com.br/)

### SQL Utilizado
- Oracle SQL (versão 11g+)
- CTEs (WITH clauses)
- Window Functions (ROW_NUMBER OVER)

### Estrutura do Projeto
- Sem build automation (Maven/Gradle)
- Configuração direta em IntelliJ IDEA
- Dependências fornecidas pelo Sankhya ERP

---

## 👨‍💼 Informações de Contato e Manutenção

- **Autor:** Diego Alves
- **Repositório:** https://github.com/DiegoAlves994/CostBreakDown
- **Organização:** ZBV Metalúrgica
- **Data de Criação:** 2026-06-10

---

## 📋 Checklist para Novos Desenvolvedores

- [ ] Clonar repositório
- [ ] Configurar IntelliJ IDEA
- [ ] Adicionar bibliotecas Sankhya ao classpath
- [ ] Executar primeiro teste manual no ERP
- [ ] Revisar esta documentação
- [ ] Revisar arquivo `ComposicaoProduto.java`
- [ ] Revisar SQL em `GetTotal.sql`
- [ ] Entender fluxo de orçamento pendente → finalizado

---

**Última atualização:** Julho 2026
