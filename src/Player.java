import java.util.*;

public class Player {
	/* Maximum number of moves lookahead. */
	private int MAX_DEPTH = 11;
	private final int MIN = -100000; // Don't use Integer.MIN_VALUE to avoid integer overflow when negating
	private final int MAX = 100000;
	
	/* Values used by the analysis function. */
	private final int KING_POINTS = 8;
	private final int MAN_POINTS = 4;
	private final int DEFENSE_POINTS = 1;
	
	/* Comparators used for branching. */
	Comparator<GameState> whiteComparator;
	Comparator<GameState> redComparator;
	
	/* A class representing a node in the game tree. */
	private class Node {
		public GameState state;
		public int score;
		
		public Node(GameState state, int score) {
			set(state, score);
		}
		
		public void set(GameState state, int score) {
			this.state = state;
			this.score = score;
		}
	}
	
	/**
	* Perform the best move given the position stored in state.
	*
	* @param state The current state of the board
	* @param due Time before which we must have returned
	* @return The next state of the board after our move, never null
	*/
	public GameState play(final GameState state, final Deadline due) {
		Vector<GameState> nextStates = new Vector<GameState>();
		state.findPossibleMoves(nextStates);

		if (nextStates.size() == 0) return new GameState(state, new Move());
		
		/* Create comparators used to determine the order in which nodes should be processed. 
		 * The comparator for red sorts in ascending order and the comparator for white sorts
         * in descending order. */
		whiteComparator = new Comparator<GameState>() {
			@Override
			public int compare(GameState a, GameState b) {
				return analyze(b) - analyze(a);
			}
		};
		
		redComparator = new Comparator<GameState>() {
			@Override
			public int compare(GameState a, GameState b) {
				return analyze(a) - analyze(b);
			}
		};
		
		/* Look for and return the board corresponding to the best move. 
		* The player of the root node is given by getNextPlayer which returns
		* one of the values CELL_WHITE or CELL_RED. */
		Node bestChild = explore(state, state.getNextPlayer() == Constants.CELL_WHITE ? 1 : -1, 0, MIN, MAX);
		if (bestChild.state == null) {
			// I'll lose whatever I do :(
			return nextStates.get(0);
		}
		return bestChild.state;
	}
	
	/**
	* Explore a new node in the game tree. This method implements the
	* NegaMax algorithm with alpha-beta pruning. The NegaMax algorithm
	* is a variant of the MiniMax algorithm and works as follows:
	* 
	* GameState nextStates = getNextStates()
	* if (nextStates.empty) return -INF
	* if (depth == MAX_DEPTH) return analyze(state)
	* int bestScore
	* foreach (nextState in nextStates)
	*     int nextScore = -explore(nextState, asOpponent, depth + 1)
	*     if (nextScore > bestScore) bestScore = nextScore
	* return bestScore
	* 
	* In the sample code above, both players are maximizers. The value 
	* assigned to the current node is the maximum score of its children. 
	* -INF corresponds to an infinitely low score. If the maximum depth 
	* of the tree has been reached, use the analysis function to determine 
	* how strong the player is in this position.
	* 
	* Alpha-beta pruning is used to avoid expanding nodes which cannot
	* possibly hold a value that will be chosen by the parent node.
	* 
	* Let us consider the following MiniMax graph:
	* 
	*       A                 MAX
	*     _/|\_
	*   _/  |  \_
	*  /    |    \
	* B     C     D           MIN
	* |\    |\_         
	* | \   |  \_
	* 6  9  |    \
	*       E     F           MAX
	*      / \
	*     /   \
	*    4     -2
	* 
	* To find the value of A we first expand the node B. B will evaluate to 
	* 6 since B is a minimizer. After this we go to C and then E, which
	* will evaluate to 4 since E is a maximizer. Now, C is a minimizer
	* and will not choose a node with a value greater than 4.  A is a
	* maximizer and will not choose C, because B holds a greater value than 
	* C will ever have. So, we can stop processing C and prune the
	* node F. After this we continue to process D.
	* 
	* Thanks to Hamed Ahmadi -> hamedahmadi.com/gametree/
	* 
	* @param state The current state of the board
	* @param color 1 if white is to move, -1 otherwise
	* @param depth The number of turns made to reach this state
	* @param alpha The lower bound for pruning nodes
	* @param beta The upper bound for pruning nodes
	* @return The best child node
	*/ 
	private Node explore(GameState state, int color, int depth, int alpha, int beta) {
		/* Have my opponent lost? */
		if (state.isEOG()) {
			/* The opponent has played pass move which marks the end of game.
			 * This means that this state is a win for 'color'. */
			return new Node(state, MAX); // note the MAX value instead of MIN
		}
		
		/* Is the state a draw? */
		if (state.isDraw()) {
			return new Node(state, 0);
		}
		
		/* Is maximum depth reached? */
		if (depth == MAX_DEPTH) {
			return new Node(state, color * analyze(state));
		}
		
		Vector<GameState> nextStates = new Vector<GameState>();
		state.findPossibleMoves(nextStates);
		/* Perform branching by sorting the nodes and processing them in that order.
		 * This ensures as early alpha-beta cutoffs as possible, with increased search speed 
		 * as result. Sorting is done from white's perspective using a comparator, and should 
		 * be done in ascending order if red is to move, and in descending order if white 
		 * is to move. */
		if (color == -1) Collections.sort(nextStates, redComparator);
		else Collections.sort(nextStates, whiteComparator);

		Node bestChild = new Node(null, MIN);
		for (int i = 0; i < nextStates.size(); i++) {
			/* Note that alpha and beta are negated and have switched place! */
			Node nextChild = explore(nextStates.get(i), -color, depth + 1, -beta, -alpha);
			/* Don't forget to negate the score value, or NegaMax won't work properly. */
			nextChild.score = -nextChild.score;
			if (nextChild.score > bestChild.score) { 
				bestChild.set(nextStates.get(i), nextChild.score);
			}
			/* This is where we perform the alpha-beta pruning.
			* 
			* The maximizer tries to raise the lower bound
			* using the score of its children and prunes with
			* the lower bound as return value when the lower
			* bound crosses the upper bound.
			* 
			* The minimizer tries to lower the upper bound
			* using the score of its children and prunes with
			* the upper bound as return value when the lower bound
			* crosses the upper bound. In our case, both players
			* act as maximizers. */ 
			if (nextChild.score > alpha) alpha = nextChild.score;
			if (alpha >= beta) break;
		}
		return bestChild;
	}
	
