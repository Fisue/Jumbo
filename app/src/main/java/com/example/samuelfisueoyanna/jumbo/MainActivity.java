package com.example.samuelfisueoyanna.jumbo;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
    import android.graphics.Color;
    import android.os.Environment;
    import android.support.v7.app.ActionBarActivity;
    import android.os.Bundle;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.TextView;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.OutputStreamWriter;
    import java.util.Random;
    import android.widget.Button;


    public class MainActivity extends ActionBarActivity {

        private GameLayout Game;

        private Button gameButton[];
        private TextView infoView;
        private TextView humanCount;
        private TextView tieCount;
        private TextView computerCount;

        private int humanCounter = 0;
        private int computerCounter = 0;
        private int tieCounter = 0;

        private boolean humanFirst = true;
        private boolean gameOver = false;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Game = new GameLayout();

            gameButton = new Button[Game.getBoardSize()];

            gameButton[0] = (Button) findViewById(R.id.one);
            gameButton[1] = (Button) findViewById(R.id.two);
            gameButton[2] = (Button) findViewById(R.id.three);
            gameButton[3] = (Button) findViewById(R.id.four);
            gameButton[4] = (Button) findViewById(R.id.five);
            gameButton[5] = (Button) findViewById(R.id.six);
            gameButton[6] = (Button) findViewById(R.id.seven);
            gameButton[7] = (Button) findViewById(R.id.eight);
            gameButton[8] = (Button) findViewById(R.id.nine);

            infoView = (TextView) findViewById(R.id.informationText);
            humanCount = (TextView) findViewById(R.id.human_score);
            tieCount = (TextView) findViewById(R.id.tie_score);
            computerCount = (TextView) findViewById(R.id.c_score);

            humanCount.setText(Integer.toString(humanCounter));
            tieCount.setText(Integer.toString(tieCounter));
            computerCount.setText(Integer.toString(computerCounter));

            Game = new GameLayout();

            startNewGame();

            Intent intent = new Intent(this, MyService.class);
            startService(intent);

        }

        private void startNewGame() {
            Game.clearBoard();

            for (int x = 0; x < gameButton.length; x++) {
                gameButton[x].setText(" ");
                gameButton[x].setEnabled(true);
                gameButton[x].setOnClickListener(new ButtonClickedListener(x));

            }

            if (humanFirst) {
                infoView.setText(R.string.human_first);
                humanFirst = false;
            } else {
                infoView.setText(R.string.computer_turn);
                int move = Game.getComputerMove();
                setMove(Game.COMPUTER_PLAYER, move);
                humanFirst = true;
            }
        }

        private class ButtonClickedListener implements View.OnClickListener {
            int location;

            public ButtonClickedListener(int location) {

                this.location = location;
            }

            public void onClick(View view) {
                if (!gameOver) {
                    if (gameButton[location].isEnabled()) {
                        setMove(Game.HUMAN_PLAYER, location);
                        int winner = Game.checkWinner();

                        if (winner == 0) {
                            infoView.setText(R.string.computer_turn);
                            int move = Game.getComputerMove();
                            setMove(Game.COMPUTER_PLAYER, move);
                            winner = Game.checkWinner();
                        }

                        if (winner == 0)
                            infoView.setText(R.string.human_turn);
                        else if (winner == 1) {
                            infoView.setText(R.string.tied_game);
                            tieCounter++;
                            tieCount.setText(Integer.toString(tieCounter));
                            gameOver = true;
                        } else if (winner == 2) {
                            infoView.setText(R.string.human_win);
                            humanCounter++;
                            humanCount.setText(Integer.toString(humanCounter));
                            gameOver = true;
                        } else {
                            infoView.setText(R.string.computer_win);
                            computerCounter++;
                            computerCount.setText(Integer.toString(computerCounter));
                            gameOver = true;
                        }
                    }
                }
            }

        }

        private void setMove(char player, int location) {
            Game.setMove(player, location);
            gameButton[location].setEnabled(false);
            gameButton[location].setText(String.valueOf(player));

            if (player == Game.HUMAN_PLAYER) {
                gameButton[location].setTextColor(Color.BLUE);
            } else
                gameButton[location].setTextColor(Color.RED);


        }

        public void restartClicked(View view) {
            Intent intt = new Intent("android.intent.action.GAMESCREEN");
            startActivity(intt);
        }


    }