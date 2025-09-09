package cida;

import cida.analysis.DepthFirstAdapter;
import cida.node.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

class TipoSemantico {
    private String tipo;
    private boolean inicializada;

    public TipoSemantico(String tipo, boolean inicializada) {
        this.tipo = tipo;
        this.inicializada = inicializada;
    }
    
    public String getTipo() {
        return tipo;
    }

    public boolean isInicializada() {
        return inicializada;
    }

    @Override
    public String toString() {
        return "Tipo: " + tipo + ", Inicializada: " + (inicializada ? "Sim" : "Não");
    }
} 


public class Semantico extends DepthFirstAdapter {

    Stack<HashMap<String, TipoSemantico>> pilhaDeTabelas = new Stack<HashMap<String, TipoSemantico>>();
    Stack<String> pilhaDeTipos = new Stack<String>();

    private void printTabela() {
        System.out.println("Tabela de Símbolos:");
        int numeroDaTabela = 0;
        for (HashMap<String, TipoSemantico> tabela : pilhaDeTabelas) {
            numeroDaTabela++;
            System.out.println("Tabela #" + numeroDaTabela + ":");
            for (Map.Entry<String, TipoSemantico> entrada : tabela.entrySet()) {
                System.out.println(entrada.getKey() + " -> " + entrada.getValue().toString());
            }
            System.out.println("-------------------"); // Linha de separação entre tabelas
        }
    }
    
    public static <K, V> void printHashMap(HashMap<K, V> mapa) {
        if (mapa.isEmpty()) {
            System.out.println("O HashMap está vazio.");
            return;
        }

        System.out.println("Conteúdo do HashMap:");
        for (Map.Entry<K, V> entrada : mapa.entrySet()) {
            System.out.println(entrada.getKey() + " -> " + entrada.getValue());
        }
    }

    public void printPilhaDeTipos() {
        System.out.println("Pilha de Tipos:");
        for (String tipo : pilhaDeTipos) {
            System.out.println(tipo);
        }
    }

    public HashMap<String, TipoSemantico> findIdentificador(String id) {
        // Busca o identificador declarado em todos os escopos
        for (HashMap<String, TipoSemantico> tabela : pilhaDeTabelas) {
            if (tabela.containsKey(id)) {
                System.out.println("\tIdentificador encontrado: " + id);
                return tabela; // Retorna tabela que contém o identificador
            }
        }
        System.out.println("\tIdentificador não encontrado: " + id);
        return null; // Retorna null se o identificador não foi encontrado
    }

    // Programa

    public void inAAProgAPrograma(AAProgAPrograma node)
    {
        System.out.println("Programa Iniciando programa.\n");
    }

    public void outAAProgAPrograma(AAProgAPrograma node)
    {
        System.out.println("Programa finalizado.\n");
    }

    // Bloco
    @Override
    public void inAABlocoABloco(AABlocoABloco node) {
        System.out.println("Iniciando Escopo...\n");
    
        HashMap<String, TipoSemantico> escopo = new HashMap<String, TipoSemantico>();

        // Cria um Escopo na pilha de tabelas (hashmaps)
        pilhaDeTabelas.push(escopo);
    }

    @Override
    public void outAABlocoABloco(AABlocoABloco node) {
        // Finaliza o Escopo
        pilhaDeTabelas.pop();
        System.out.println("\nFinalizando escopo...\n");
    }

/* --------------------------------------------------------------------------------------------
    Declarações
/* --------------------------------------------------------------------------------------------*/

    // alterable
    // entrada
    public void inAAAlterableADeclaracao(AAAlterableADeclaracao node)
    {
        
        String id = node.getId().toString();
        System.out.println("Declaracao: " + id);
        

        pilhaDeTabelas.peek().put(
            id,
            new TipoSemantico(
                node.getATipo().toString(),
                false
            )
        );
    }