	/**
	* Analyse a given position and return a score.
	* The score returned is linear-weighted sum based on material, but a more 
	* advanced analysis function might be used. A high score means a good
	* position for white.
	* 
	* The score in the current implementation is based on the difference
	* of material between the white and the red opponent. A man is worth 
	* MAN_POINTS points and a king is worth KING_POINTS points.
	* 
	* @param state The state of the game that should be analysed.
	* @return The score for a player, a score greater than zero is favourable 
	* for white.
	*/
	private int analyze2(GameState state) {
		/* Look through all cells and sum up the material for both 
		* players. */
		int white = 0;
		int red = 0;
		for (int p = 1; p < 33; p++) {
			if ((state.get(p) & Constants.CELL_WHITE) != 0) {
				/* This position contains a white piece. */
				if ((state.get(p) & Constants.CELL_KING) != 0) white += KING_POINTS;
				else white += MAN_POINTS;
			} else if ((state.get(p) & Constants.CELL_RED) != 0) {
				/* This position contains a red piece. */
				if ((state.get(p) & Constants.CELL_KING) != 0) red += KING_POINTS;
				else red += MAN_POINTS;
			}
		}
		return white - red;
	}
	
	/**
	* Analyse a given position and return a score.
	* The score returned is linear-weighted sum based on material and tactical position.
	* 
	* @param state The state of the game that should be analysed.
	* @return The score for a player, a score greater than zero is favourable 
	* for white.
	*/
	private int analyze(GameState state) {
		int white = 0;
		int red = 0;
		for (int p = 1; p < 33; p++) {
			int cell = state.get(p);
			int cP = Constants.CELL_INVALID; /* Player who has a piece on this position. */
			if ((cell & Constants.CELL_WHITE) != 0) cP = Constants.CELL_WHITE;
			if ((cell & Constants.CELL_RED) != 0) cP = Constants.CELL_RED;
			if (cP == Constants.CELL_INVALID) continue; /* No content. */
			int rP = 0; /* Points for this round. */

			/* Check type of piece. */
			if ((cell & Constants.CELL_KING) != 0) rP += KING_POINTS;
			else rP += MAN_POINTS;
			/* Check neighbouring cells for defending pieces.
			 *
			 * Example: defending piece upper left upper right (strike position)
			 * Protects pieces from being jumped over in both directions (\ and /).
			 * Does not block movement.
			 * x . x
			 * . x .
			 * . . .
			 * Example: defending piece on both diagonals.
			 * Protects all three pieces from being jumped over in
			 * this direction: / but blocks movement.
			 * . . x
			 * . x .
			 * x . .
			 * Piece is considered protected if it has at least one defending
			 * piece in one of the diagonally neighbouring cells. */
			int pR = state.cellToRow(p);
			int pC = state.cellToCol(p);
			
			/* Extract neighbouring cells. */
			int uL = state.get(pR - 1, pC - 1);
			int uR = state.get(pR - 1, pC + 1);
			int lL = state.get(pR + 1, pC - 1);
			int lR = state.get(pR + 1, pC + 1);
			
			/* Check if this piece is protected by the edge of the board. */
			if (((uL & Constants.CELL_INVALID) != 0) || ((lR & Constants.CELL_INVALID) != 0)) {
				/* This piece cannot be jumped over in either direction. */
				rP += DEFENSE_POINTS * 2;
			} else if ((uL & cP) != 0) {
				/* Protected from being jumped over in this direction: \ */
				if ((lR & cP) != 0) rP += DEFENSE_POINTS;
				else rP += DEFENSE_POINTS * 2;
			} else if ((uR & cP) != 0) {
				/* Protected from being jumped over in this direction: / */
				if ((lL & cP) != 0) rP += DEFENSE_POINTS;
				else rP += DEFENSE_POINTS * 2;
			}
			/* Add points for this round to total. */
			if (cP == Constants.CELL_WHITE) white += rP;
			else red += rP;
		}
		return white - red;
	}
	
	/**
	* Count the number of red and white pieces.
	* @param state The board to count pieces on.
	* @return The number of pieces on the board provided.
	*/
	private int countPieces(GameState state) {
		int pieces = 0;
		for (int p = 1; p < 33; p++) {
			if ((state.get(p) & Constants.CELL_WHITE) != 0 || (state.get(p) & Constants.CELL_RED) != 0) pieces++;
		}
		return pieces;
	}
	
	/**
	* Print a debug message to std err.
	*/
	private void d(String msg, int depth) {
		StringBuilder sb = new StringBuilder();
		while (depth --> 0) sb.append('>');
		sb.append(msg);
		System.err.println(sb.toString());
	}
}
