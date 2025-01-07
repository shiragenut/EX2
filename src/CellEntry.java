
public class CellEntry  implements Index2D {
    private int x;  //Represents the column number (0-25 for A-Z)
    private int y; //Represent the row number (0-99)

    public CellEntry(String cell) {
        if (cell == null || cell.isEmpty() || !Character.isLetter(cell.charAt(0))) {
            this.x = Ex2Utils.ERR;
            this.y = Ex2Utils.ERR;
            return; //If cell is null, empty or doesn't start with a letter, mark as invalid
        }

        try { //Attempt to convert the cell reference to numeric coordinate
            char letter = Character.toUpperCase(cell.charAt(0));
            if (letter < 'A' || letter > 'Z') { //if letter isn't in the range A-Z, update x and y to ERROR
                this.x = Ex2Utils.ERR;
                this.y = Ex2Utils.ERR;
            }
            this.x = letter - 'A'; //Convert letter to column number (A=0, B=1,..)
            this.y = Integer.parseInt(cell.substring(1)); //Convert remaining string to row

            if (!isValid()) { //Check if coordinates are within valid range
                this.x = Ex2Utils.ERR;
                this.y = Ex2Utils.ERR;
            }
        } catch(Exception e) { //If any conversion fails, mark as invalid
            this.x = Ex2Utils.ERR;
            this.y = Ex2Utils.ERR;
        }
    }

    @Override
    public boolean isValid() {
        return  x>= 0 && x<26 && y>=0 && y<100;
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
            return null;
        }
        return Ex2Utils.ABC[x]+y; //Convert coordinates back to cell reference
    }
}
