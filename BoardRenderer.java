import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * This class creates the GUI for the board
 * 
 * @author Andrew Davidson. Created May 7, 2010.
 */
public class BoardRenderer extends JComponent implements MouseListener {
	private int startingX = 380;
	private int startingY = 640;
	private Hex[] boardArray = new Hex[Main.boardSize];
	private int[] colorNumberArray;
	private int[] rollNumberArray;
	private Point2D.Double[] pointArray = new Point2D.Double[Main.boardSize];
	private ArrayList<Structure> structureArray = new ArrayList<Structure>();
	private UserPanel userPanel;

	/**
	 * constructs an empty, randomized board.
	 * 
	 * @param randomColorArray
	 * @param randomNumberArray
	 * @param myPanel
	 */
	public BoardRenderer(int[] randomColorArray, int[] randomNumberArray,
			UserPanel myPanel) {
		this.setPreferredSize(new Dimension(800, 800));
		this.colorNumberArray = randomColorArray;
		this.rollNumberArray = randomNumberArray;
		this.userPanel = myPanel;
		this.addMouseListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(0, 0, 100));
		g2.fill(new Rectangle2D.Double(0, 0, 900, 800));
		for (int i = 0; i < Main.boardSize; i++)
			this.boardArray[i].drawHex(g2);
		for (Structure structure : this.structureArray)
			this.boardArray[structure.getHex()].drawStructures(g2, structure);
	}

	/**
	 * creates the randomized hexes on the board.
	 * 
	 */
	public void setBoard() {
		double rightShift = 1.5 * Hex.RADIUS;
		double diagonalUpShift = (Hex.RADIUS * Hex.Y_SCALAR);
		double leftShift = 1.5 * Hex.RADIUS;
		double eachUpShift = Hex.RADIUS * Hex.Y_SCALAR;

		int diagonalSquares = 3;
		int switcher = 1;
		int rightCoeff = 0;
		int startIndex = 0;
		int diagCoeff = 0;

		// creates an array of the 19 hexes on the board.
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < diagonalSquares; j++) {

				this.boardArray[j + startIndex] = new Hex(this.startingX
						+ rightCoeff * rightShift - j * leftShift,
						this.startingY - diagCoeff * diagonalUpShift - j
								* eachUpShift, this.colorNumberArray[j
								+ startIndex], this.rollNumberArray[j
								+ startIndex]);

			}
			rightCoeff++;
			diagCoeff++;
			if (diagonalSquares == 5)
				switcher = -1;
			if (switcher == -1) {
				rightCoeff = 2;
				diagCoeff++;
			}
			startIndex += diagonalSquares;
			diagonalSquares += switcher;
		}
		for (int i = 0; i < Main.boardSize; i++) {
			this.pointArray[i] = new Point2D.Double(this.boardArray[i].getX(),
					this.boardArray[i].getY());
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		int[] nearArray = findNearestHexes(arg0.getX(), arg0.getY());
		int pos = 0;
		if (nearArray[1] != -1) {
			int build = this.userPanel.getBuildType();
			if (build > 0) {
				if (build < 3)
					pos = this.determineSettlePosition(nearArray);
				else
					pos = this.determineRoadPosition(nearArray);
				System.out.println(pos);
				Structure structure = new Structure(this.userPanel
						.getCurrentPlayer(), pos, build, nearArray[0],
						nearArray[1], nearArray[2], this.userPanel
								.getPlayerColor());
				// if(this.isNotOccupied(pos, nearArray, structure))
				this.structureArray.add(structure);
			}
			this.repaint();
		}

	}

	/**
	 * determines which position in a hex a settlement should be built at.
	 * 
	 * @param findNearestHexes
	 * @return position to place structure
	 */
	private int determineSettlePosition(int[] nearHex) {
		int pos = 0;

		if (nearHex[0] == nearHex[1] - 1 && nearHex[0] > nearHex[2]
				|| nearHex[0] == nearHex[2] - 1 && nearHex[0] > nearHex[1])
			pos = 1;
		if (nearHex[0] == nearHex[1] - 1 && nearHex[0] < nearHex[2]
				|| nearHex[0] == nearHex[2] - 1 && nearHex[0] < nearHex[1])
			pos = 2;
		if (nearHex[0] == nearHex[1] + 1 && nearHex[0] > nearHex[2]
				|| nearHex[0] == nearHex[2] + 1 && nearHex[0] > nearHex[1])
			pos = -2;
		if (nearHex[0] == nearHex[1] + 1 && nearHex[0] < nearHex[2]
				|| nearHex[0] == nearHex[2] + 1 && nearHex[0] < nearHex[1])
			pos = -1;
		if (nearHex[0] < nearHex[1] - 1 && nearHex[0] < nearHex[2] - 1)
			pos = 3;
		if (nearHex[0] > nearHex[1] + 1 && nearHex[0] > nearHex[2] + 1)
			pos = -3;
		// deals with border hexes/positions
		if (nearHex[4] > Hex.RADIUS * 1.5) {

			if (nearHex[3] < Hex.RADIUS * 1.5) {
				// deals with the settlements on the coast that border two
				// hexes.
				switch (nearHex[0]) {
				case 1:
					if (nearHex[0] > nearHex[1]) {
						pos = -2;
						break;
					} else
						pos = 1;
					break;
				case 3:
					if (nearHex[0] > nearHex[1]) {
						pos = -3;
						break;
					} else
						pos = -1;
					break;
				case 6:
					if (nearHex[0] > nearHex[1]) {
						pos = -3;
						break;
					} else
						pos = 2;
					break;
				case 12:
					if (nearHex[0] > nearHex[1]) {
						pos = -2;
						break;
					} else
						pos = 3;
					break;
				case 15:
					if (nearHex[0] > nearHex[1]) {
						pos = 1;
						break;
					} else
						pos = 3;
					break;
				case 17:
					if (nearHex[0] > nearHex[1]) {
						pos = -1;
						break;
					} else
						pos = 2;
					break;
				case 0:
					if (nearHex[0] > nearHex[1] - 2) {
						pos = 1;
						break;
					} else
						pos = -1;
					break;
				case 2:
					if (nearHex[0] > nearHex[1]) {
						pos = -2;
						break;
					} else
						pos = 2;
					break;
				case 7:
					if (nearHex[0] > nearHex[1]) {
						pos = -3;
						break;
					} else
						pos = 3;
					break;
				case 11:
					if (nearHex[0] > nearHex[1]) {
						pos = -3;
						break;
					} else
						pos = 3;
					break;
				case 16:
					if (nearHex[0] > nearHex[1]) {
						pos = -2;
						break;
					} else
						pos = 2;
					break;
				case 18:
					if (nearHex[0] > nearHex[1] + 2) {
						pos = 1;
						break;
					} else
						pos = -1;
					break;
				}
			} else {
				// deals with positions that only border the hex and the ocean.
				switch (nearHex[0]) {
				case 1:
					pos = -3;
					break;
				case 3:
					pos = -2;
					break;
				case 6:
					pos = 1;
					break;
				case 12:
					pos = -1;
					break;
				case 15:
					pos = 2;
					break;
				case 17:
					pos = 3;
					break;
				}
				// deals with hexes that have two positions that only border the
				// ocean and the main hex.
				if (nearHex[4] > Hex.RADIUS * 2.25) {
					switch (nearHex[0]) {
					case 0:
						if (nearHex[0] > nearHex[1] - 2) {
							pos = -3;
							break;
						} else
							pos = -2;
						break;
					case 2:
						if (nearHex[0] > nearHex[1]) {
							pos = -3;
							break;
						} else
							pos = 1;
						break;
					case 7:
						if (nearHex[0] > nearHex[1]) {
							pos = -2;
							break;
						} else
							pos = -1;
						break;
					case 11:
						if (nearHex[0] > nearHex[1]) {
							pos = 1;
							break;
						} else
							pos = 2;
						break;
					case 16:
						if (nearHex[0] > nearHex[1]) {
							pos = -1;
							break;
						} else
							pos = 3;
						break;
					case 18:
						if (nearHex[0] > nearHex[1] + 2) {
							pos = 2;
							break;
						} else
							pos = 3;
						break;
					}
				}

			}

		}
		return pos;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// does nothing.
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// does nothing.

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// does nothing.

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// does nothing.

	}

	// returns an array of: the hex clicked, the second nearest hex, the third
	// nearest, the distance to the center of the second nearest hex from the
	// mouse click, and the same for the third.
	private int[] findNearestHexes(int xCoord, int yCoord) {
		int[] nearArray = new int[5];
		int recordA = -1;
		int recordB = -1;
		int recordC = -1;
		double distanceRecordB = 10 * Hex.RADIUS;
		double distanceRecordA = 10 * Hex.RADIUS;
		double distanceRecordC = 10 * Hex.RADIUS;
		for (int i = 0; i < Main.boardSize; i++) {
			double deltaX = xCoord - this.pointArray[i].getX();
			double deltaY = yCoord - this.pointArray[i].getY();
			double distanceCurrent = Math.sqrt(deltaX * deltaX + deltaY
					* deltaY);

			// cycles through the arraylist of center points of the hexes and
			// compares them to the mouse click. The top three 'record-setters'
			// are kept.
			if (distanceCurrent < distanceRecordC) {
				if (distanceCurrent < distanceRecordB) {
					if (distanceCurrent < distanceRecordA) {
						distanceRecordC = distanceRecordB;
						distanceRecordB = distanceRecordA;
						distanceRecordA = distanceCurrent;
						recordC = recordB;
						recordB = recordA;
						recordA = i;
					} else {
						distanceRecordC = distanceRecordB;
						distanceRecordB = distanceCurrent;
						recordC = recordB;
						recordB = i;
					}
				} else {
					distanceRecordC = distanceCurrent;
					recordC = i;
				}
			}
		}
		System.out.println(recordA);
		System.out.println(recordB);
		System.out.println(recordC);
		nearArray[0] = recordA;
		nearArray[1] = recordB;
		nearArray[2] = recordC;
		nearArray[3] = (int) distanceRecordB;
		nearArray[4] = (int) distanceRecordC;
		// if the mouse click was in the center of the hex, the neighbor is set
		// to -1 to signal that the mouse click was not valid.
		if (distanceRecordA < Hex.RADIUS / 2)
			nearArray[1] = -1;
		return nearArray;
	}

	// does not work yet.
	@SuppressWarnings("unused")
	private boolean isNotOccupied(int pos, int[] nearArray, Structure structure) {
		// for (Structure structure : this.structureArray) {
		if (structure.getPos() == pos && structure.getHex() == nearArray[0]) {
			System.out.println("Cliffs of dover");
			return false;
		}
		for (int i = 0; i < 3; i++) {
			if (structure.getHex() == nearArray[i]
					&& structure.getNeighborA() == nearArray[(i + 1)]
					&& structure.getNeighborB() == nearArray[(i + 2)]) {
				System.out.println("Cliffs of dover");
				return false;
			}
			if (structure.getHex() == nearArray[i]
					&& structure.getNeighborA() == nearArray[(i + 2) % 3]
					&& structure.getNeighborB() == nearArray[(1 + i) % 3]) {
				System.out.println("Cliffs of dover");
				return false;

			}
			// }
		}

		return true;
	}

	// determines the position for the road to be built on the clicked hex.
	private int determineRoadPosition(int[] nearHex) {
		int pos = 0;
		int[] rowArray = new int[5];
		rowArray[0] = 3;
		rowArray[1] = 4;
		rowArray[2] = 5;
		rowArray[3] = 4;
		rowArray[4] = 3;
		int row = -1;
		if (nearHex[0] < 3)
			row = 0;
		else if (nearHex[0] < 7)
			row = 1;
		else if (nearHex[0] < 12)
			row = 2;
		else if (nearHex[0] < 16)
			row = 3;
		else
			row = 4;

		if (nearHex[1] == nearHex[0] + 1)
			pos = 1;
		if (nearHex[1] == nearHex[0] - 1)
			pos = -1;
		if (row > 2) {

			if (nearHex[0] == nearHex[1] + rowArray[row])
				pos = -3;
			if (nearHex[0] == nearHex[1] - rowArray[4])
				pos = 3;
			if (nearHex[0] == nearHex[1] + rowArray[row - 1])
				pos = -2;
			if (nearHex[0] == nearHex[1] - rowArray[row])
				pos = 2;
			if (nearHex[3] > Hex.RADIUS * Hex.Y_SCALAR)
				System.out.println(nearHex[3]);

		} else if (row == 2) {

			if (nearHex[0] == nearHex[1] + rowArray[row])
				pos = -2;
			if (nearHex[0] == nearHex[1] - rowArray[row])
				pos = 2;
			if (nearHex[0] == nearHex[1] + rowArray[row - 1])
				pos = -3;
			if (nearHex[0] == nearHex[1] - rowArray[3])
				pos = 3;

		} else if (row < 2) {

			if (nearHex[0] == nearHex[1] - rowArray[row + 1])
				pos = 2;
			if (nearHex[0] == nearHex[1] + rowArray[row])
				pos = -2;
			if (nearHex[0] == nearHex[1] - rowArray[row])
				pos = 3;
			if (nearHex[0] == nearHex[1] + rowArray[0])
				pos = -3;
		}
		// catches the exception cases where the road is along the coast.

		if (nearHex[4] > Hex.RADIUS * 2 - 5) {
			switch (nearHex[0]) {
			case 1:
				if (nearHex[0] > nearHex[1]) {
					pos = -2;
					break;
				} else
					pos = -3;
				break;
			case 3:
				if (nearHex[0] > nearHex[1]) {
					pos = -2;
					break;
				} else
					pos = -1;
				break;
			case 6:
				if (nearHex[0] > nearHex[1]) {
					pos = -3;
					break;
				} else
					pos = 1;
				break;
			case 12:
				if (nearHex[0] > nearHex[1]) {
					pos = -1;
					break;
				} else
					pos = 3;
				break;
			case 15:
				if (nearHex[0] > nearHex[1]) {
					pos = 1;
					break;
				} else
					pos = 2;
				break;
			case 17:
				if (nearHex[0] > nearHex[1]) {
					pos = 3;
					break;
				} else
					pos = 2;
				break;
			case 0:
				if (nearHex[0] > nearHex[1] - 2) {
					pos = -3;
					break;
				} else
					pos = -1;
				break;
			case 2:
				if (nearHex[0] > nearHex[1]) {
					pos = -2;
					break;
				} else
					pos = 1;
				break;
			case 7:
				if (nearHex[0] > nearHex[1]) {
					pos = -2;
					break;
				} else
					pos = 3;
				break;
			case 11:
				if (nearHex[0] > nearHex[1]) {
					pos = -3;
					break;
				} else
					pos = 2;
				break;
			case 16:
				if (nearHex[0] > nearHex[1]) {
					pos = -1;
					break;
				} else
					pos = 2;
				break;
			case 18:
				if (nearHex[0] > nearHex[1] + 2) {
					pos = 1;
					break;
				} else
					pos = 3;
				break;
			}

			// catches the case when the road being built does not start or end
			// at a different hex.
			if (nearHex[3] > Hex.RADIUS * 2 - 5) {
				switch (nearHex[0]) {
				case 0:
					pos = -2;
					break;
				case 2:
					pos = -3;
					break;
				case 7:
					pos = -1;
					break;
				case 11:
					pos = 1;
					break;
				case 16:
					pos = 3;
					break;
				case 18:
					pos = 2;
					break;

				}
			}
		}
		return pos;
	}
}
