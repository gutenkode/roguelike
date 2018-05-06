package de.gutenko.roguelike.loop;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.gutenko.motes.render.Shader;
import de.gutenko.motes.render.Texture;
import de.gutenko.motes.render.mesh.FontUtils;
import de.gutenko.roguelike.R;
import de.gutenko.roguelike.data.Const;
import de.gutenko.roguelike.data.Input;
import de.gutenko.roguelike.habittracker.data.player.GamePlayer;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String GAME_PLAYER_KEY = "GAME_PLAYER";
    private Surface surface;
    private GameLoop gameLoop;
    private GamePlayer gamePlayer;

    public static Intent launchIntent(GamePlayer gamePlayer) {
        Intent intent = new Intent();
        intent.putExtra(GAME_PLAYER_KEY, gamePlayer);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gamePlayer = (GamePlayer) getIntent().getSerializableExtra(GAME_PLAYER_KEY);

        loadResources();

        // Create a GLSurfaceView instance and set it as the ContentView for this Activity
        surface = new Surface(this);
        surface.setOnTouchListener(this);
        gameLoop = surface.getGameLoop();

        // remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // prevent rotation
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);

        this.setContentView(surface);
    }

    private void loadResources() {
        Resources res = getResources();

        try {
            InputStream is = res.openRawResource(R.raw.misterpixel_metric);
            byte[] metric = new byte[256];
            is.read(metric, 0, 256);
            is.close();
            FontUtils.loadMetric(metric, "font");
            FontUtils.useMetric("font");
            FontUtils.setCharPixelWidth(16);

            Shader.addSource("flatVert", loadShader(res, R.raw.flatvert));
            Shader.addSource("flatFrag", loadShader(res, R.raw.flatfrag));
            Shader.addSource("texVert", loadShader(res, R.raw.texvert));
            Shader.addSource("texFrag", loadShader(res, R.raw.texfrag));
            //Shader.addSource("crtFrag", loadShader(res, R.raw.crtfrag));
            Shader.addSource("quadVert", loadShader(res, R.raw.quadvert));
            Shader.addSource("quadFrag", loadShader(res, R.raw.quadfrag));
            //Shader.addSource("persistFrag", loadShader(res, R.raw.persistfrag));
            //Shader.addSource("blurFrag", loadShader(res, R.raw.blurfrag));
            Shader.addSource("spriteVert", loadShader(res, R.raw.spritevert));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Texture.loadFile(this.getApplicationContext(), R.drawable.tileset2, Const.TEX_TILESET);
        Texture.loadFile(this.getApplicationContext(), R.drawable.slime, Const.TEX_SLIME);
        Texture.loadFile(this.getApplicationContext(), R.drawable.rat, Const.TEX_RAT);
        Texture.loadFile(this.getApplicationContext(), R.drawable.player, Const.TEX_PLAYER);
        Texture.loadFile(this.getApplicationContext(), R.drawable.misterpixel, Const.TEX_FONT);
        Texture.loadFile(this.getApplicationContext(), R.drawable.healthbar, Const.TEX_HEALTHBAR);
    }

    private String loadShader(Resources res, int resHandle) throws IOException {
        InputStream inputStream = res.openRawResource(resHandle);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = buffreader.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        return text.toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
            case (MotionEvent.ACTION_POINTER_DOWN):
                Input.touchEventDown(event.getX(), event.getY(), event.getActionIndex());
                return true;
            case (MotionEvent.ACTION_MOVE):
                Input.touchEventMove(event.getX(), event.getY(), event.getActionIndex());
                return true;
            case (MotionEvent.ACTION_UP):
            case (MotionEvent.ACTION_POINTER_UP):
            case (MotionEvent.ACTION_CANCEL):
            case (MotionEvent.ACTION_OUTSIDE):
                Input.touchEventUp(event.getX(), event.getY(), event.getActionIndex());
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
