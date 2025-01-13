import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;


    public Ex2Sheet(int x, int y) {
        // Validate input dimensions (columns: 0–26, rows: 0–100)
        if (x < 0 || x > 26 || y < 0 || y > 100) {
            throw new IndexOutOfBoundsException(); // Throw an exception for invalid dimensions
        }
        this.table = new SCell[x][y]; // Initialize the table with the specified dimensions
        for (int i = 0; i < x; i = i + 1) { // Loop through all columns
            for (int j = 0; j < y; j = j + 1) { // Loop through all rows in the current column
                this.table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Assign each cell with an empty value
            }
        }
        eval(); // Evaluate the table to set initial types and values
    }


    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT); // Call the main constructor with default dimensions
    }

    @Override
    public String value(int x, int y) {
        Cell c = get(x, y); // Retrieve the cell at the specified position
        if (c == null) {
            return Ex2Utils.EMPTY_CELL; // If the cell doesn't exist, return an empty string
        }
        return eval(x, y); // Return the computed value of the cell
    }

    @Override
    public Cell get(int x, int y) {
        if (!isIn(x, y)) {
            return null; // Return null if the indices are out of bounds
        }
        return table[x][y]; // Return the cell at the specified location
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null; // Initialize the result as null
        CellEntry ce = new CellEntry(cords); // Convert the string coordinates into a numeric representation
        if (ce.isValid() && isIn(ce.getX(), ce.getY())) {
            ans = get(ce.getX(), ce.getY()); // Retrieve the cell if the location is valid
        }
        return ans; // Return the retrieved cell or null
    }

    @Override
    public int width() {
        return table.length; // Return the number of columns in the table
    }

    @Override
    public int height() {
        return table[0].length; // Return the number of rows in the table
    }

    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s); // Create a new cell with the provided value
        table[x][y] = c; // Assign the cell to the specified position
        eval(); // Recompute the values and types of all cells in the table
    }

    @Override
    public void eval() {
        int[][] dd = depth(); // Calculate the dependency depth for all cells

        // Reset all cell types
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell curr = get(i, j); // Get the current cell
                if (curr != null) {
                    if (dd[i][j] == Ex2Utils.ERR_CYCLE_FORM) {
                        curr.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark cells with cyclic dependencies as errors
                    } else {
                        String data = curr.getData(); // Retrieve the raw data of the cell
                        if (data.startsWith("=")) {
                            if (SCell.isForm(data)) {
                                curr.setType(Ex2Utils.FORM); // Mark as formula if valid
                            } else {
                                curr.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as format error if invalid
                            }
                        } else if (SCell.isNumber(data)) {
                            curr.setType(Ex2Utils.NUMBER); // Mark as a number if it is numeric
                        } else {
                            curr.setType(Ex2Utils.TEXT); // Mark as text otherwise
                        }
                    }
                }
            }
        }

        // Compute values in order of dependencies
        for (int d = 0; d < width() * height(); d++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (dd[i][j] == d) { // Only process cells at the current depth
                        Cell curr = get(i, j); // Get the current cell
                        if (curr != null) {
                            if (curr.getType() == Ex2Utils.ERR_CYCLE_FORM ||
                                    curr.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                                continue; // Skip cells with errors
                            }

                            if (curr.getType() == Ex2Utils.FORM) { // If the cell is a formula
                                try {
                                    Set<String> visitedCells = new HashSet<>(); // Track visited cells to prevent cycles
                                    String form = curr.getData(); // Retrieve the formula
                                    double result = computeForm(form, visitedCells, this); // Compute the formula
                                    if (result == Ex2Utils.ERR) {
                                        curr.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark as error if cyclic
                                    }
                                } catch (Exception e) {
                                    curr.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as error if invalid format
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx >= 0 && yy >= 0; // Ensure the indices are non-negative
        ans = ans && xx < Ex2Utils.WIDTH && yy < Ex2Utils.HEIGHT; // Ensure indices are within the table's dimensions
        return ans; // Return true if the indices are valid, otherwise false
    }


    @Override
    public int[][] depth() {
        int w = width(); // Get the number of columns
        int h = height(); // Get the number of rows
        int[][] ans = new int[w][h]; // Initialize a 2D array to store depth values

        // Initialize the depth array
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Cell cell = get(i, j); // Retrieve the cell at the current position
                if (cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) {
                    ans[i][j] = 0; // Set depth to 0 for text and number cells
                } else {
                    ans[i][j] = -1; // Mark as unprocessed for other types
                }
            }
        }

        int count = 0; // Track the number of processed cells
        int max = w * h; // Maximum number of cells to process
        boolean flagC = true; // Flag to indicate progress in processing

        while (count < max && flagC) {
            flagC = false; // Reset the flag
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    Cell cell = get(x, y); // Get the current cell
                    if (cell.getType() == Ex2Utils.FORM && ans[x][y] == -1) {
                        // Calculate the maximum dependency depth for formula cells
                        int maxDependencyDepth = getMaxDependencyDepth(x, y, ans);
                        if (maxDependencyDepth == Ex2Utils.ERR_CYCLE_FORM) {
                            ans[x][y] = Ex2Utils.ERR_CYCLE_FORM; // Mark as cyclic dependency error
                            cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                            count++;
                            flagC = true;
                        } else if (maxDependencyDepth != -1) {
                            ans[x][y] = 1 + maxDependencyDepth; // Update depth
                            count++;
                            flagC = true;
                        }
                    }
                }
            }
        }

        // Handle unprocessed cells
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (ans[x][y] == -1 && get(x, y).getType() == Ex2Utils.FORM) {
                    ans[x][y] = Ex2Utils.ERR_CYCLE_FORM; // Mark as cyclic dependency error
                    get(x, y).setType(Ex2Utils.ERR_CYCLE_FORM);
                }
            }
        }

        return ans; // Return the depth array
    }

    private int getMaxDependencyDepth(int x, int y, int[][] depths) {
        Cell cell = get(x, y); // Retrieve the current cell
        String form = cell.getData(); // Get the formula
        int maxDepth = -1; // Initialize the maximum depth
        Set<String> visited = new HashSet<>(); // Track visited cells to detect cycles
        String currentCell = x + "," + y;
        visited.add(currentCell); // Add the current cell to the visited set

        for (int i = 0; i < form.length(); i++) {
            if (Character.isUpperCase(form.charAt(i))) {
                StringBuilder cellRef = new StringBuilder(); // Build a cell reference
                cellRef.append(form.charAt(i));

                int j = i + 1;
                while (j < form.length() && Character.isDigit(form.charAt(j))) {
                    cellRef.append(form.charAt(j)); // Add digits to the cell reference
                    j++;
                }

                if (j > i + 1) {
                    String ref = cellRef.toString();
                    CellEntry ce = new CellEntry(ref); // Parse the cell reference
                    if (ce.isValid() && isIn(ce.getX(), ce.getY())) {
                        String dependentCell = ce.getX() + "," + ce.getY();
                        if (visited.contains(dependentCell)) {
                            return Ex2Utils.ERR_CYCLE_FORM; // Return error if cycle is detected
                        }
                        visited.add(dependentCell);

                        if (depths[ce.getX()][ce.getY()] == -1) {
                            return -1; // Dependency depth is not yet calculated
                        }
                        if (depths[ce.getX()][ce.getY()] == Ex2Utils.ERR_CYCLE_FORM) {
                            return Ex2Utils.ERR_CYCLE_FORM; // Return error if cyclic dependency
                        }
                        maxDepth = Math.max(maxDepth, depths[ce.getX()][ce.getY()]); // Update maximum depth
                        visited.remove(dependentCell);
                    }
                }
                i = j - 1; // Skip processed characters
            }
        }
        return maxDepth == -1 ? 0 : maxDepth; // Return the maximum dependency depth
    }


    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String header = reader.readLine(); // Read the first line (header)
            if (header == null || header.isEmpty()) {
                throw new IOException("File is empty or has no valid header."); // Error if the file has no header
            }

            // Clear existing content in the table
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Reset each cell to an empty state
                }
            }

            String line;
            while ((line = reader.readLine()) != null) { // Read the file line by line
                String[] parts = line.split(",", 3); // Split each line into three parts: x, y, and content
                if (parts.length < 3) {
                    continue; // Skip invalid lines
                }

                try {
                    int x = Integer.parseInt(parts[0].trim()); // Parse the column index
                    int y = Integer.parseInt(parts[1].trim()); // Parse the row index
                    String content = parts[2].trim(); // Parse the cell content

                    if (isIn(x, y)) {
                        table[x][y] = new SCell(content); // Update the specified cell with the parsed content
                    }
                } catch (NumberFormatException e) {
                    continue; // Skip lines with invalid numbers
                }
            }

            eval(); // Recalculate the table after loading new data
        }
    }


    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("I2CS ArielIU: Spreadsheet (Ex2) assignment\n"); // Write the header

            for (int i = 0; i < width(); i++) { // Loop through all columns
                for (int j = 0; j < height(); j++) { // Loop through all rows
                    Cell currentCell = table[i][j]; // Get the current cell
                    if (!currentCell.getData().equals(Ex2Utils.EMPTY_CELL)) {
                        writer.write(i + "," + j + "," + currentCell.getData() + "\n"); // Write non-empty cells
                    }
                }
            }
        }
    }


    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y); // Retrieve the specified cell
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL; // Return an empty value if the cell is undefined
        }

        // Handle error cases
        if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
            return Ex2Utils.ERR_CYCLE; // Return error if there's a cyclic dependency
        }
        if (cell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
            return Ex2Utils.ERR_FORM; // Return error if the formula format is invalid
        }

        if (cell.getType() == Ex2Utils.TEXT) {
            return cell.getData(); // Return text data as-is
        } else if (cell.getType() == Ex2Utils.NUMBER) {
            try {
                double value = Double.parseDouble(cell.getData()); // Parse the number
                return formatNumber(value); // Format and return the number
            } catch (NumberFormatException e) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as format error if parsing fails
                return Ex2Utils.ERR_FORM; // Return error
            }
        } else if (cell.getType() == Ex2Utils.FORM) {
            try {
                Set<String> visitedCells = new HashSet<>(); // Track visited cells to prevent cycles
                double result = computeForm(cell.getData(), visitedCells, this); // Compute the formula
                if (result == Ex2Utils.ERR) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM); // Mark as cyclic dependency error
                    return Ex2Utils.ERR_CYCLE; // Return error
                }
                return formatNumber(result); // Format and return the computed value
            } catch (Exception e) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as format error
                return Ex2Utils.ERR_FORM; // Return error
            }
        }

        return Ex2Utils.EMPTY_CELL; // Return empty value for unhandled cases
    }
    private String formatNumber(double value) {
        return Double.toString(value); // Convert the number to a string
    }

    public static double computeForm(String form, Set<String> visitedCells, Sheet sheet) {
        if (!SCell.isForm(form)) {
            try {
                String numStr = form;
                if (numStr.startsWith("+")) {
                    numStr = numStr.substring(1); // Remove the '+' sign if present
                }
                return Double.parseDouble(numStr); // Convert the number string to a double
            } catch (NumberFormatException e) {
                return Ex2Utils.ERR; // Return error if parsing fails
            }
        }
        String expression = form.substring(1).trim(); // Remove '=' and trim the formula

        if (expression.startsWith("(") && expression.endsWith(")")) {
            expression = expression.substring(1, expression.length() - 1).trim(); // Remove surrounding parentheses
        }

        try {
            return Double.parseDouble(expression); // Attempt to parse the expression as a number
        } catch (NumberFormatException e) {
            int parenthesesCount = 0; // Track nesting of parentheses
            int lastAddSubIndex = -1; // Index of the last '+' or '-' operator
            int lastMulDivIndex = -1; // Index of the last '*' or '/' operator
            char lastOperator = ' '; // Last encountered operator

            for (int i = 0; i < expression.length(); i++) {
                char c = expression.charAt(i);
                if (c == '(') {
                    parenthesesCount++; // Increment parentheses depth
                } else if (c == ')') {
                    parenthesesCount--; // Decrement parentheses depth
                } else if (parenthesesCount == 0) {
                    if (c == '+' || c == '-') {
                        lastAddSubIndex = i; // Update last '+' or '-' operator index
                        lastOperator = c; // Set the operator
                    } else if ((c == '*' || c == '/') && lastAddSubIndex == -1) {
                        lastMulDivIndex = i; // Update last '*' or '/' operator index
                        lastOperator = c; // Set the operator
                    }
                }
            }
            int operatorIndex = lastAddSubIndex != -1 ? lastAddSubIndex : lastMulDivIndex; // Use the appropriate operator index

            if (operatorIndex != -1) {
                String leftPart = expression.substring(0, operatorIndex).trim(); // Extract the left part of the expression
                String rightPart = expression.substring(operatorIndex + 1).trim(); // Extract the right part of the expression

                double left = computeForm("=" + leftPart, visitedCells, sheet); // Compute the left part
                double right = computeForm("=" + rightPart, visitedCells, sheet); // Compute the right part

                switch (lastOperator) {
                    case '+': return left + right;
                    case '-': return left - right;
                    case '*': return left * right;
                    case '/': return right == 0 ? Ex2Utils.ERR_FORM_FORMAT : left / right; // Handle division by zero
                }
            }

            CellEntry ce = new CellEntry(expression);
            if (ce.isValid()) {
                int x = ce.getX();
                int y = ce.getY();
                String cellKey = x + "," + y;
                if (visitedCells.contains(cellKey)) {
                    return Ex2Utils.ERR_CYCLE_FORM; // Detect cyclic dependency
                }
                visitedCells.add(cellKey);
                String cellValue = sheet.eval(x, y); // Evaluate the referenced cell
                visitedCells.remove(cellKey);

                try {
                    return Double.parseDouble(cellValue); // Parse the cell's value as a number
                } catch (NumberFormatException ex) {
                    return Ex2Utils.ERR; // Return error if parsing fails
                }
            }

            return Ex2Utils.ERR_FORM_FORMAT; // Return error for invalid formulas
        }
    }
}



