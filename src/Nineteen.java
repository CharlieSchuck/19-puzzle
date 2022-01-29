//Charlie Schuck, 2022 -- NINETEEN.java
//This program solves a "Nineteen" puzzle, using A* and a heuristic or IDS,
//given an entered "problem state."
//ADAPTED FROM DR. SIMON'S Eight.java -- Heuristic changes Inspired by https://web.mit.edu/6.034/wwwbob/EightPuzzle.pdf
//Due 2/11/22

import java.util.Random;
import java.util.Stack;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Scanner;

public class Nineteen {
    //Basically, I'm just going to pretend like we're playing on a 6x6 grid with 16 tiles that are off limits.
    final static int SIZE = 35;
    final static int LENGTH = 6;

    byte tiles[];
    byte blankPos;

    public Nineteen(byte[] x){
        tiles = Arrays.copyOf(x, x.length);
        for(byte i = 0; i <= SIZE; i++){ //this just finds the blank tile in each state. Code should still work, so I'm keeping it.
            if(tiles[i] == 0){
                blankPos = i;
                return;
            }
        }
    }
    //cool, another borrowed constructor. "arigatou gozaimasu"
    public Nineteen(byte tiles[], byte blankPos){
        this.tiles = Arrays.copyOf(tiles, tiles.length);
        this.blankPos = blankPos;
    }
    //Some mild modifications here... We don't want to print the unused spaces. but your formatting is quite nice,
    //so I think I'll keep it.
    public String toString(){
        String s = "";
        for(int i=0; i<LENGTH; i++) {
            for(int j=0; j<LENGTH; j++)
                if(tiles[i*LENGTH+j] != -1)
                    s += String.format(" %2d", tiles[i*LENGTH+j]);
                else
                    s += String.format(" %2s", " ");
            s += "\n";
        }
        return s;
    }
    //This should all still work, as the structure of these "crosses" are always the same... with -1's in the same places.
    public boolean equals(Object o){
        Nineteen r = (Nineteen) o;
        return blankPos == r.blankPos && Arrays.equals(tiles, r.tiles);
    }

    //hashcode is hashcode
    public int hashCode(){
        return Arrays.hashCode(tiles);
    }

    //and I will preserve your interface structure -- very handy.
    interface MoveAction{
        boolean valid();
        void move();
    }
    //so this is where I decided to make our large square into a cross... basically, any tiles that have a -1 on them are
    //off limits... and they will be seen as immovable... hopefully.

    private MoveAction[] moveActions = new MoveAction[]{
            new MoveAction() { // up
                public boolean valid() { return (blankPos > LENGTH-1) && (tiles[blankPos - LENGTH] != -1 ); }
                public void move() { tiles[blankPos] = tiles[blankPos-LENGTH]; blankPos -= LENGTH; tiles[blankPos] = 0;}
            },
            new MoveAction() { // down
                public boolean valid() { return (blankPos < SIZE-LENGTH+1) && (tiles[blankPos + LENGTH] != - 1); }
                public void move() { tiles[blankPos] = tiles[blankPos+LENGTH]; blankPos += LENGTH; tiles[blankPos] = 0;}
            },
            new MoveAction() { // left
                public boolean valid() { return (blankPos % LENGTH != 0) && (tiles[blankPos - 1] != - 1); }
                public void move() { tiles[blankPos] = tiles[blankPos-1]; blankPos -= 1; tiles[blankPos] = 0;}
            },
            new MoveAction() { // right
                public boolean valid() { return (blankPos % LENGTH != LENGTH-1) && (tiles[blankPos + 1] != -1); }
                public void move() { tiles[blankPos] = tiles[blankPos+1]; blankPos += 1; tiles[blankPos] = 0;}
            }
    };

    //-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

    //okay, the Node class. -- Again, yours is perfectly capable for what I need.
    static class Node implements Comparable<Node>, Denumerable {
        public Nineteen state;
        public Node parent;
        public int g, h;
        public boolean inFrontier;
        public int x;

        //constructor.
        Node(Nineteen state, Node parent, int g, int h) {
            this.state = state;
            this.parent = parent;
            this.g = g;
            this.h = h;
            inFrontier = true;
            x = 0;
        }

        //satisfying the implement.
        public int compareTo(Node a) {
            return g + h - a.g - a.h;
        }

        public int getNumber() {
            return x;
        }

        public void setNumber(int x) {
            this.x = x;
        }

        public String toString() {
            return state + ""; //should be satisfactory.
        }
    }

    //-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --

    private static int[] opp = {1,0,3,2}; //options? opportunities?

    public static void main(String[] args){
        //I need not randomly generate anything.
        byte[] x = new byte[SIZE+1];
        //let's construct our goal state... 0 in the first available space... 19 in the last.
        byte j = 0;
        for(byte i = 0; i < SIZE+1; i++){
            //we're going to do this the dumb way.
            if (i == 0 || i == 1 || i == 4 || i == 5 || i == 6 || i == 7 || i == 10 || i == 11 || i == 24 || i == 25
                    || i == 28 || i == 29 || i == 30 || i == 31 || i == 34 || i == 35) {
                x[i] = -1;
            }
            else {
                x[i] = j;
                j++;
            }
        }
        Nineteen goal = new Nineteen(x);

        //now, let's construct our problem state.
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your starting state, one number at a time, pressing ENTER after each entry.");
        for (byte i = 0; i < SIZE; i++) {
            if (i == 0 || i == 1 || i == 4 || i == 5 || i == 6 || i == 7 || i == 10 || i == 11 || i == 24 || i == 25
                    || i == 28 || i == 29 || i == 30 || i == 31 || i == 34 || i == 35) {
                x[i] = -1;
            } else {
                x[i] = sc.nextByte();
            }
        }
        Nineteen r = new Nineteen(x);
        System.out.println(r);
        //no need to shuffle this bad boy, I trust all the states you've provided are solvable.
        //I know this can get dicey with memory usage, but I'm going to use astar... Not to flex or anything,
        // but I have 64gb of ram.

        astar(r, goal);
        //ids(r, goal);
        //I hit a road block with astar... not enough memory... looks like I'll be adapting the ids too. ----
        //and IDS was taking forever... so I'm going to try to optimize memory use a bit, maybe swapping over to bytes.
    }

