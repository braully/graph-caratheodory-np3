/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

/**
 *
 * @author braully
 */
public class TrabalhoProcessamento extends StrategyEstagnacao {

    Integer indiceAtual;
    Processamento last;

    public String getName() {
        return "Executar Trabalho Processamento";
    }

    public TrabalhoProcessamento(Integer indiceAtual) {
        this.indiceAtual = indiceAtual;
    }

    @Override
    public void generateGraph(Processamento processamento) {
        last = processamento;
        processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(indiceAtual);
        System.out.printf("Trabalho atual %d do indice %d \n", processamento.trabalhoAtual, indiceAtual);
        estagnarVertice(processamento);
        processamento.printGraphCaminhoPercorrido();
        System.out.printf("Concluido trabalho %d do indice %d \n", processamento.trabalhoAtual, indiceAtual);
    }
}
