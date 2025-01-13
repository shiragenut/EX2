import java.util.HashSet;
import java.util.Set;

public class SCell implements Cell {
    private String line; //Initialize cell content
    private int type; //Initialize cell type (text=1, number=2, form=3)
    private int order; //Initialize cell order of calculation

    public SCell(String s) {
        if (s == null) {
            s = Ex2Utils.EMPTY_CELL;
        }
        setData(s); //Stores the received information
        computeType(); //Calculate the cell type (number,form or text)
    }

    @Override
    public int getOrder() {
        if (type == Ex2Utils.ERR_CYCLE_FORM) {
            return -1; //If the cell type is a cycle form, order=-1
        }
        if (type == Ex2Utils.NUMBER || type == Ex2Utils.TEXT) {
            return 0; //If the cell type it's number or text, order=0
        }
        return order; //If the cell type is a formula, return the form's order set or compute the order dynamical
    }

    //@Override
    @Override
    public String toString() {
        return getData();//convert cell content to a string
    }

    @Override
    public void setData(String s) {
        line = s; //Update cell content
    }

    @Override
    public String getData() {
        return line; //Get cell content
    }

    @Override
    public int getType() {
//        computeType();
        return type; //Get cell type
    }

    @Override
    public void setType(int t) {
        this.type = t; //Update cell type
    }

    @Override
    public void setOrder(int t) {
        if (t == Ex2Utils.ERR_CYCLE_FORM) {
            order = Ex2Utils.ERR_CYCLE_FORM;
        } else {
            this.order = Math.max(0, t);
        }
    }

    private void computeType() {
        int t_type = Ex2Utils.ERR;
        if (line.startsWith("=")) {
            if (isForm(line)) {
                t_type = Ex2Utils.FORM;
            } else {
                t_type = Ex2Utils.ERR_FORM_FORMAT;
            }
        } else if (isNumber(line)) {
            t_type = Ex2Utils.NUMBER;
        } else if(isText(line)){
            t_type = Ex2Utils.TEXT;
        } else {
            t_type = Ex2Utils.ERR;
        }
        setType(t_type);
    }

    public static boolean isNumber(String Text) { //Function to check if the string can be parsed as a number
        if (Text == null || Text.isEmpty()) {
            return false; //If the string is null or empty, return false
        }
        try {
            Double.parseDouble(Text); //Try to parse the string as a double
            return true; //If parsing succeeds,it is a number
        } catch (NumberFormatException e) {
            return false; //If parsing fails, it isn't a number
        }
    }

    static boolean isText(String s) {//Function to Check if string is a text
        return !(s.startsWith("=") || s.startsWith("+") || isNumber(s)); //If the string is neither a number nor a formula, it is text
    }

    public static boolean isForm(String Text) {
        // Basic validations
        if (Text == null || Text.isEmpty()) { // Check if the input is null or empty
            return false;
        }
        if (!Text.startsWith("=")) { // Check if the input starts with '=' (formula indicator)
            return false;
        }

        String Form = Text.substring(1).trim(); // Remove the '=' and trim whitespace
        if (Form.isEmpty()) { // Check if the formula is empty after removing '='
            return false;
        }
        if (Form.contains(" ")) { // Formulas should not contain spaces
            return false;
        }

        if (isNumber(Form)) { // If the formula is a valid number, return true
            return true;
        }

        int balance = 0; // Tracks the balance of parentheses
        int operatorCount = 0; // Counts the number of operators
        StringBuilder cellReferences = new StringBuilder(); // Stores valid cell references
        boolean lastOperator = false; // Tracks if the last character was an operator
        boolean insideParentheses = false; // Tracks if currently inside parentheses
        boolean isNegative = false; // Tracks if the number is negative
        boolean inNumber = false; // Tracks if currently inside a number
        boolean hasDecimal = false; // Tracks if the current number has a decimal point

        // Iterate through each character in the formula
        for (int i = 0; i < Form.length(); i++) {
            char current = Form.charAt(i);

            // Validate character: must be digit, operator, parentheses, dot, or valid letter
            if (!Character.isDigit(current) &&
                    "+-*/().".indexOf(current) == -1 &&
                    (current < 'A' || current > 'Z')) {
                return false;
            }
            if (current == '.') { // Handle decimal point
                if (!inNumber || hasDecimal) { // Decimal point is invalid outside numbers or if repeated
                    return false;
                }
                hasDecimal = true;
                continue;
            }

            if (Character.isDigit(current)) { // If the character is a digit
                inNumber = true; // Mark as inside a number
            } else if ("+-*/()".indexOf(current) != -1) { // If the character is an operator or parentheses
                inNumber = false; // End the current number
                hasDecimal = false; // Reset decimal flag
            }

            if (Character.isLetter(current)) { // Handle cell references (e.g., A1, B2)
                if (i + 1 >= Form.length()) { // A letter must be followed by a digit
                    return false;
                }
                if (!Character.isDigit(Form.charAt(i + 1))) { // Check if the next character is a digit
                    return false;
                }
            }

            if (current == '(') { // Handle opening parentheses
                balance++; // Increment balance
                insideParentheses = true; // Mark as inside parentheses
            } else if (current == ')') { // Handle closing parentheses
                balance--; // Decrement balance
                if (balance < 0) { // Check for unmatched closing parentheses
                    return false;
                }
                if (insideParentheses && i > 0 && Form.charAt(i - 1) == '(') { // Empty parentheses are invalid
                    return false;
                }
                insideParentheses = false; // Exit parentheses
            }

            if ("+-*/".indexOf(current) != -1) { // Handle operators
                if (lastOperator) { // Two consecutive operators are invalid
                    return false;
                }
                operatorCount++; // Increment operator count
                lastOperator = true; // Mark as the last character being an operator
            } else {
                lastOperator = false; // Reset operator flag
            }

            if (current == '-' && (i == 0 || (i > 0 && (Form.charAt(i - 1) == '(' || "+-*/".indexOf(Form.charAt(i - 1)) != -1)))) {
                // Handle negative numbers (e.g., -5 or (-5))
                isNegative = true;
                continue;
            }

            if (Character.isLetter(current)) { // Parse cell references (e.g., A1, B2)
                int j = i + 1;
                StringBuilder cellRef = new StringBuilder();
                cellRef.append(current);

                if (j < Form.length() && Character.isDigit(Form.charAt(j))) { // Validate digits following the letter
                    while (j < Form.length() && Character.isDigit(Form.charAt(j))) { // Collect the digits
                        cellRef.append(Form.charAt(j++));
                    }
                    if (cellRef.length() >= 2 && cellRef.length() <= 3 && Character.isLetter(cellRef.charAt(0))) { // Validate length and format
                        CellEntry ce = new CellEntry(cellRef.toString());
                        if (ce.isValid()) { // Validate the cell reference
                            cellReferences.append(cellRef.toString()).append(" "); // Append valid cell reference
                        }
                        i = j - 1; // Update the loop index
                    } else {
                        return false; // Invalid cell reference
                    }
                }
            }
        }

        // Formula is valid if parentheses are balanced and does not end with an operator (unless no operators exist)
        return (balance == 0 && (!lastOperator || operatorCount == 0));
    }
}


