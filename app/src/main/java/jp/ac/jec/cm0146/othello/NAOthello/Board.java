package jp.ac.jec.cm0146.othello.NAOthello;

import java.util.*;

public class Board{
    Players p1;
    Players p2;
    boolean toWin;

    Board(Players p1, Players p2){
        this.p1 = p1;
        this.p2 = p2;

        //initialize the position of coins
        this.p1.setBoard(0x0000000810000000L);
        this.p1.setFirst(true);
        this.p2.setBoard(0x0000001008000000L);
        this.p2.setFirst(false);
    }

    //think computer
    public long think(Players p, int depth){
        Players o = returnOpponentPlayer(p);
        SearchBoard sb = new SearchBoard();
        sb.setPlayersBoard(p.getBoard());
        sb.setOpponentBpard(o.getBoard());
        SearchBoard tmp = new SearchBoard();
        tmp.setPlayersBoard(sb.getPlayersBoard());
        tmp.setOpponentBpard(sb.getOpponentBoard());
        long legalBoard = makeLegalBoard(sb);

        int currentP1Coins = countLegalBits(p1.getBoard());
        int currentP2Coins = countLegalBits(p2.getBoard());
        int restCoins = 64 - (currentP1Coins + currentP2Coins);
        if(restCoins >= 57){
            depth = 1;
        }

        int countLegalBoard = countLegalBits(legalBoard);
        long mask = 0x8000000000000000L;
        int score = -10000;
        int alpha = -10000;
        int beta = 10000;
        long bestWay = 0;
        long blankBoard = ~(p.getBoard() | o.getBoard());
        int blankCells = countLegalBits(blankBoard);
        int lastDepth = (int)(depth * 1.9);

        Map<Long, Integer> mMap = new HashMap<>();
        Queue queue = new PriorityQueue();

        while(mask != 0){
            if((mask & legalBoard) == 0){
                mask = mask >>> 1;
                continue;
            }
            reverse(mask, sb);
            if(blankCells <= lastDepth){
                score = -maxCount(Math.abs(depth - 4), sb, -beta, -alpha);
            }else{
                score = -negaAlpha(Math.abs(depth - 4), sb, -beta, -alpha);
            }
            if(alpha < score){
                alpha = score;
            }
            mMap.put(mask, -score);
            queue.add(-score);
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
            mask = mask >>> 1;
        }

        score = -10000;
        alpha = -10000;
        beta = 10000;
        bestWay = 0;
        Map<Long, Integer> mMap2 = new HashMap<>();
        Queue queue2 = new PriorityQueue();

        while(!queue.isEmpty()) {
            int next = (int)queue.poll();
            long location = getKey(mMap, next);
            mMap.remove(location);
            reverse(location, sb);
            if(blankCells <= lastDepth){
                score = -maxCount(Math.abs(depth - 3), sb, -beta, -alpha);
            }else{
                score = -negaAlpha(Math.abs(depth - 3), sb, -beta, -alpha);
            }
            if(alpha < score){
                alpha = score;
            }
            mMap2.put(location, -score);
            queue2.add(-score);
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
        }
        score = -10000;
        alpha = -10000;
        beta = 10000;
        bestWay = 0;

        Map<Long, Integer> mMap3 = new HashMap<>();
        Queue queue3 = new PriorityQueue();

        while(!queue2.isEmpty()) {
            int next = (int)queue2.poll();
            long location = getKey(mMap2, next);
            mMap2.remove(location);
            reverse(location, sb);
            if(blankCells <= lastDepth){
                score = -maxCount(Math.abs(depth - 2), sb, -beta, -alpha);
            }else{
                score = -negaAlpha(Math.abs(depth - 2), sb, -beta, -alpha);
            }
            if(alpha < score){
                alpha = score;
            }
            mMap3.put(location, -score);
            queue3.add(-score);
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
        }
        score = -10000;
        alpha = -10000;
        beta = 10000;
        bestWay = 0;


        //fire negaAlpha
        while(!queue3.isEmpty()) {
            int next = (int)queue3.poll();
            long location = getKey(mMap3, next);
            mMap3.remove(location);
            reverse(location, sb);
            if(blankCells <= lastDepth){
                score = -maxCount(blankCells - 1, sb, -beta, -alpha);
            }else{
                score = -negaAlpha(depth, sb, -beta, -alpha);
            }
            if(alpha < score){
                bestWay = location;
                alpha = score;
            }
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
        }
        return bestWay;
    }

