package com.noisyapple;

import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

    private Stack<String> symbolStack;
    private Grammar grammar;
    private LexicalAnalyzer lexicalAnalyzer;
    private TokenTable tokenTable;

    public Parser(Grammar grammar, LexicalAnalyzer lexicalAnalyzer) {
        symbolStack = new Stack<String>();
        tokenTable = new TokenTable();

        this.grammar = grammar;
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    // LLParser algorithm.
    public void parse() {

        String currentSymbol; // x
        Token currentToken; // a

        symbolStack.push(grammar.getStartSymbol()); // Start symbol is added to the stack.

        // currentSymbol be always attached to the value of the stack peek.
        currentSymbol = symbolStack.peek();
        currentToken = lexicalAnalyzer.getNextToken();

        // Parsing loop.
        while (!symbolStack.isEmpty()) {

            System.out.println("STACK => " + symbolStack);
            System.out.println("x => " + currentSymbol);
            System.out.println("a => Token" + currentToken);
            System.out.println("-----------------------------------------");

            if (grammar.isNonTerminal(currentSymbol)) {

                System.out.println("INTERSECTION => " + currentSymbol + " x "
                        + currentToken.getClassification());
                int parsingTableValue = grammar.getParsingTableValue(currentSymbol,
                        currentToken.getClassification());

                System.out.println(
                        "PRODUCTION FOUND => " + grammar.getProductionRules()[parsingTableValue]);

                if (parsingTableValue != ParsingTable.EMPTY) {

                    String[] rightSide = grammar.getRightSideArrayByIndex(parsingTableValue);

                    symbolStack.pop();

                    for (int i = rightSide.length - 1; i >= 0; i--) {
                        symbolStack.push(rightSide[i]);
                    }

                    currentSymbol = symbolStack.peek();

                } else {
                    throw new Error("Syntax error found!");
                }

            } else if (grammar.isTerminal(currentSymbol)) {

                if (currentSymbol.equals(currentToken.getClassification())) {

                    System.out.println(
                            "MATCH FOUND => " + currentSymbol + " == Token" + currentToken);

                    symbolStack.pop();
                    currentSymbol = symbolStack.peek();

                    tokenTable.addToken(currentToken);

                    currentToken = lexicalAnalyzer.getNextToken();
                } else {
                    throw new Error("Syntax error found!");
                }

            } else { // currentSymbol is neither terminal nor terminal.
                throw new Error("Syntax error found!");
            }

        }

        resolvePendingSymbols();

        System.out.println();
        System.out.println(symbolStack);
        System.out.println("DONE!");
        System.out.println();

        System.out.println("---------------[TOKEN TABLE]--------------");
        System.out.println(tokenTable);

    }

    // After the parsing process identifiers will have the PENDING type, this
    // procedure determines what type to assign to each identifier symbol.
    private void resolvePendingSymbols() {
        ArrayList<Symbol> pendingSymbols = lexicalAnalyzer.getSymbolTable().getPendingTypeSymbols();

        pendingSymbols.forEach(symbol -> {
            ArrayList<Integer> occurrenceLineNumbers = symbol.getLines();

            occurrenceLineNumbers.forEach(lineNumber -> {
                String lineContent = Utils.getLineContent(lexicalAnalyzer.getFile(), lineNumber);

                LexicalAnalyzer lineAnalyzer = new LexicalAnalyzer(lineContent);

                Token currentToken = lineAnalyzer.getNextToken();

                ArrayList<Token> lineTokens = new ArrayList<Token>();

                do {
                    lineTokens.add(currentToken);

                    currentToken = lineAnalyzer.getNextToken();
                } while (currentToken != null);

                Token firstTokenInLine = lineTokens.get(0);

                boolean isFirstTokenDataTypeDeclaration = false;
                boolean isFirstTokenThePendingSymbol = false;

                String firstTokenClassification = firstTokenInLine.getClassification();

                isFirstTokenDataTypeDeclaration = Token.DATA_TYPE_RESERVED_WORDS.get(firstTokenClassification) != null;
                isFirstTokenThePendingSymbol = symbol.getLexeme().equals(firstTokenInLine.getLexeme());

                if (isFirstTokenDataTypeDeclaration) {
                    if (symbol.getType() == "PENDING") {
                        symbol.setType(Token.DATA_TYPE_RESERVED_WORDS.get(firstTokenInLine.getClassification()));
                    } else {
                        // TODO: Should create an error, since identifier is being declared more than
                        // once.
                    }
                }

                if (isFirstTokenThePendingSymbol) {
                    // TODO: Check if next token is assignment operator and then evaluate the
                    // resulting type after performing the given operation.
                }
            });

        });
    }

}
