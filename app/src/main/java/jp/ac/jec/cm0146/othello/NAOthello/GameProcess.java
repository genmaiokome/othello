package jp.ac.jec.cm0146.othello.NAOthello;

import java.util.Scanner;

public class GameProcess{

    Scanner sc = new Scanner(System.in);
    Board b;
    Players p1;
    Players p2;
    boolean isFirst;

    boolean toWin;

    public GameProcess(Players p1, Players p2, boolean toWin){
        this.p1 = p1;
        this.p2 = p2;
        if(p1 instanceof Player){
            isFirst = true;
        }else{
            isFirst = false;
        }
        b = new Board(p1, p2);
        b.toWin = toWin;
    }

    public boolean isFirst(){
        return isFirst;
    }

    public long inputLocation(Player p, char X, int Y){
        return b.coordinateToBit(X, Y);
    }

    public long inputLocation(Computer c){
        System.out.println("コンピューターの番です");
        return b.think(c, c.getDepth());
    }

    public boolean checkLocation(Player p, char X, int Y){
        return (b.makeLegalBoard(p) & b.coordinateToBit(X, Y)) == b.coordinateToBit(X, Y);
    }

    public void turn(Player p, char X, int Y){
        b.reverse(inputLocation(p, X, Y), p);
    }

    public Location turn(Computer c){
        long longLocation = inputLocation(c);
        Location l = null;
        b.reverse(longLocation, c);
        char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(b.coordinateToBit(chars[x],y + 1) == longLocation){
                    l = new Location(x,y);
                }
            }
        }
        return l;
    }

    public boolean isPass(Players p){
        return b.isPass(p);
    }

    public int[][] returnBoard(){
        return b.printBoard();
    }

    public int getCpuCount(){
        if(p1 instanceof Computer){
            return b.countLegalBits(p1.getBoard());
        }else{
            return b.countLegalBits(p2.getBoard());
        }
    }

    public int getPlayerCount(){
        if(p1 instanceof Player){
            return b.countLegalBits(p1.getBoard());
        }else{
            return b.countLegalBits(p2.getBoard());
        }
    }

}