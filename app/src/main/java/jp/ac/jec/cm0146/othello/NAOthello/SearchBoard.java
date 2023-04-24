package jp.ac.jec.cm0146.othello.NAOthello;

public class SearchBoard {
    private long playersBoard;
    private long opponentBoard;

    public void setOpponentBpard(long opponentBpard) {
        this.opponentBoard = opponentBpard;
    }

    public long getOpponentBoard() {
        return opponentBoard;
    }

    public void setPlayersBoard(long playersBoard) {
        this.playersBoard = playersBoard;
    }

    public long getPlayersBoard() {
        return playersBoard;
    }
}