    // unalterable ( sem atribuição )
    // entrada
    public void inAAUnalterableADeclaracao(AAUnalterableADeclaracao node)
    {
        // o unalterable está com algum erro de sintaxe ou léxico
        // por mais que o LexicoMain, consiga identificar o token, o parser não consegue
        // identificar o token do id, mesmo que dentro dos expecting, o id esteja presente.
        String id = node.getId().toString();

        pilhaDeTabelas.peek().put(
            id,
            new TipoSemantico(
                node.getATipo().toString(),
                false
            )
        );
    }

    // unalterable ( com atribuição )
    // entrada
    public void inAAUnalterableAtribADeclaracao(AAUnalterableAtribADeclaracao node)
    { 
        String id = node.getId().toString();

        System.out.println("Identificador: " + id);
        System.out.println("Tipo de Identificador: " + node.getATipo().toString());

        pilhaDeTabelas.peek().put(
            id,
            new TipoSemantico(
                node.getATipo().toString(),
                false
            )
        );
    }

    // unalterable ( com atribuição )
    // saída - Validação de tipo
    public void outAAUnalterableAtribADeclaracao(AAUnalterableAtribADeclaracao node)
    {  // atualiza o valor de inicializada para true
        String id = node.getId().toString();

        // Verifica se o identificador declarado coincide com a atribuição
        if (findIdentificador(id).get(id).getTipo().toString().equals(pilhaDeTipos.pop())){
            throw new RuntimeException(
                "O tipo declarado não coincide com o tipo atribuido.\n"
                + "Tipo declarado: " + pilhaDeTabelas.peek().get(id).getTipo() + "\n"
                +  "Tipo atribuido: " + pilhaDeTipos.peek());
        }

        pilhaDeTabelas.peek().put(
            id,
            new TipoSemantico(
                node.getATipo().toString(),
                true // Atualiza para true
            )
        );
    }

/*--------------------------------------------------------------------------------------------
    Comando
/* -----------------------------------------------------------------------------------------*/

    // atribuição ( << )
    public void outAAAtribuicaoAComando(AAAtribuicaoAComando node)
    {
        String id = node.getALocal().toString();

        // Verifica se o identificador declarado coincide com a atribuição
        if (findIdentificador(id).get(id).getTipo().toString().equals(pilhaDeTipos.peek())){
            throw new RuntimeException(
                "O tipo declarado não coincide com o tipo atribuido.\n"
                + "Tipo declarado: " + pilhaDeTabelas.peek().get(id).getTipo() + "\n"
                +  "Tipo atribuido: " + pilhaDeTipos.peek());
        }
        
        // Adiciona o tipo na tabela de símbolos
        findIdentificador(id).put(
            id,
            new TipoSemantico(
                pilhaDeTipos.pop(),
                true // já seja como true pq caso não seja aceito futuramente, o erro será lançado
            )
        );

    }

    // while ( as long as )
    public void outAAAsLongAsAComando(AAAsLongAsAComando node)
    {
        String expr_tipo = pilhaDeTipos.pop();
        

        if (expr_tipo != "answer") {
            throw new RuntimeException("Erro: Tipo incompatível. As Long As espera um tipo answer.");
        }

    }

    // if ( in case that i from k to l by j do X)
    public void outAAConsideringAComando(AAConsideringAComando node)
    {
        String id_tipo = pilhaDeTipos.pop();
        String from_tipo = pilhaDeTipos.pop();
        String end = pilhaDeTipos.pop();
        String step = pilhaDeTipos.pop();

        System.out.println("Tipo do Identificador: " + id_tipo);
        System.out.println("Tipo do From: " + from_tipo);
        System.out.println("Tipo do End: " + end);
        System.out.println("Tipo do Step: " + step);

        if (from_tipo != "number" || end != "number" || step != "number") {
            throw new RuntimeException("Erro: Tipos incompatíveis. O comando Considering espera um tipo number.");
        }
    }

