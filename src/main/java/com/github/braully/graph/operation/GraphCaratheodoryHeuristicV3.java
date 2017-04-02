package com.github.braully.graph.operation;

public class GraphCaratheodoryHeuristicV3
        extends GraphCaratheodoryHeuristicV2 {

    static final String description = "Nº Caratheodory (Heuristic v3)";

    @Override
    int calcRanking(int deltaHs, int neighborCount, int bfsP, int auxv) {
        int ranking = 0;
        ranking = (int) (deltaHs * 1 + neighborCount * 0.5 + bfsP * 0.3 + auxv * 0.2);
        return ranking;
    }
}
