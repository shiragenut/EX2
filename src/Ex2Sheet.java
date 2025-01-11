import java.io.*;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;


    public Ex2Sheet(int x, int y) {
        if (x > 26 || y > 100){
            throw new IndexOutOfBoundsException();
        }
        this.table = new SCell[x][y];
        for (int i = 0; i < x; i = i + 1) {
            for (int j = 0; j < y; j = j + 1) {
                this.table[i][j] = new SCell("");
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL; //Get an empty cell
        Cell c = get(x, y); //Get the cell at the desired location
        if (c != null) { //Check if the cell is available
            ans = c.toString(); //Return its value as a string
        }
        return ans; //If the cell isn't available, return empty cell
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y]; //Return the cell at the desired location at the sheet
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        CellEntry ce = new CellEntry(cords); //Convert cell's location to numbers
        if (ce.isValid() && isIn(ce.getX(), ce.getY())) { //Check if location is valid
            ans = get(ce.getX(), ce.getY()); //Return the cell at the desired location
        }
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s); //Make a new cell with the received value
        table[x][y] = c; //Put the cell at the desired location
        eval(); //Update all sheet's celle's value
    }

    @Override
    public void eval() {
        int[][] dd = depth(); //Get all cell's depth's array
        //Calculates the values according to the dependency order from low to high
        for (int d = 0; d < width() * height(); d++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (dd[i][j] == d) { //When a cell at the current depth were found
                        Cell curr = get(i, j);
                        if (curr != null) { //If the cell is available
                            String result = eval(i, j);
                            if (curr.getType() == Ex2Utils.NUMBER || curr.getType() == Ex2Utils.FORM) {
                                curr.setData(result); //compute cell's value
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx >= 0 && yy >= 0; //Check location isn't negative
        ans = ans && xx < Ex2Utils.WIDTH && yy < Ex2Utils.HEIGHT; // location is within the table's boundaries
        return ans;
    }

    @Override
    public int[][] depth() {
        int w = width();
        int h = height();
        int[][] ans = new int[width()][height()];

        for (int i = 0; i < w; i++) { //Iterate through all cells in the sheet
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1; //Set depth to -1
            }
        }
        for (int x = 0; x < w; x++) { //Loop over the dependency cycle
            for (int y = 0; y < h; y++) {
                Cell cell = get(x, y);
                if (cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) { //Check if the cell can be computed now
                    ans[x][y] = 0;
                }
            }
        }

        int maxDepth = 0;
        boolean changed;
        do {
            changed = false;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (ans[x][y] == -1) {
                        int depth = computeCellDepth(x,y,ans);
                        if (depth != -1) {
                            ans[x][y] = depth;
                            maxDepth = Math.max(maxDepth, depth);
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
        return ans;
    }

    private int computeCellDepth(int x, int y, int[][] depths) {
        Cell cell = get(x, y);
        if (cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) {
            return 0; //For text and number cells, there is no depth
        }
        if (cell.getType() == Ex2Utils.FORM) { //For form cells, need to check dependencies
            String form = cell.getData();
            int maxDependencyDepth = 0;
            for (int i = 1; i < form.length(); i++) { //Go through the form string
                if (Character.isUpperCase(form.charAt(i))) { //Found potential cell reference
                    int j = i + 1;
                    while (j < form.length() && Character.isDigit(form.charAt(j))) { //Find the complete reference
                        j++;
                    }
                    if (j > i + 1) { //Found a complete cell reference
                        String ref = form.substring(i, j); //Convert referenced cell to a string
                        CellEntry ce = new CellEntry(ref); //Convert the string to a cell's location
                        if (ce.isValid() && isIn(ce.getX(), ce.getY())) {//Check if reference is valid and have been computed
                            if (depths[ce.getX()][ce.getY()] == -1) {
                                return -1; //Found dependency that hasn't been computed yet
                            }
                            maxDependencyDepth = Math.max(maxDependencyDepth, depths[ce.getX()][ce.getY()]);
                        }
                    }
                    i = j-1; //Skip to the end of current reference
                }
            }
            return maxDependencyDepth + 1; //All dependencies have been computed, check if current cell needs computing
        }
        return -1;
    }

    @Override
    public void load(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try {
            reader.readLine();
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    table[i][j] = new SCell(" ");
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",", 3);
                    if (parts.length < 3) continue;
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    String content = parts[2].trim();
                    if (isIn(x, y)) {
                        table[x][y] = new SCell(content);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            eval();
        } finally {
            reader.close();
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        try{
            writer.write("I2CS ArielIU: Spreadsheet (Ex2) assigment\n");
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    Cell currentCell = get(i, j);
                    String cellContent = currentCell.getData();
                    if (!cellContent.equals(" ")) {
                        writer.write(i + "," + j + "," + cellContent +"\n");
                    }
                }
            }
        } finally {
            writer.close();
        }
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL;
        }
        if (cell.getType() == Ex2Utils.TEXT) { //Check if the input is number or text
            return cell.getData(); //If it does, get the cell's value
        } else if (cell.getType() == Ex2Utils.NUMBER) {
            try {
                double value = Double.parseDouble(cell.getData());
                return String.format("%.1f", value);
            } catch (NumberFormatException e) {
            return Ex2Utils.ERR_FORM;
            }
        } else if (cell.getType() == Ex2Utils.FORM) { //In case the cell contains a form
            try {
                double result = SCell.computeForm(cell.getData());
                if (result == Ex2Utils.ERR_FORM_FORMAT) {
                    cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    return Ex2Utils.ERR_FORM;
                }
                return String.format("%.1f", result); //try to compute the form value
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM; //Return error if computation fails
                }
            }
        return Ex2Utils.EMPTY_CELL;
        }
    }

