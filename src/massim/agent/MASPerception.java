package massim.agent;

import java.util.Collection;

public class MASPerception {

	final int posX;
	final int poxY;

	final int cowsInCoral;

	final int step;

	final Collection<CellPercept> cellPercepts;

	public MASPerception(int posX, int poxY, int cowsInCoral, int step,
						 Collection<CellPercept> cellPercepts) {
		super();
		this.posX = posX;
		this.poxY = poxY;
		this.cowsInCoral = cowsInCoral;
		this.step = step;
		this.cellPercepts = cellPercepts;
	}

	@Override
	public String toString() {
		return "MASPerception [posX=" + posX + ", poxY=" + poxY
				+ ", cowsInCoral=" + cowsInCoral + ", step=" + step
				+ ", cellPercepts=" + cellPercepts + "]";
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return poxY;
	}

	public int getNoOfCowsInCoral() {
		return cowsInCoral;
	}

	public int getStep() {
		return step;
	}

	public Collection<CellPercept> getCellPercepts() {
		return cellPercepts;
	}
}
