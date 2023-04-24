package jp.ac.jec.cm0146.othello.NAOthello;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import jp.ac.jec.cm0146.othello.R;

public class MainActivity extends AppCompatActivity {

    private ImageButton[][] buttons = new ImageButton[8][8];
    private int[][] board;
    private Players p1;
    private Players p2;
    private int level = 7;
    private boolean firstTurn = true;
    private boolean toWinMode = false;

    private Button btnRestart;
    private Spinner sp;
    private ToggleButton tbtn;
    private Switch switchBtn;
    private boolean started = false;
    private Location l;
    private TextView txtResultText;

    private GameProcess gp;
    private char[] alp = {'A','B','C','D','E','F','G','H'};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRestart = findViewById(R.id.btnRestart);
        sp = findViewById(R.id.cpuLevelSp);
        tbtn = findViewById(R.id.btnToggle);
        switchBtn = findViewById(R.id.btnSwitch);
        txtResultText = findViewById(R.id.resultText);
        tbtn.setChecked(true);
        sp.setSelection(6);

        String btnName;

        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                btnName = "cell" + alp[x] + (y + 1);
                int btnId = getResources().getIdentifier(btnName, "id", getPackageName());
                buttons[x][y] = (ImageButton)findViewById(btnId);
                buttons[x][y].setVisibility(View.INVISIBLE);
            }
        }

        createNewGP();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setBoardLayout();

                reloadBoard();
            }
        }, 1000);
    }

    public void onClickRestart(View view){
        createNewGP();

        btnRestart.setText("リセット&スタート");
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                buttons[x][y].setVisibility(View.VISIBLE);
            }
        }
        btnRestart.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(!firstTurn){
                    turnCpu();
                    board = gp.returnBoard();
                }
                btnRestart.setEnabled(true);
            }
        }, 1000);

    }

    public void onClickCell(View view){
        Player p;
        if(p1 instanceof Player){
            p = (Player) p1;
        }else{
            p = (Player) p2;
        }

        enabled(false);
        for(int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if(buttons[x][y] == view){

                    if(gp.checkLocation(p,alp[x],y + 1)){
                        gp.turn(p,alp[x], y + 1);
                        board = gp.returnBoard();
                        reloadBoard();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                turnCpu();
                            }
                        }, 1);
                    }

                }
            }
        }
        enabled(true);
    }

    public void turnCpu(){
        Computer c;
        Player p;
        if(p1 instanceof Computer){
            c = (Computer) p1;
            p = (Player) p2;
        }else{
            c = (Computer) p2;
            p = (Player) p1;
        }

        l = gp.turn(c);
        board = gp.returnBoard();
        reloadBoard();
        reloadCpu();
         if(gp.b.isGameFinished()){
            reloadBoard();
            int player = gp.getPlayerCount();
            int computer = gp.getCpuCount();

            String win;
            if(player > computer){
                win = "あなたの勝ちです";
            }else if(player < computer){
                win = "あなたの負けです";
            }else{
                win = "引き分けです";
            }
            txtResultText.setText("あなた：" + player + "枚\n" + "コンピューター：" + computer + "枚\n" + win);


        }else if(gp.isPass(p)){
             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     turnCpu();
                 }
             }, 1000);
        }
    }

    public void reloadCpu(){

        try {
            System.out.println(l.x + " " + l.y);
            if (firstTurn) {
                buttons[l.x][l.y].setImageResource(R.drawable.selectwhite);
            } else {
                buttons[l.x][l.y].setImageResource(R.drawable.selectblack);
            }
        }catch(Exception e){

        }
    }

    public void turnPlayer(){}

    public void reloadBoard(){
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                if(firstTurn) {
                    if (board[x][y] == 0) {
                        buttons[x][y].setImageResource(R.drawable.empty);
                    } else if (board[x][y] == 1) {
                        buttons[x][y].setImageResource(R.drawable.black);
                    } else {
                        buttons[x][y].setImageResource(R.drawable.white);
                    }
                }else{
                    if (board[x][y] == 0) {
                        buttons[x][y].setImageResource(R.drawable.empty);
                    } else if (board[x][y] == 1) {
                        buttons[x][y].setImageResource(R.drawable.white);
                    } else {
                        buttons[x][y].setImageResource(R.drawable.black);
                    }
                }
            }
        }
    }

    public void enabled(boolean b){
        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                buttons[x][y].setEnabled(b);
            }
        }
    }

    public void setBoardLayout() {
        LinearLayout layout = findViewById(R.id.othelloBoard);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = layout.getWidth();
        System.out.println("width" + layout.getWidth());
        layout.setLayoutParams(params);

    }

    public void onClickToggle(){
        if(tbtn.isChecked()){
            firstTurn = true;
        }else{
            firstTurn = false;
        }
    }


    public void createNewGP(){

        try {
            level = Integer.parseInt(sp.getSelectedItem().toString());
        }catch(Exception e){

        }
        if(tbtn.isChecked()){
            p1 = new Player();
            p2 = new Computer(level);
            firstTurn = true;
        }else {
            p1 = new Computer(level);
            p2 = new Player();
            firstTurn = false;
        }

        if(switchBtn.isChecked()){
            toWinMode = false;
        }else{
            toWinMode = true;
        }

        if(firstTurn) {
            gp = new GameProcess(p1, p2, toWinMode);
        }else{
            gp = new GameProcess(p2, p1, toWinMode);
        }

        board = gp.returnBoard();

        reloadBoard();

        txtResultText.setText("");
    }

}

