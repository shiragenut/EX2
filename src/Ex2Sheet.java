import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;


    public Ex2Sheet(int x, int y) {
        if (x < 0 || x > 26 || y < 0 || y > 100) {
            throw new IndexOutOfBoundsException();
        }
        this.table = new SCell[x][y];
        for (int i = 0; i < x; i = i + 1) {
            for (int j = 0; j < y; j = j + 1) {
                this.table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
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
        if (!isIn(x, y)) { //Check if indexes are valid
            return null;
        }
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
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++){
                Cell curr = get(i, j);
                if (curr != null) {
                    String data = curr.getData();
                    if (data.startsWith("=")){
                        curr.setType(Ex2Utils.FORM);
                    } else if (SCell.isNumber(data)){
                        curr.setType(Ex2Utils.NUMBER);
                    } else {
                        curr.setType(Ex2Utils.TEXT);
                    }
                }
            }
        }
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
        int[][] ans = new int[w][h];

        // אתחול כל המטריצה לערך -1
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = -1;
            }
        }

        // קביעת העומק עבור תאים המכילים טקסט או מספר
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell cell = get(x, y);
                if (cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) {
                    ans[x][y] = 0;  // העומק עבור טקסט או מספר הוא 0
                }
            }
        }

        int count = 0;
        int max = w * h;
        boolean flagC = true;

        // עדכון העומק עבור נוסחאות
        while (count < max && flagC) {
            flagC = false;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    Cell cell = get(x, y);
                    if (cell.getType() == Ex2Utils.FORM && ans[x][y] == -1) {
                        int maxDependencyDepth = getMaxDependencyDepth(x, y, ans);
                        if (maxDependencyDepth == Ex2Utils.ERR_CYCLE_FORM) {
                            ans[x][y] = Ex2Utils.ERR_CYCLE_FORM;
                            cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                            count++;
                            flagC = true;
                        } else if (maxDependencyDepth != -1) {
                            ans[x][y] = 1 + maxDependencyDepth;
                            count++;
                            flagC = true;
                        }
                    }
                }
            }
        }

        // אם אחרי הלולאה עוד ישנם תאים שלא עודכנו, קבע אותם כשגיאה מעגלית
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (ans[x][y] == -1 && get(x, y).getType() == Ex2Utils.FORM) {
                    ans[x][y] = Ex2Utils.ERR_CYCLE_FORM;
                    get(x, y).setType(Ex2Utils.ERR_CYCLE_FORM);
                }
            }
        }

        return ans;
    }
    private int getMaxDependencyDepth(int x, int y, int[][] depths) {
        Cell cell = get(x, y);
        String form = cell.getData();
        int maxDepth = -1;
        Set<String> visited = new HashSet<>();  // מעקב אחר תאים שביקרנו בהם

        for (int i = 0; i < form.length(); i++) {
            if (Character.isUpperCase(form.charAt(i))) {  // אם מדובר בהפניה לתא (למשל A1)
                StringBuilder cellRef = new StringBuilder();
                cellRef.append(form.charAt(i));

                int j = i + 1;
                // המשך חיפוש עד למציאת המספרים בהפניה (למשל 1 ב-A1)
                while (j < form.length() && Character.isDigit(form.charAt(j))) {
                    cellRef.append(form.charAt(j));
                    j++;
                }

                if (j > i + 1) {
                    String ref = cellRef.toString();

                    // אם כבר ביקרנו בתא זה, מדובר בלולאת תלות
                    if (visited.contains(ref)) {
                        return Ex2Utils.ERR_CYCLE_FORM;  // במקרה של לולאת תלות
                    }

                    visited.add(ref);

                    // בדיקה אם ההפניה לתא חוקית
                    CellEntry ce = new CellEntry(ref);
                    if (ce.isValid() && isIn(ce.getX(), ce.getY())) {
                        // אם התא תלוי בתא אחר, נשאב את העומק שלו
                        if (depths[ce.getX()][ce.getY()] == -1) {
                            return -1;  // אם העומק עדיין לא הוקצה, לא ניתן להחזיר תשובה כעת
                        }
                        if (depths[ce.getX()][ce.getY()] == Ex2Utils.ERR_CYCLE_FORM) {
                            return Ex2Utils.ERR_CYCLE_FORM;  // אם יש תלות מעגלית, נחזור עם שגיאה
                        }
                        maxDepth = Math.max(maxDepth, depths[ce.getX()][ce.getY()]);
                    }
                }
                i = j - 1;  // לעדכן את מיקום ה-i לאור ההפניה המלאה
            }
        }

        // אם לא נמצאו תלויות, החזר 0 (למשל אם זו נוסחה פשוטה ללא הפניות)
        return maxDepth == -1 ? 0 : maxDepth;
    }




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
        if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
            return Ex2Utils.ERR_CYCLE;
        }
        if (cell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
            return Ex2Utils.ERR_FORM;
        }
        if (cell.getType() == Ex2Utils.TEXT) { //Check if the input is text
            return cell.getData(); //If it does, get the cell's data
        } else if (cell.getType() == Ex2Utils.NUMBER) { //Check if the input is text
            try {
                double value = Double.parseDouble(cell.getData()); //Try to convert to a number
                return String.format("%.1f", value); //Try to return the number by the format
            } catch (NumberFormatException e) { //If parsing doesn't succeed
                cell.setType(Ex2Utils.ERR_FORM_FORMAT); //Set type to not in the form format
                return Ex2Utils.ERR_FORM;
            }
        }
        if (cell.getType() == Ex2Utils.FORM) { //In case the cell contains a form
            try {
                Set<String> visitedCells = new HashSet<>();
                double result = computeForm(cell.getData(), visitedCells, this);
                if (result == Ex2Utils.ERR) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    return Ex2Utils.ERR_CYCLE;
                }
                return String.format("%.1f", result); //try to compute the form value
            } catch (Exception e) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM; //Return error if computation fails
            }
        }
        return Ex2Utils.EMPTY_CELL;
    }
    public static double computeForm(String form, Set<String> visitedCells, Sheet sheet) {
        if (!SCell.isForm(form)) { //Verify form is valid formula
            try {
                return Double.parseDouble(form); //If it's not a form, try to convert to a number
            } catch (NumberFormatException e) {
                return Ex2Utils.ERR;
            }
        }
        String expression = form.substring(1).trim(); //Remove the '=' from the form
        if (expression.startsWith("(") && expression.endsWith(")")) { //Remove parentheses around the form if there are
            expression = expression.substring(1, expression.length() - 1).trim();
        }

        try {
            return Double.parseDouble(expression); //Try to convert the form to a number
        } catch (NumberFormatException e) {
            int parenthesesCount = 0; //Track nested parentheses depth
            int lastAddSubIndex = -1;
            int lastMulDivIndex = -1;
            char lastOperator = ' ';

            for (int i = 0; i < expression.length(); i++) { //Scan through the expression to search the lastOperator
                char c = expression.charAt(i);
                if (c == '(') { //Track opening parentheses
                    parenthesesCount++;
                } else if (c == ')') { //Track closing parentheses
                    parenthesesCount--;
                } else if (parenthesesCount == 0) { //If not within parentheses
                    if (c == '+' || c == '-') { //Check for + or - operators first
                        lastAddSubIndex = i;
                        lastOperator = c;
                    } else if ((c == '*' || c == '/') && lastAddSubIndex == -1) { //Check for * or / operators
                        lastMulDivIndex = i;
                        lastOperator = c;
                    }
                }
            }
            int operatorIndex = lastAddSubIndex != -1 ? lastAddSubIndex : lastMulDivIndex;
            if (operatorIndex != -1) {
                //Split expression to a leftPart (before the operator) and rightPart (after the operator
                String leftPart = expression.substring(0, operatorIndex).trim();
                String rightPart = expression.substring(operatorIndex + 1).trim();
                //Compute both parts
                double left = computeForm("=" + leftPart, visitedCells, sheet); //Compute left part
                double right = computeForm("=" + rightPart, visitedCells, sheet); //Compute rightPart

                switch (lastOperator) { //Fit the operator to tha fit action
                    case '+':
                        return left + right;
                    case '-':
                        return left - right;
                    case '*':
                        return left * right;
                    case '/':
                        if (right == 0) return Ex2Utils.ERR_FORM_FORMAT;
                        return left / right;
                }
            }
            CellEntry ce = new CellEntry(expression);
                if (ce.isValid()) {
                    int x = ce.getX();
                    int y = ce.getY();
                    String cellKey = x + "," + y;
                    if (visitedCells.contains(cellKey)) {
                        return Ex2Utils.ERR_CYCLE_FORM; //Recognize a dependencies cycle
                    }
                    visitedCells.add(cellKey);
                    String cellValue = sheet.eval(x, y);
                    visitedCells.remove(cellKey);

                    try {
                        return Double.parseDouble(cellValue);
                    } catch (NumberFormatException ex){
                        return Ex2Utils.ERR;
                    }
                }
                return Ex2Utils.ERR_FORM_FORMAT;
            }
        }
    }


