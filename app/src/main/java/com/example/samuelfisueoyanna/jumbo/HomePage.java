package com.example.samuelfisueoyanna.jumbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class HomePage extends Activity{
    @Override
    protected void onCreate(Bundle FisJumbo) {
        super.onCreate(FisJumbo);
        setContentView(R.layout.home);

    }

    public void aButtonClicked(View view){
        Intent i = new Intent("android.intent.action.GAMESCREEN");
        startActivity(i);
    }

    public void bButtonClicked(View view){
        finish();
    }
}