    //heuristic -- I will play around with this, and think of something in the coming days... Is there something better
    //than NY, given the extra edges? -- ALSO... since anything that has a -1 is already in place, we can just ignore
    //-1s in the goal and current position.

    //So, after completing the first 5 examples, I found this article: https://web.mit.edu/6.034/wwwbob/EightPuzzle.pdf
    //I'm going to try and implement a ""reversal penalty""
    public static int h(Nineteen r, Nineteen goal){
        byte[] rev = new byte[SIZE+1];
        int total = 0;
        byte j = SIZE;
        for(byte i = 0; i <= SIZE; i++){
            if(goal.tiles[i] != -1)
                rev[goal.tiles[i]] = i;
        }
        for(byte i = 0; i <= SIZE; i++){
            if(r.tiles[i] != 0 && r.tiles[i] != -1){
                total += Math.abs(i % LENGTH - rev[r.tiles[i]] % LENGTH) + Math.abs(i / LENGTH - rev[r.tiles[i]] / LENGTH);
                //now for the added reversal penalty -- basically if two adjacent tiles are occupying each other's space, add a penalty for each
                if((rev[r.tiles[i]] == i - LENGTH) && (rev[r.tiles[i-LENGTH]] == i)){
                    //if the tile you're supposed to be in, B, is above the tile you're in now, A, AND if the contents of tile B are supposed to be in tile A...
                    total++;
                }
                else if((rev[r.tiles[i]] == i + LENGTH) && (rev[r.tiles[i + LENGTH]] == i)){
                    total++;
                }
                else if((rev[r.tiles[i]] == i - 1) && (rev[r.tiles[i - 1]] == i)){
                    total++;
                }
                else if((rev[r.tiles[i]] == i + 1) && (rev[r.tiles[i + 1]] == i)){
                    total++;
                }
            }
        }
        return total;
    }

    //the actual astar algorithm... shouldn't need any changes from me... same with IndexMinPQ
    public static int astar(Nineteen start, Nineteen goal){
        System.out.println(" |moves|   |nodes in frontier|  |explored nodes|");
        int maxF = 0;

        Node z = new Node(start, null, 0, h(start, goal));
        IndexMinPQ<Node> frontier = new IndexMinPQ<>();
        frontier.add(z);
        HashMap<Nineteen, Node> explored = new HashMap<>();
        explored.put(start, z);

        while(true){ //dangerous.
            if(frontier.isEmpty()){
                return 0; // failure
            }

            Node x = frontier.remove();
            x.inFrontier = false;
            if(x.g + x.h > maxF){
                maxF = x.g + x.h;
                System.out.printf(" %3d      %10d        %10d\n", maxF, frontier.size(), explored.size());
            }
            if(x.state.equals(goal)){
                printAnswer(x);
                return 2;
            }
            for(int i = 0; i < 4; i++){
                if(x.state.moveActions[i].valid()){
                    x.state.moveActions[i].move();
                    Node n = explored.get(x.state);

                    if(n == null){
                        Nineteen s = new Nineteen(x.state.tiles, x.state.blankPos);
                        n = new Node(s, x, x.g+1, h(x.state, goal));
                        explored.put(s, n);
                        frontier.add(n);
                    }
                    else if(n.inFrontier){
                        if(x.g+1 < n.g){
                            n.parent = x;
                            n.g = x.g + 1;
                            frontier.update(n);
                        }
                    }
                    x.state.moveActions[opp[i]].move();
                }
            }
        }
    }

    public static void printAnswer(Node x){
        Stack<Node> stack = new Stack<>();
        int numMoves = 0;
        for(Node y = x; y != null; y = y.parent){
            stack.push(y);
            numMoves++;
        }
        while(!stack.isEmpty()){
            System.out.println(stack.pop());
        }
        System.out.println((numMoves-1) + " moves.");
    }

    //----- looks like copy/paste might work.

    public static int ids(Nineteen r, Nineteen goal){
        for(int limit=0;;limit++) {
            System.out.println(limit + " ");
            int result = bdfs(r, goal, limit);
            if(result != 1) {
                System.out.println(r + "\n\n");
                System.out.println();
                return result;
            }
        }
    }

    public static int bdfs(Nineteen r, Nineteen goal, int limit) {
        // returns 0: failure, 1: cutoff, 2: success
        if(r.equals(goal))
            return 2;
        else if(limit == 0)
            return 1;
        else {
            boolean cutoff = false;
            for(int i=0; i<4; i++) {
                if(r.moveActions[i].valid()) {
                    r.moveActions[i].move();
                    switch(bdfs(r, goal, limit-1)) {
                        case 1: cutoff = true; break;
                        case 2: return 2;
                        default:
                    }
                    r.moveActions[opp[i]].move();
                }
            }
            return (cutoff ? 1 : 0);
        }
    }
}
