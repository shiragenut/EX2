
public class CellEntry  implements Index2D {
    private int x;  //Represents the column number (0-25 for A-Z)
    private int y; //Represent the row number (0-99)

    public CellEntry(String cell) {
        this.x = Ex2Utils.ERR;
        this.y = Ex2Utils.ERR;
        if (cell == null || cell.length() < 2 || cell.length() > 3) {
            return; //If cell is null, empty or has less than 2 or more than 3 chars, mark as invalid
        }
        char firstChar = Character.toUpperCase(cell.charAt(0));
        if (firstChar < 'A' || firstChar > 'Z') { //if letter isn't in the range A-Z, update x and y to ERROR
            return;
        }
        try {
            this.x = firstChar - 'A'; //Convert letter to column number (A=0, B=1,..)
            this.y = Integer.parseInt(cell.substring(1)) -1; //Convert remaining string to row

            if (!isValid()) { //Check if coordinates are within valid range
                this.x = Ex2Utils.ERR;
                this.y = Ex2Utils.ERR;
            }
        } catch(Exception e) { //If any conversion fails, mark as invalid
            this.x = Ex2Utils.ERR;
            this.y = Ex2Utils.ERR;
        }
    }

    public CellEntry(int xx, int yy) {
        if (xx < 0 || xx >= Ex2Utils.WIDTH || yy < 0 || yy >= Ex2Utils.HEIGHT) {
            this.x = Ex2Utils.ERR;
            this.y = Ex2Utils.ERR;
        } else {
            this.x = xx;
            this.y = yy;
        }
    }

    @Override
    public boolean isValid() {
        return x >= 0 && x < Ex2Utils.WIDTH && y >= 0 && y < Ex2Utils.HEIGHT;
    }
    @Override
    public int getX() {
        return x;
    }
    @Override
    public int getY() {
        return y;
    }
    public String toString() {
        if (!isValid()) {
            return "ERR";
        }
        return Ex2Utils.ABC[x]+(y + 1); //Convert coordinates back to cell reference
    }
}