    private <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry: map.entrySet())
        {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private int negaAlpha(int depth, SearchBoard sb, int alpha, int beta){

        //return maximum value to meet limited depth
        if(depth == 0){
            return evaluator(sb);
        }

        //return becouse of being finished or not being able to put
        if(isGameFinished()){
            return evaluator(sb);
        }

        SearchBoard tmp = new SearchBoard();
        tmp.setPlayersBoard(sb.getPlayersBoard());
        tmp.setOpponentBpard(sb.getOpponentBoard());
        long legalBoard = makeLegalBoard(sb);
        long mask = 0x8000000000000000L;
        int max = -10000;
        int score;
        long blankBoard = ~(sb.getPlayersBoard() | sb.getOpponentBoard());
        int blankCells = countLegalBits(blankBoard);

        int preAlpha = -10000;
        int preBeta = 10000;
        int preScore = -10000;

        Map<Long, Integer> mMap = new HashMap<>();
        Queue queue = new PriorityQueue();

        while(mask != 0){
            if((mask & legalBoard) == 0){
                mask = mask >>> 1;
                continue;
            }
            reverse(mask, sb);

            preScore = -negaAlpha(0, sb, -preBeta, -preAlpha);

            if(preAlpha < preScore){
                preAlpha = preScore;
            }
            mMap.put(mask, -preScore);
            queue.add(-preScore);
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
            mask = mask >>> 1;
        }



        //search all legal ways
        if(isPass(sb)){
            switchTurn(sb);
            return -negaAlpha(depth, sb, -beta, -alpha);
        }else {
            while(!queue.isEmpty()) {
                int next = (int)queue.poll();
                long location = getKey(mMap, next);
                mMap.remove(location);
                reverse(location, sb);
                score = -negaAlpha(depth - 1, sb, -beta, -alpha);
                if(score >= beta){
                    return score;
                }
                alpha = Math.max(alpha, score);
                max = Math.max(max, score);
                sb.setPlayersBoard(tmp.getPlayersBoard());
                sb.setOpponentBpard(tmp.getOpponentBoard());
                mask = mask >>> 1;
            }
        }

        return max;
    }



    private int maxCount(int depth, SearchBoard sb, int alpha, int beta){

        //return maximum value to meet limited depth
        if(depth == 0){
            return maxEvaluator(sb);
        }

        //return becouse of being finished or not being able to put
        if(isGameFinished()){
            return maxEvaluator(sb);
        }

        SearchBoard tmp = new SearchBoard();
        tmp.setPlayersBoard(sb.getPlayersBoard());
        tmp.setOpponentBpard(sb.getOpponentBoard());
        long legalBoard = makeLegalBoard(sb);
        long mask = 0x8000000000000000L;
        int max = -10000;
        int score;

        //search all legal ways
        if(isPass(sb)){
            switchTurn(sb);
            return -maxCount(depth ,sb, -beta, -alpha);
        }else while(mask != 0){
            if((mask & legalBoard) == 0){
                mask = mask >>> 1;
                continue;
            }
            reverse(mask, sb);
            score = -maxCount(depth - 1, sb, -beta, -alpha);
            if(score >= beta){
                return score;
            }
            alpha = Math.max(alpha, score);
            max = Math.max(max, score);
            sb.setPlayersBoard(tmp.getPlayersBoard());
            sb.setOpponentBpard(tmp.getOpponentBoard());
            mask = mask >>> 1;
        }

        return max;
    }

    private int evaluator(Players p){
        Players o = returnOpponentPlayer(p);
        int playerLegalCount = countLegalBits(makeLegalBoard(p));
        int opponentLegalCount = countLegalBits(makeLegalBoard(o));
        return playerLegalCount - opponentLegalCount;
    }

    private int evaluator(SearchBoard board){
        int cornerPoint = 4;
        int playerLegalCount = countLegalBits(makeLegalBoard(board));
        int opponentLegalCount = countLegalBits(makeOpponentLegalBoard(board));
        int currentP1Coins = countLegalBits(p1.getBoard());
        int currentP2Coins = countLegalBits(p2.getBoard());
        int restCoins = 64 - (currentP1Coins + currentP2Coins);
        long topLeft = 0x8000000000000000L;
        long topRight = 0x0100000000000000L;
        long bottomLeft = 0x0000000000000080L;
        long bottomRight = 0x0000000000000001L;
//        if((topLeft & board.getPlayersBoard()) == topLeft){
//            evaluation += cornerPoint;
//        }
//        if((topRight & board.getPlayersBoard()) == topRight){
//            evaluation += cornerPoint;
//        }
//        if((bottomLeft & board.getPlayersBoard()) == bottomLeft){
//            evaluation += cornerPoint;
//        }
//        if((bottomRight & board.getPlayersBoard()) == bottomRight){
//            evaluation += cornerPoint;
//        }
//        if((topLeft & board.getOpponentBoard()) == topLeft){
//            evaluation -= cornerPoint;
//        }
//        if((topRight & board.getOpponentBoard()) == topRight){
//            evaluation -= cornerPoint;
//        }
//        if((bottomLeft & board.getOpponentBoard()) == bottomLeft){
//            evaluation -= cornerPoint;
//        }
//        if((bottomLeft & board.getOpponentBoard()) == bottomLeft){
//            evaluation -= cornerPoint;
//        }

        int evaluation;
        int definedPlayer = countDefinedCoins(board);
        int definedOpponent = countOpponentDefinedCoins(board);
        if(toWin) {
            evaluation = (2 * (playerLegalCount - opponentLegalCount) + 3 * (definedPlayer - definedOpponent));
        }else{
            evaluation = (2 * (playerLegalCount - opponentLegalCount) - 6 * (definedPlayer - definedOpponent) - (countLegalBits(board.getPlayersBoard()) - countLegalBits(board.getOpponentBoard())));
        }

        int opponentCoins = countLegalBits(board.getOpponentBoard());
        if(opponentCoins == 0){
            if(toWin) {
                return 1000000;
            }else{
                return -1000000;
            }
        }

        if(restCoins >= 57){
            Random r = new Random();
            return r.nextInt(100);
        }

        return evaluation;
    }

    private int countDefinedCoins(SearchBoard board){
        long p = board.getPlayersBoard();
        long tmp1;
        long tmp2;
        long tmp3;
        long mask = 0;
        //wall's monitors
        int max;
        int min;

        tmp1 = 0x8000000000000000L & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp2 = 0x8000000000000000L & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> (i * 8)) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> (i * 8)) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0100000000000000L & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp2 = 0x0100000000000000L & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L >>> (i * 8)) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L << i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L >>> (i * 8)) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L << i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0000000000000080L & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp2 = 0x8000000000000080L & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000080L << i * 8) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000080L >>> i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000080L << i * 8) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000080L >>> i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0000000000000001L & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp2 = 0x8000000000000001L & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i * 8) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i * 8) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        return countLegalBits(mask);
    }

    private int countOpponentDefinedCoins(SearchBoard board){
        long p = board.getOpponentBoard();
        long tmp1;
        long tmp2;
        long tmp3;
        long mask = 0;
        //wall's monitors
        int max;
        int min;

        tmp1 = 0x8000000000000000L & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp2 = 0x8000000000000000L & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> (i * 8)) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> (i * 8)) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000000L >>> i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0100000000000000L & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp2 = 0x0100000000000000L & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp2 |= (tmp2 >>> 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L >>> (i * 8)) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L << i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L >>> (i * 8)) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0100000000000000L << i) >>> j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0000000000000080L & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp1 |= (tmp1 >>> 1) & p;
        tmp2 = 0x8000000000000080L & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000080L << i * 8) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000080L >>> i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000080L << i * 8) >>> j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x8000000000000080L >>> i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        tmp1 = 0x0000000000000001L & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp1 |= (tmp1 << 1) & p;
        tmp2 = 0x8000000000000001L & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp2 |= (tmp2 << 8) & p;
        tmp3 = 0;
        max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
        min = Math.min(countLegalBits(tmp1), countLegalBits(tmp2));

        if(countLegalBits(tmp1) > countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i * 8) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else if(countLegalBits(tmp1) < countLegalBits(tmp2)){
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }else{
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i * 8) << j) & p);
                    if(x == 0){
                        if(j - 1 >= 0){
                            max = j;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
            max = Math.max(countLegalBits(tmp1), countLegalBits(tmp2));
            for(int i = 0; i < min; i++){
                for(int j = 0; j < max; j++) {
                    long x = (((0x0000000000000001L << i) << j * 8) & p);
                    if(x == 0){
                        if(i - 1 >= 0){
                            max = i;
                        }
                        break;
                    }else{
                        tmp3 |= x;
                    }
                }
                if(max - 1 > 0) {
                    max -= 1;
                }
            }
        }
        mask |= tmp3;

        return countLegalBits(mask);
    }

    private int maxEvaluator(SearchBoard board){
        int playersCoins = countLegalBits(board.getPlayersBoard());
        int opponentCoins = countLegalBits(board.getOpponentBoard());
        if(opponentCoins == 0){
            if(toWin) {
                return 1000000;
            }else{
                return -1000000;
            }
        }

        if(toWin) {
            return (playersCoins - opponentCoins);
        }else{
            return  (opponentCoins - playersCoins);
        }
    }

    public int countLegalBits(long bits){
        bits = (bits & 0x5555555555555555L) + ((bits >>> 1) & 0x5555555555555555L);
        bits = (bits & 0x3333333333333333L) + ((bits >>> 2) & 0x3333333333333333L);
        bits = (bits & 0x0f0f0f0f0f0f0f0fL) + ((bits >>> 4) & 0x0f0f0f0f0f0f0f0fL);
        bits = (bits & 0x00ff00ff00ff00ffL) + ((bits >>> 8) & 0x00ff00ff00ff00ffL);
        bits = (bits & 0x0000ffff0000ffffL) + ((bits >>> 16) & 0x0000ffff0000ffffL);
        return (int)((bits & 0x00000000ffffffffL) + ((bits >>> 32) & 0x00000000ffffffffL));
    }

    //only to print the othello board
    public int[][] printBoard(){
        int[][] board = new int[8][8];

        //mask is located at A-1 at first
        System.out.println();
        System.out.println();
        System.out.print("  A B C D E F G H");
        long mask = 0x8000000000000000L;
        for(int i = 0; i < 64; i++){
            if((i % 8) == 0){
                System.out.println();
                System.out.print((i / 8) + 1 + " ");
            }
            if((p1.getBoard() | mask) == p1.getBoard()){
                System.out.print("○");
                board[i%8][i/8] = 1;
            }else if((p2.getBoard() | mask) == p2.getBoard()){
                System.out.print("●");
                board[i%8][i/8] = 2;
            }else{
                System.out.print("×");
                board[i%8][i/8] = 0;
            }
            System.out.print(" ");
            mask = mask >>> 1;
        }
        System.out.println();

        for(int y = 0; y < 8; y++){
            for(int x = 0; x < 8; x++){
                System.out.print(board[x][y]);
            }
            System.out.println();
        }
        return board;
    }

    //only to print the othello board
    public void printBoard(SearchBoard board){
        //mask is located at A-1 at first
        System.out.println();
        System.out.println();
        System.out.print("  A B C D E F G H");
        long mask = 0x8000000000000000L;
        long x = countDefinedCoins(board);
        for(int i = 0; i < 64; i++){
            if((i % 8) == 0){
                System.out.println();
                System.out.print((i / 8) + 1 + " ");
            }
            if((x | mask) == x){
                System.out.print("○");
            }else{
                System.out.print("×");
            }
            System.out.print(" ");
            mask = mask >>> 1;
        }
        System.out.println();
    }

    public long coordinateToBit(char X, int Y){
        //mask is located at A-1 at first
        long mask = 0x8000000000000000L;

        //horizontal bit shift
        switch (X){
            case 'A':
                break;
            case 'B':
                mask = mask >>> 1;
                break;
            case 'C':
                mask = mask >>> 2;
                break;
            case 'D':
                mask = mask >>> 3;
                break;
            case 'E':
                mask = mask >>> 4;
                break;
            case 'F':
                mask = mask >>> 5;
                break;
            case 'G':
                mask = mask >>> 6;
                break;
            case 'H':
                mask = mask >>> 7;
                break;
        }
        //vertical bit shift
        mask = mask >>> ((Y - 1) * 8);

        return mask;
    }

    //return whether player was passed or not
    public boolean isPass(Players p){
        Players o = returnOpponentPlayer(p);
        long playerLegalBoard = makeLegalBoard(p);
        long opponentLegalBoard = makeLegalBoard(o);
        return (playerLegalBoard == 0x0000000000000000L);
    }

    //return whether player was passed or not
    public boolean isPass(SearchBoard board){
        long playerLegalBoard = makeLegalBoard(board);
        long opponentLegalBoard = makeOpponentLegalBoard(board);
        return (playerLegalBoard == 0x0000000000000000L) && (opponentLegalBoard != 0x0000000000000000L);
    }

    //return whether game finishes or not
    public boolean isGameFinished(){
        Players p2 = returnOpponentPlayer(p1);
        long playerLegalBoard = makeLegalBoard(p1);
        long opponentLegalBoard = makeLegalBoard(p2);
        return (playerLegalBoard == 0x0000000000000000L) && (opponentLegalBoard == 0x0000000000000000L);
    }

    //return whether game finishes or not
    public boolean isGameFinished(SearchBoard board){
        long playerLegalBoard = makeLegalBoard(board);
        long opponentLegalBoard = makeOpponentLegalBoard(board);
        return (playerLegalBoard == 0x0000000000000000L) && (opponentLegalBoard == 0x0000000000000000L);
    }

    //reverse coins
    public void reverse(long put, SearchBoard board){
        if((makeLegalBoard(board) & put) == put) {
            long rev = 0;
            for (int k = 0; k < 8; k++) {
                long rev_ = 0L;
                long mask = transfer(put, k);
                while (mask != 0 && ((mask & board.getOpponentBoard()) != 0)) {
                    rev_ |= mask;
                    mask = transfer(mask, k);
                }
                if ((mask & board.getPlayersBoard()) != 0) {
                    rev |= rev_;
                }
            }
            board.setPlayersBoard(board.getPlayersBoard() ^ (put | rev));
            board.setOpponentBpard(board.getOpponentBoard() ^ rev);
            switchTurn(board);
        }
    }

    public void switchTurn(SearchBoard board){
        long tmp = board.getPlayersBoard();
        board.setPlayersBoard(board.getOpponentBoard());
        board.setOpponentBpard(tmp);
    }

    //reverse coins
    public void reverse(long put, Players p){
        if((makeLegalBoard(p) & put) == put) {
            Players o = returnOpponentPlayer(p);
            long rev = 0;
            for (int k = 0; k < 8; k++) {
                long rev_ = 0L;
                long mask = transfer(put, k);
                while (mask != 0 && ((mask & o.getBoard()) != 0)) {
                    rev_ |= mask;
                    mask = transfer(mask, k);
                }
                if ((mask & p.getBoard()) != 0) {
                    rev |= rev_;
                }
            }
            p.setBoard(p.getBoard() ^ (put | rev));
            o.setBoard(o.getBoard() ^ rev);
        }
    }

    //make all positions which player can put
    public long makeLegalBoard(Players p){
        Players o = returnOpponentPlayer(p);

        //wall's monitors
        long horizontalWatchBoard = o.getBoard() & 0x7e7e7e7e7e7e7e7eL;
        long verticalWatchBoard = o.getBoard() & 0x00FFFFFFFFFFFF00L;
        long allSideWatchBoard = o.getBoard() & 0x007e7e7e7e7e7e00L;
        long blankBoard = ~(p.getBoard() | o.getBoard());
        long tmp;
        long legalBoard = 0;

        //left
        tmp = horizontalWatchBoard & (p.getBoard() << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        legalBoard |= blankBoard & (tmp << 1);

        //right
        tmp = horizontalWatchBoard & (p.getBoard() >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        legalBoard |= blankBoard & (tmp >>> 1);

        //top
        tmp = verticalWatchBoard & (p.getBoard() << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        legalBoard |= blankBoard & (tmp << 8);

        //bottom
        tmp = verticalWatchBoard & (p.getBoard() >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        legalBoard |= blankBoard & (tmp >>> 8);

        //top left
        tmp = allSideWatchBoard & (p.getBoard() << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        legalBoard |= blankBoard & (tmp << 9);

        //top right
        tmp = allSideWatchBoard & (p.getBoard() << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        legalBoard |= blankBoard & (tmp << 7);

        //bottom left
        tmp = allSideWatchBoard & (p.getBoard() >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        legalBoard |= blankBoard & (tmp >>> 7);

        //top right
        tmp = allSideWatchBoard & (p.getBoard() >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        legalBoard |= blankBoard & (tmp >>> 9);

        return legalBoard;
    }

    //make all positions which player can put
    public long makeLegalBoard(SearchBoard board){
        long p = board.getPlayersBoard();
        long o = board.getOpponentBoard();

        //wall's monitors
        long horizontalWatchBoard = o & 0x7e7e7e7e7e7e7e7eL;
        long verticalWatchBoard = o & 0x00FFFFFFFFFFFF00L;
        long allSideWatchBoard = o & 0x007e7e7e7e7e7e00L;
        long blankBoard = ~(p | o);
        long tmp;
        long legalBoard = 0;

        //left
        tmp = horizontalWatchBoard & (p << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        legalBoard |= blankBoard & (tmp << 1);

        //right
        tmp = horizontalWatchBoard & (p >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        legalBoard |= blankBoard & (tmp >>> 1);

        //top
        tmp = verticalWatchBoard & (p << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        legalBoard |= blankBoard & (tmp << 8);

        //bottom
        tmp = verticalWatchBoard & (p >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        legalBoard |= blankBoard & (tmp >>> 8);

        //top left
        tmp = allSideWatchBoard & (p << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        legalBoard |= blankBoard & (tmp << 9);

        //top right
        tmp = allSideWatchBoard & (p << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        legalBoard |= blankBoard & (tmp << 7);

        //bottom left
        tmp = allSideWatchBoard & (p >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        legalBoard |= blankBoard & (tmp >>> 7);

        //top right
        tmp = allSideWatchBoard & (p >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        legalBoard |= blankBoard & (tmp >>> 9);

        return legalBoard;
    }

    //make all positions which player can put
    public long makeOpponentLegalBoard(SearchBoard board){
        long p = board.getOpponentBoard();
        long o = board.getPlayersBoard();

        //wall's monitors
        long horizontalWatchBoard = o & 0x7e7e7e7e7e7e7e7eL;
        long verticalWatchBoard = o & 0x00FFFFFFFFFFFF00L;
        long allSideWatchBoard = o & 0x007e7e7e7e7e7e00L;
        long blankBoard = ~(p | o);
        long tmp;
        long legalBoard = 0;

        //left
        tmp = horizontalWatchBoard & (p << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        tmp |= horizontalWatchBoard & (tmp << 1);
        legalBoard |= blankBoard & (tmp << 1);

        //right
        tmp = horizontalWatchBoard & (p >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        tmp |= horizontalWatchBoard & (tmp >>> 1);
        legalBoard |= blankBoard & (tmp >>> 1);

        //top
        tmp = verticalWatchBoard & (p << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        tmp |= verticalWatchBoard & (tmp << 8);
        legalBoard |= blankBoard & (tmp << 8);

        //bottom
        tmp = verticalWatchBoard & (p >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        tmp |= verticalWatchBoard & (tmp >>> 8);
        legalBoard |= blankBoard & (tmp >>> 8);

        //top left
        tmp = allSideWatchBoard & (p << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        tmp |= allSideWatchBoard & (tmp << 9);
        legalBoard |= blankBoard & (tmp << 9);

        //top right
        tmp = allSideWatchBoard & (p << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        tmp |= allSideWatchBoard & (tmp << 7);
        legalBoard |= blankBoard & (tmp << 7);

        //bottom left
        tmp = allSideWatchBoard & (p >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        tmp |= allSideWatchBoard & (tmp >>> 7);
        legalBoard |= blankBoard & (tmp >>> 7);

        //top right
        tmp = allSideWatchBoard & (p >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        tmp |= allSideWatchBoard & (tmp >>> 9);
        legalBoard |= blankBoard & (tmp >>> 9);

        return legalBoard;
    }
    //return object of opponent player
    private Players returnOpponentPlayer(Players p){
        Players o;
        if(p.isFirst()){
            o = p2;
        }else{
            o = p1;
        }
        return o;
    }

    //transfer to each direction
    private long transfer(long put, int k){
        switch (k){
            case 0:
                return (put << 8) & 0xffffffffffffff00L;
            case 1:
                return (put << 7) & 0x7f7f7f7f7f7f7f00L;
            case 2:
                return (put >>> 1) & 0x7f7f7f7f7f7f7f7fL;
            case 3:
                return (put >>> 9) & 0x007f7f7f7f7f7f7fL;
            case 4:
                return (put >>> 8) & 0x00ffffffffffffffL;
            case 5:
                return (put >>> 7) & 0x00fefefefefefefeL;
            case 6:
                return (put << 1) & 0xfefefefefefefefeL;
            case 7:
                return (put << 9) & 0xfefefefefefefe00L;
            default:
                return 0;
        }
    }
}
