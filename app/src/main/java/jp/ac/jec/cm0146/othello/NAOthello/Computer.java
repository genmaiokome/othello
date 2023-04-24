package jp.ac.jec.cm0146.othello.NAOthello;

public class Computer extends Players{
    private int cornerPoints;
    private int staticNumberOfLegalWays;
    private int staticDefinedNumber;

    public Computer(int depth){
        super();
        super.depth = depth;
    }
}
