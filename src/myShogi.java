import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class myShogi to run the game miniShogi, in interactive or file mode.
 *
 * @author Kedar Rudrawar
 */

public class myShogi {
    private Board board;

    private static final int TURNLIMIT = 200;


    public myShogi() {
        this.board = new Board();
    }

    public myShogi(Utils.TestCase tc) {
        this.board = new Board(tc);
    }

    public void printBoardAndStats() {
        Utils.stringifyBoard(this.board.getBoard());
        System.out.println(this.board.printBoardAndStats());
    }

    public void printIllegalMoveOutput(Player winnerPlayer) {
        System.out.println(this.board.printBoardAndStats());
        System.out.println(winnerPlayer.toString() + " player wins.  Illegal move.");
    }

    public void printCheckmateOutput(Player winnerPlayer) {
        System.out.println(this.board.printBoardAndStats());
        System.out.println(winnerPlayer.toString() + " player wins.  Checkmate.");
    }

    public boolean moveCountLimit() {
        if (board.getUpper().getTurnCount() == TURNLIMIT && board.getLower().getTurnCount() == TURNLIMIT) {
            System.out.println("Tie game.  Too many moves.");
            return true;
        }
        return false;
    }

    public void runInteractiveMode() {
        // Initialize board
        this.board = new Board();

        // Initialize players (all pieces of one team are initialized to same player)
        Player lower = this.board.getLower();
        Player upper = this.board.getUpper();

        boolean success;
        boolean lowerTurn = true;
        boolean checkmate = false;

        Player currPlayer = lower;
        Player opponentPlayer = upper;

        Scanner sc = new Scanner(System.in);

        String availableMoves = "";

        // Initial printing
        this.printBoardAndStats();
        System.out.print(currPlayer.getName() + "> ");

        String input = sc.nextLine();

        while (upper.getTurnCount() < TURNLIMIT || lower.getTurnCount() < TURNLIMIT) {
            if (input.equals("quit"))
                return;

            availableMoves = "";

            System.out.println(currPlayer.getName() + " player action: " + input);

            success = this.board.executeCommand(currPlayer, input);
            if (!success) {
                this.printIllegalMoveOutput(opponentPlayer);
                return;
            }

            Piece opponentKing = opponentPlayer.getKing();
            Location opponentKingLoc = opponentKing.getLocation();
            boolean opponentKingInCheck = this.board.isInCheckBoolean(opponentPlayer, opponentKingLoc);

            if (opponentKingInCheck) {
                List<String> allMoves = this.board.listAllAvailableMoves(opponentPlayer, opponentKingLoc);

                if (allMoves.size() == 0) {
                    this.printCheckmateOutput(currPlayer);
                    System.exit(0);
                } else {
                    availableMoves = this.board.printCheckOutput(opponentPlayer, allMoves);
                }
            }

            currPlayer.incrementTurn();

//            Flip turn:
            lowerTurn = !lowerTurn;

            if (lowerTurn) {
                currPlayer = lower;
                opponentPlayer = upper;
            } else {
                currPlayer = upper;
                opponentPlayer = lower;
            }

            this.printBoardAndStats();
            System.out.print(availableMoves);

            System.out.print(currPlayer.getName() + "> ");

            input = sc.nextLine();

        }
    }

    public void runFileMode(Utils.TestCase tc) {
        this.board = new Board(tc);
        Player currPlayer = this.board.getLower();
        Player opponentPlayer = this.board.getUpper();
        boolean lowerTurn = true;
        boolean checkmate = false;
        boolean lastMove = false;
        boolean success;

        String availableMoves;
        for (int i = 0; i < tc.moves.size(); i++) {
            String command = tc.moves.get(i);
            String[] commandSplit = command.split(" ");
            String action = commandSplit[0];
            int capturedIndex = 0;
            availableMoves = "";

            if (i == tc.moves.size() - 1) {
                lastMove = true;
            }

            success = this.board.executeCommand(currPlayer, command);
            if (!success) {
                System.out.println(currPlayer.getName() + " player action: " + command);
                this.printIllegalMoveOutput(opponentPlayer);
                System.exit(0);
            }

            Piece opponentKing = opponentPlayer.getKing();
            Location opponentKingLoc = opponentKing.getLocation();
            boolean opponentKingInCheck = this.board.isInCheckBoolean(opponentPlayer, opponentKingLoc);

            if (opponentKingInCheck) {
                List<String> allMoves = this.board.listAllAvailableMoves(opponentPlayer, opponentKingLoc);
                if (allMoves.size() == 0) {
                    success = false;
                    checkmate = true;
                } else {
                    availableMoves = this.board.printCheckOutput(opponentPlayer, allMoves);
                }
            }

            if (!success || lastMove) {
                System.out.println(currPlayer.getName() + " player action: " + command);
            }

//            FLIP TURN HERE

            lowerTurn = !lowerTurn;

            if (lowerTurn) {
                currPlayer = this.board.getLower();
                opponentPlayer = this.board.getUpper();
            } else {
                currPlayer = this.board.getUpper();
                opponentPlayer = this.board.getLower();
            }

            if (action.equals("drop")) {
                String pieceName = commandSplit[1];
                Location dropLoc = new Location(commandSplit[2]);
                if (pieceName.equalsIgnoreCase("p")) {
                    if (this.board.illegalPawnDrop(success, checkmate, opponentPlayer, dropLoc, capturedIndex)) {
                        this.printIllegalMoveOutput(currPlayer);
                        System.exit(0);
                    }
                }
            }

            if (lastMove) {
                if (!success) {
                    if (checkmate) {
                        this.printCheckmateOutput(opponentPlayer);
                    } else {
                        this.printIllegalMoveOutput(currPlayer);
                    }
                    System.exit(0);
                }

                System.out.println(this.board.printBoardAndStats());
                System.out.print(availableMoves);

                currPlayer.incrementTurn();
                if (moveCountLimit()) {
                    System.exit(0);
                }
                System.out.println(currPlayer.getName() + "> ");

            } else {
                currPlayer.incrementTurn();
                if (moveCountLimit()) {
                    System.exit(0);
                }
            }


        }

    }




    public static void main(String[] args) {
        if (args[0].equals("-f")) {
            Utils.TestCase tc = null;
            try {
                tc = Utils.parseTestCase(args[1]);
            } catch (Exception e) {
                System.out.println("caught exception");
            }

            myShogi game = new myShogi(tc);
            game.runFileMode(tc);

        } else {
            myShogi game = new myShogi();
            game.runInteractiveMode();
        }

    }
}




