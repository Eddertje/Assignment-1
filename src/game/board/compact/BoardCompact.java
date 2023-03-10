package game.board.compact;

import game.actions.EDirection;
import game.board.compressed.BoardCompressed;
import game.board.compressed.MTile;
import game.board.compressed.MTile.SubSlimTile;
import game.board.minimal.StateMinimal;
import game.board.oop.Board;
import game.board.oop.EEntity;
import game.board.oop.EPlace;
import game.board.oop.ESpace;
import game.board.slim.BoardSlim;
import game.board.slim.STile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * More memory-compact representation of OOP-bulky {@link Board}.
 * 
 * BEWARE: once {@link #hashCode()} is called and you use {@link #moveBox(int, int, int, int)} or {@link #movePlayer(int, int, int, int)} it will
 *         force {@link #hashCode()} recomputation.
 * 
 * @author Jimmy
 */
public class BoardCompact implements Cloneable, Comparable {

	private Integer hash = null;
	
	/**
	 * Compact representation of tiles.
	 */
	public int[][] tiles;
	
	public int playerX;
	public int playerY;
	
	public int boxCount;
	public int boxInPlaceCount;

	public EDirection action;
	public BoardCompact previousState;
	private int gn;
	public boolean[][] deadSquares;

	private float heuristic = -1;

	private static HashMap<Integer, Float> outcomes;

	public static int saves;
	
	private BoardCompact() {
	}
	
	public BoardCompact(int width, int height, boolean[][] deadSquares) {
		tiles = new int[width][height];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				tiles[x][y] = 0;
			}			
		}
		action = null;
		previousState = null;
		outcomes = new HashMap<Integer, Float>();
		saves = 0;
		this.deadSquares = deadSquares;
	}
	
	@Override
	public BoardCompact clone() {
		BoardCompact result = new BoardCompact();
		result.tiles = new int[width()][height()];
		for (int x = 0; x < width(); ++x) {
			for (int y = 0; y < height(); ++y) {
				result.tiles[x][y] = tiles[x][y];
			}			
		}
		result.playerX = playerX;
		result.playerY = playerY;
		result.boxCount = boxCount;
		result.boxInPlaceCount = boxInPlaceCount;
		result.previousState = this;
		result.deadSquares = deadSquares;
		result.gn = this.gn + 1;
		return result;
	}
	
	@Override
	public int hashCode() {
		if (hash == null) {
			hash = 0;
			for (int x = 0; x < width(); ++x) {
				for (int y = 0; y < height(); ++y) {
					hash += (290317 * x + 97 * y) * tiles[x][y];
				}		
			}
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (obj.hashCode() != hashCode()) return false;
		if (!(obj instanceof BoardCompact)) return false;		
		return equalsState((BoardCompact) obj);
	}
	
	public boolean equalsState(BoardCompact other) {
		if (other == null) return false;
		if (width() != other.width() || height() != other.height()) return false;
		for (int x = 0; x < width(); ++x) {
			for (int y = 0; y < height(); ++y) {
				if (tiles[x][y] != other.tiles[x][y]) return false;
			}			
		}
		return true;
	}
	
	public int width() {
		return tiles.length;		
	}
	
	public int height() {
		return tiles[0].length;
	}
	
	public int tile(int x, int y) {
		return tiles[x][y];
	}
	
	/**
	 * Fair warning: by moving the player you're invalidating {@link #hashCode()}...
	 * @param sourceTileX
	 * @param sourceTileY
	 * @param targetTileX
	 * @param targetTileY
	 */
	public void movePlayer(int sourceTileX, int sourceTileY, int targetTileX, int targetTileY) {
		int entity = tiles[sourceTileX][sourceTileY] & EEntity.SOME_ENTITY_FLAG;
		
		tiles[targetTileX][targetTileY] &= EEntity.NULLIFY_ENTITY_FLAG;
		tiles[targetTileX][targetTileY] |= entity;
		
		tiles[sourceTileX][sourceTileY] &= EEntity.NULLIFY_ENTITY_FLAG;
		tiles[sourceTileX][sourceTileY] |= EEntity.NONE.getFlag();	
		
		playerX = targetTileX;
		playerY = targetTileY;
		
		hash = null;
	}
	
	/**
	 * Fair warning: by moving the box you're invalidating {@link #hashCode()}...
	 * @param sourceTileX
	 * @param sourceTileY
	 * @param targetTileX
	 * @param targetTileY
	 */
	public void moveBox(int sourceTileX, int sourceTileY, int targetTileX, int targetTileY) {
		int entity = tiles[sourceTileX][sourceTileY] & EEntity.SOME_ENTITY_FLAG;
		int boxNum = CTile.getBoxNum(tiles[sourceTileX][sourceTileY]);

		if (CTile.forBox(boxNum, tiles[targetTileX][targetTileY]) || CTile.forAnyBox(tiles[targetTileX][targetTileY])) {
			++boxInPlaceCount;
		}
		tiles[targetTileX][targetTileY] &= EEntity.NULLIFY_ENTITY_FLAG;
		tiles[targetTileX][targetTileY] |= entity;
		
		if (CTile.forBox(boxNum, tiles[sourceTileX][sourceTileY]) || CTile.forAnyBox(tiles[sourceTileX][sourceTileY])) {
			--boxInPlaceCount;
		}
		tiles[sourceTileX][sourceTileY] &= EEntity.NULLIFY_ENTITY_FLAG;
		tiles[sourceTileX][sourceTileY] |= EEntity.NONE.getFlag();
		
		hash = null;
	}
	
	/**
	 * Whether the board is in WIN-STATE == all boxes are in correct places.
	 * 
	 * @return
	 */
	public boolean isVictory() {
		return boxCount == boxInPlaceCount;
	}
	
	/**
	 * Adds "state" to this board, has sense only if {@link #unsetState(StateMinimal)} has been previously called.
	 * @param state
	 */
	public void setState(StateMinimal state) {
		playerX = state.getX(state.positions[0]);
		playerY = state.getY(state.positions[0]);
		boxInPlaceCount = 0;

		tiles[playerX][playerY] = (tiles[playerX][playerY] & EEntity.NULLIFY_ENTITY_FLAG) | EEntity.PLAYER.getFlag();
		
		for (int i = 1; i < state.positions.length; ++i) {
			int x = state.getX(state.positions[i]);
			int y = state.getY(state.positions[i]);
			tiles[x][y] = (tiles[x][y] & EEntity.NULLIFY_ENTITY_FLAG) | EEntity.BOX_1.getFlag();
			if (CTile.forSomeBox(tiles[x][y])) ++boxInPlaceCount;
		}
	}
	
	/**
	 * Removes "dynamic" information from the board, leaves statics only. Use {@link #setState(StateMinimal)} to put the state back...
	 * @param state
	 */
	public void unsetState(StateMinimal state) {

		tiles[playerX][playerY] = (tiles[playerX][playerY] & EEntity.NULLIFY_ENTITY_FLAG) | EEntity.NONE.getFlag();

		playerX = -1;
		playerY = -1;
		boxInPlaceCount = -1;

		for (int i = 1; i < state.positions.length; ++i) {
			int x = state.getX(state.positions[i]);
			int y = state.getY(state.positions[i]);
			tiles[x][y] = (tiles[x][y] & EEntity.NULLIFY_ENTITY_FLAG) | EEntity.NONE.getFlag();
		}
	}

	/**
	 * Prints the board into {@link System#out}.
	 */
	public void debugPrint() {
		System.out.print(getBoardString());
		System.out.println();
	}
	
	/**
	 * String representation of the board.
	 * @return
	 */
	public String getBoardString() {
		StringBuffer sb = new StringBuffer();
		
		for (int y = 0; y < height(); ++y) {
			if (y != 0) sb.append("\n");
			for (int x = 0; x < width(); ++x) {
				EEntity entity = EEntity.fromFlag(tiles[x][y]);
				EPlace place = EPlace.fromFlag(tiles[x][y]);
				ESpace space = ESpace.fromFlag(tiles[x][y]);
				
				if (entity != null && entity != EEntity.NONE) {
					sb.append(entity.getSymbol());
				} else
				if (place != null && place != EPlace.NONE) {
					sb.append(place.getSymbol());
				} else
				if (space != null) {
					sb.append(space.getSymbol());
				} else {
					sb.append("?");
				}
			}			
		}
		
		return sb.toString();
	}
	
	public BoardCompressed makeBoardCompressed() {
		BoardCompressed result = new BoardCompressed(width(), height());
		result.boxCount = boxCount;
		result.boxInPlaceCount = boxInPlaceCount;
		result.playerX = playerX;
		result.playerY = playerY;
		
		for (int x = 0; x < width(); ++x) {
			for (int y = 0; y < height(); ++y) {
				SubSlimTile sst = MTile.getSubSlimTile(x, y);
				int tx = x / 2;
				int ty = y / 2;
				result.tiles[tx][ty] |= computeCompressedTile(sst, x, y);
			}
		}
		
		return result;
	}
	
	public int computeCompressedTile(SubSlimTile subSlimTile, int x, int y) {
		int compact = tile(x, y);
		
		int result = 0;
		
		if (CTile.forSomeBox(compact)) result |= subSlimTile.getPlaceFlag();		
		if (CTile.isFree(compact)) return result;
		if (CTile.isWall(compact)) {
			result |= subSlimTile.getWallFlag();
			return result;
		}
		if (CTile.isSomeBox(compact)) {
			result |= subSlimTile.getBoxFlag();
			return result;
		}		
		if (CTile.isPlayer(compact)) {
			result |= subSlimTile.getPlayerFlag();
			return result;
		}
		
		return result;
	}
	
	public BoardSlim makeBoardSlim() {
		BoardSlim result = new BoardSlim((byte)width(), (byte)height());
		result.boxCount = (byte)boxCount;
		result.boxInPlaceCount = (byte)boxInPlaceCount;
		result.playerX = (byte)playerX;
		result.playerY = (byte)playerY;
		
		for (int x = 0; x < width(); ++x) {
			for (int y = 0; y < height(); ++y) {
				result.tiles[x][y] = computeSlimTile(x, y);
			}
		}
		
		return result;
	}
	
	public byte computeSlimTile(int x, int y) {
		int compact = tile(x, y);
		
		byte result = 0;
		
		if (CTile.forSomeBox(compact)) result |= STile.PLACE_FLAG;		
		if (CTile.isFree(compact)) return result;
		if (CTile.isWall(compact)) {
			result |= STile.WALL_FLAG;
			return result;
		}
		if (CTile.isSomeBox(compact)) {
			result |= STile.BOX_FLAG;
			return result;
		}		
		if (CTile.isPlayer(compact)) {
			result |= STile.PLAYER_FLAG;
			return result;
		}
		
		return result;
	}
	
	
	@Override
	public String toString() {
		return "BoardCompact[\n" + getBoardString() + "\n]";
	}

	private int gn() {
		return this.gn;
	}

	private float fn() {
		if(heuristic != -1) {
			return heuristic;
		}
		// HEURISTIC
		List<int[]> boxes = new ArrayList<>();
		List<int[]> flags = new ArrayList<>();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				if(CTile.forSomeBox(tiles[i][j])) {
					flags.add(new int[]{i, j});
				}
				if(CTile.isSomeBox(tiles[i][j])) {
					boxes.add(new int[]{i, j});
				}
			}
		}

		float total = Float.POSITIVE_INFINITY;
		for (int[] box : boxes) {
			float distance = Math.abs(box[0] - playerX) + Math.abs(box[1] - playerY);
			total = Math.min(total, distance - 1);
		}
		/*
		if(outcomes.containsKey(boxes.hashCode())) {
			saves++;
			return total + outcomes.get(boxes.hashCode());
		}
		HungarianAlgorithm ha = new HungarianAlgorithm(boxes, flags);
		int[][] matches = ha.findOptimalAssignment();
		float minimumMatch = 0;
		for (int[] match : matches) {
			int[] box = boxes.get(match[1]);
			int[] flag = flags.get(match[0]);
			minimumMatch += Math.abs(box[0] - flag[0]) + Math.abs(box[1] - flag[1]);
		}
		 */

		float minimumMatch = 0;
		for(int[] box : boxes) {
			float currentMin = Float.POSITIVE_INFINITY;
			for (int[] flag : flags) {
				currentMin = Math.min(currentMin, Math.abs(box[0] - flag[0]) + Math.abs(box[1] - flag[1]));
			}
			minimumMatch += currentMin;
		}


		//outcomes.put(boxes.hashCode(), minimumMatch);
		total += minimumMatch;

		heuristic = total;
		return total;
	}
	/*
solving level 1... 93893
solved in 1630,0 ms (97 steps)
solving level 2... 81259
solved in 1211,0 ms (148 steps)
solving level 3... 97938
solved in 1444,0 ms (104 steps)
solving level 4... 123421
solved in 2096,0 ms (113 steps)
solving level 5... 127617
solved in 2270,0 ms (171 steps)
solving level 6... 147892
solved in 2691,0 ms (136 steps)
minimal distance
solving level 1... solved in 5937,0 ms (119 steps)
solving level 2... solved in 6952,0 ms (262 steps)
solving level 3... solved in 7821,0 ms (151 steps)
solving level 4... solved in 9094,0 ms (118 steps)
solving level 5... solved in 9937,0 ms (245 steps)
solving level 6... solved in 13324,0 ms (201 steps)
solving level 7... solved in 15913,0 ms (140 steps)
solving level 8... solved in 19106,0 ms (302 steps)
solving level 9... solved in 23813,0 ms (158 steps)
solving level 10... solved in 21728,0 ms (132 steps)
solving level 11... solved in 25050,0 ms (146 steps)
solving level 12... solved in 28021,0 ms (211 steps)
solving level 13... solved in 48689,0 ms (147 steps)
static map improvement
solving level 1... solved in 1695,0 ms (97 steps)
solving level 2... solved in 1243,0 ms (148 steps)
solving level 3... solved in 1504,0 ms (104 steps)
solving level 4... solved in 2230,0 ms (113 steps)
solving level 5... solved in 2258,0 ms (171 steps)
solving level 6... solved in 2595,0 ms (136 steps)
solving level 7... solved in 2373,0 ms (178 steps)
solving level 8... solved in 2618,0 ms (69 steps)
solving level 9... solved in 1667,0 ms (110 steps)
solving level 10... solved in 2529,0 ms (113 steps)
Hard:
solving level 1... solved in 8242,0 ms (119 steps)
solving level 2... solved in 9567,0 ms (262 steps)
solving level 3... solved in 10185,0 ms (151 steps)
solving level 4... solved in 10837,0 ms (118 steps)
solving level 5... solved in 11583,0 ms (245 steps)
solving level 6... solved in 16778,0 ms (201 steps)
solving level 7... solved in 19037,0 ms (140 steps)
solving level 8... solved in 15608,0 ms (302 steps)
solving level 9... solved in 23969,0 ms (158 steps)
solving level 10... solved in 23325,0 ms (132 steps)
solving level 11... solved in 20616,0 ms (146 steps)
solving level 12... solved in 29724,0 ms (211 steps)
solving level 13... solved in 54872,0 ms (147 steps)
static map improvement:
solving level 1... solved in 6833,0 ms (119 steps)
solving level 2... solved in 8072,0 ms (262 steps)
solving level 3... solved in 9244,0 ms (151 steps)
solving level 4... solved in 10258,0 ms (118 steps)
solving level 5... solved in 11242,0 ms (245 steps)
solving level 6... solved in 15302,0 ms (201 steps)
solving level 7... solved in 18077,0 ms (140 steps)
solving level 8... solved in 15449,0 ms (302 steps)
solving level 9... solved in 23590,0 ms (158 steps)
solving level 10... solved in 22872,0 ms (132 steps)
solving level 11... solved in 20158,0 ms (146 steps)
solving level 12... solved in 28527,0 ms (211 steps)
solving level 13... solved in 48358,0 ms (147 steps)
	*/

	private float cost() {
		return this.gn() + this.fn();
	}

	public List<EDirection> getActions() {
		if (this.previousState == null) {
			return new ArrayList<>();
		} else {
			List<EDirection> result = previousState.getActions();
			result.add(this.action);
			return result;
		}
	}

	@Override
	public int compareTo(Object o) {
		BoardCompact other = (BoardCompact) o;
		return (int) Math.round(this.cost() - other.cost());
	}
}
