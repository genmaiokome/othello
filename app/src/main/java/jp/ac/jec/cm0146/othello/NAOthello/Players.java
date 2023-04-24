package jp.ac.jec.cm0146.othello.NAOthello;

// super class of Player class and Computer class
public class
Players {
    //othello board whose information is held by each player
    private long board;
    private boolean first;
    int depth;

    Players(){
    }

    public void setBoard(long board) {
        this.board = board;
    }
    public long getBoard() {
        return board;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }

    public int getDepth() {
        return depth;
    }
}