    // if ( in case that i from k to l by j do X  otherwise Y)    
    public void outAACaseDoOtherAComando(AACaseDoOtherAComando node)
    {
        // em questao semantica ambos são iguais pq o tipo do comando não importa ( acredito que seja void )
        String id_tipo = pilhaDeTipos.pop();
        String from_tipo = pilhaDeTipos.pop();
        String end = pilhaDeTipos.pop();
        String step = pilhaDeTipos.pop();

        System.out.println("Tipo do Identificador: " + id_tipo);
        System.out.println("Tipo do From: " + from_tipo);
        System.out.println("Tipo do End: " + end);
        System.out.println("Tipo do Step: " + step);

        if (from_tipo != "number" || end != "number" || step != "number") {
            throw new RuntimeException("Erro: Tipos incompatíveis. O comando Considering espera um tipo number.");
        }
    }

/*--------------------------------------------------------------------------------------------
    Procedimentos
/* -----------------------------------------------------------------------------------------*/
    
    public void outAACaptureAComando(AACaptureAComando node)
    {
        // o documento não especifica, mas irei considera que capture aceita um tipo string
        // que seria equivalente ao texto do prompt na hora da captura do teclado
        // Ex.: Qual seu nome:__________

        if (pilhaDeTipos.pop() != "string" && node.getAParams().size() != 1) { 
            // estou limitando a 1, mas o documento nao especifica.
            throw new RuntimeException("Erro: Tipo incompatível. Capture espera um tipo string.");
        }
    }
    
    public void outAAShowAComando(AAShowAComando node)
    {
        // Já para o show, eu vou levar em consideração o print do C, que aceita N argumentos
        // sendo o primeiro necessariamente uma string e os demais opcionais (com base no formato definido na string)
        // Ex.: printf("O valor de x é: %d", x);

        if (pilhaDeTipos.pop() != "string") { 
            throw new RuntimeException("Erro: Tipo incompatível. Show espera um tipo string.");
        }
    }


//--------------------------------------------------------------------------------------------
// Tipo - Terminais
//--------------------------------------------------------------------------------------------

    // vector
    public void outAAVectorATipo(AAVectorATipo node)
    {   
        System.out.println(node.getADimensao().toString());

    }

    // answer
    public void outAAAnswerTATipo(AAAnswerTATipo node) {
        // Nao faz nada, é a classe base pra True or False
    }
    
    // answer - true
    public void outAATrueAAnswer(AATrueAAnswer node)
    {
        defaultOut(node);
    }
    
    // answer - false
    public void outAAFalseAAnswer(AAFalseAAnswer node)
    {
        defaultOut(node);
    }

    // symbol
    public void outAASymbolTATipo(AASymbolTATipo node)
    {
        pilhaDeTipos.push("symbol");
    }

    // number
    public void outAANumberTATipo(AANumberTATipo node)
    {
        pilhaDeTipos.push("number");
    }

//--------------------------------------------------------------------------------------------
// Expressão - Tipos Terminais
//--------------------------------------------------------------------------------------------

    // a_inteiro (number)
    public void outAAInteiroAExpr(AAInteiroAExpr node)
    {
        pilhaDeTipos.push("number");
    }

    // a_real (number)
    public void outAARealAExpr(AARealAExpr node)
    {
        pilhaDeTipos.push("number");
    }

    // a_symbol
    public void outAASymbolAExpr(AASymbolAExpr node)
    {
        pilhaDeTipos.push("symbol");
    }

    // a_string
    public void outAAStringAExpr(AAStringAExpr node)
    {
        // symbol = string?
        pilhaDeTipos.push("symbol");
    }

    //--------------------------------------------------------------------------------------------

// Expressão - Operadores

// Booleanos
    
