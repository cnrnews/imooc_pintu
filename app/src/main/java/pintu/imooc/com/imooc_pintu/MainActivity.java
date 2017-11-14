package pintu.imooc.com.imooc_pintu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pintu.imooc.com.imooc_pintu.view.GamePintuLayout;

/**
 * @author lihl
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvLevel;
    private TextView tvTime;
    private GamePintuLayout gamePintuLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLevel = findViewById(R.id.tv_level);
        tvTime = findViewById(R.id.tv_time);
        gamePintuLayout = findViewById(R.id.gameLayout);
        gamePintuLayout.setTimeEnavled(true);
        gamePintuLayout.setGameLevelListener(new GamePintuLayout.GamePintuListener() {
            @Override
            public void levelUp(final int level) {
                new AlertDialog.Builder(MainActivity.this).setTitle("游戏拼图")
                        .setMessage("next level?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                gamePintuLayout.nextLevel();
                                tvLevel.setText(level + "");
                            }
                        }).show();
            }

            @Override
            public void timeChanged(int currentTime) {
                tvTime.setText(currentTime + "");
            }

            @Override
            public void gameOver() {
                if (!isFinishing()) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("拼图游戏")
                            .setMessage("game Over!")
                            .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    gamePintuLayout.restart();
                                }
                            }).setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePintuLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamePintuLayout.resume();
    }
}
