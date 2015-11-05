package com.example.samuelfisueoyanna.jumbo;

import java.util.Random;

/**
 * Created by SamuelFisueOyanna on 06/07/2015.
 */



public class GameLayout {
    private char gameBoard [];

    private final static int BOARD_SIZE = 9;
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = '0';
    public static final char FREE_SPACE = ' ';

    char player;
    int position;
    int move;
    int x;

    private Random randy;
    private int checkWinner;


    public static int getBoardSize(){

        return BOARD_SIZE;
    }

    public GameLayout(){
        gameBoard = new char[BOARD_SIZE];

        for (int x = 0; x < BOARD_SIZE; x++)
            gameBoard[x] = FREE_SPACE;

        randy = new Random();

    }

    public void clearBoard(){
        for (int x = 0; x < BOARD_SIZE; x++){
            gameBoard[x] = FREE_SPACE;
        }
    }

    public void setMove(char player, int position){

        gameBoard[position] = player;
    }



    public int checkWinner(){
        for (int x = 0; x <= 6; x++){
            if (gameBoard[x] == HUMAN_PLAYER && gameBoard[x+1] == HUMAN_PLAYER && gameBoard[x+2] == HUMAN_PLAYER){
                return 2;
            }
            if (gameBoard[x] == COMPUTER_PLAYER && gameBoard[x+1] == COMPUTER_PLAYER && gameBoard[x+2] == COMPUTER_PLAYER){
                return 3;
            }
        }

        for (int x = 0; x <= 2; x++){
            if (gameBoard[x] == HUMAN_PLAYER && gameBoard[x+3] == HUMAN_PLAYER && gameBoard[x+6] == HUMAN_PLAYER){
                return 2;
            }
            if (gameBoard[x] == COMPUTER_PLAYER && gameBoard[x+3] == COMPUTER_PLAYER && gameBoard[x+6] == COMPUTER_PLAYER) {
                return 3;
            }
        }

        if (gameBoard[0] == HUMAN_PLAYER && gameBoard[4] == HUMAN_PLAYER && gameBoard[8] == HUMAN_PLAYER ||
                gameBoard[2] == HUMAN_PLAYER && gameBoard[4] == HUMAN_PLAYER && gameBoard[6] == HUMAN_PLAYER){
            return 2;
        }

        if (gameBoard[0] == COMPUTER_PLAYER && gameBoard[4] == COMPUTER_PLAYER && gameBoard[8] == COMPUTER_PLAYER ||
                gameBoard[2] == COMPUTER_PLAYER && gameBoard[4] == COMPUTER_PLAYER && gameBoard[6] == COMPUTER_PLAYER) {
            return 3;
        }
        for (x = 0; x <= getBoardSize(); x++){
            if (gameBoard[x] != HUMAN_PLAYER && gameBoard[x] != COMPUTER_PLAYER){
                return 0;
            }
        } return 1;
    }

    public int getComputerMove(){
        for (int x = 0; x < getBoardSize(); x++) {
            if (gameBoard[x] != HUMAN_PLAYER && gameBoard[x] != COMPUTER_PLAYER) {
                char current = gameBoard[x];
                gameBoard[x] = COMPUTER_PLAYER;

                if (checkWinner == 3){
                    setMove(COMPUTER_PLAYER, x);
                    return x;
                }
                else
                    gameBoard[x] = current;
            }
        }

        for (int x = 0; x < getBoardSize(); x++){
            if (gameBoard[x] != HUMAN_PLAYER && gameBoard[x] != COMPUTER_PLAYER) {
                char current = gameBoard[x];
                gameBoard[x] = HUMAN_PLAYER;

                if (checkWinner == 2){
                    setMove(COMPUTER_PLAYER, x);
                    return x;
                }
                else
                    gameBoard[x] = current;
            }
        }
        do {
            move = randy.nextInt(getBoardSize());
        } while (gameBoard[move] == HUMAN_PLAYER || gameBoard[move] == COMPUTER_PLAYER);
        setMove(COMPUTER_PLAYER, move);

        return move;

    }
}