    // xor ( xor )
    public void outAAXorAExpr(AAXorAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        // Verifica se os tipos são compatíveis
        if ( tipo_esq != "answer" || tipo_esq != tipo_dir) {
            System.out.println("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // or ( or )
    public void outAAOrAExpr(AAOrAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        // Verifica se os tipos são compatíveis
        if ( tipo_esq != "answer" || tipo_esq != tipo_dir) {
            System.out.println("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // and ( and )
    public void outAAAndAExpr(AAAndAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        // Verifica se os tipos são compatíveis
        if ( tipo_esq != "answer" || tipo_esq != tipo_dir) {
            System.out.println("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // Aritmeticos

    // add ( + )
    public void outAAAddAExpr(AAAddAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        if (tipo_esq == "number" && tipo_dir == "number") { 
            
            pilhaDeTipos.push("number");
            System.out.println("\tTipo da Soma: number");

        }
    }

    // sub ( - )
    public void outAASubAExpr(AASubAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();
    
        if (tipo_esq == "number" && tipo_dir == "number") { 
            
            pilhaDeTipos.push("number");
            System.out.println("\tTipo da Subtracao: number");
        }
    }

    // div ( / )
    public void outAADivAExpr(AADivAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();
    
        if (tipo_esq == "number" && tipo_dir == "number") { 
            
            pilhaDeTipos.push("number");
            System.out.println("\tTipo da Divisao: number");
        }
    }

    // mul ( * )
    public void outAAMulAExpr(AAMulAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();
    
        if (tipo_esq == "number" && tipo_dir == "number") { 
            
            pilhaDeTipos.push("number");
            System.out.println("\tTipo da Multiplicacao: number");
        }
    }

    // mod ( mod )
    public void outAAModAExpr(AAModAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();
    
        if (tipo_esq == "number" && tipo_dir == "number") { 
            
            pilhaDeTipos.push("number");
            System.out.println("\tTipo do Modulo: number");
        }
    }

// Relacionais
    /* Dica da Professora: Aceitam number retornam answer */

    // diferente a ( != )
    public void outAADiferenteAAExpr(AADiferenteAAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        System.out.println("Tipo esq: " + tipo_esq);
        System.out.println("Tipo dir: " + tipo_dir);

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis. Diferente espera um tipo number.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }


    // igual a ( == )
    public void outAAIgualAAExpr(AAIgualAAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        System.out.println("Tipo esq: " + tipo_esq);
        System.out.println("Tipo dir: " + tipo_dir);

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis. Igualdade espera um tipo number.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }
    // maior a ( > )
    public void outAAMaiorAExpr(AAMaiorAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        System.out.println("Tipo esq: " + tipo_esq);
        System.out.println("Tipo dir: " + tipo_dir);

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis. Maior que espera um tipo number.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // maior igual a ( >= )
    public void outAAMaiorIgualAExpr(AAMaiorIgualAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }
    
    // menor a ( < )
    public void outAAMenorAExpr(AAMenorAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // menor igual a ( <= )
    public void outAAMenorIgualAExpr(AAMenorIgualAExpr node)
    {
        String tipo_esq = pilhaDeTipos.pop();
        String tipo_dir = pilhaDeTipos.pop();

        if ( tipo_esq != "number" || tipo_esq != tipo_dir) {
            throw new RuntimeException("Erro: Tipos incompatíveis.");
        } else {
            pilhaDeTipos.push("answer");
        }
    }

    // Identificador em Expressão
    public void outAAIdAExpr(AAIdAExpr node)
    {
        // busca o valor do identificador na tabela de símbolos
        String id = node.getId().toString();
        HashMap<String, TipoSemantico> tabela = findIdentificador(id);

        if (tabela == null) {
            throw new RuntimeException("Erro: Identificador não declarado. " + id);
        } else {
            pilhaDeTipos.push(tabela.get(id).getTipo());
        }
    }

    // identificador para comando
    public void inAAIdALocal(AAIdALocal node)
    {
        
        String id = node.getId().toString();
        System.out.println("Identificador de Vetor: " + id);
        HashMap<String, TipoSemantico> tabela = findIdentificador(id);

        if (tabela == null) {
            throw new RuntimeException("Erro: Identificador não declarado. " + id);

        } else {
            pilhaDeTipos.push(tabela.get(id).getTipo());
        }
    }
}