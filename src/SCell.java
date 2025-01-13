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
        }
        if (isNumber(line)) {
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

    public static boolean isForm(String Text) { //Function to check if the string is a valid formula
        if (Text == null || Text.isEmpty()) {
            return false; //Check if string is empty or null
        }
        if (!Text.startsWith("=")) {
            return false; //A valid formula must start with char "="
        }
        String Form = Text.substring(1).trim();//Remove the leading "=" for further checks
        if (Form.isEmpty()) {
            return false; //If the formula part is empty, it's not a valid formula
        }
        if (Form.contains(" ")){
            return false;
        }
        //Initialize variables to track balance, operators and consecutive operators
        int balance = 0;
        int operatorCount = 0;
        StringBuilder cellReferences = new StringBuilder();
        boolean lastOperator = false;
        boolean insideParentheses = false;
        boolean isNegative = false;

        for (int i = 0; i < Form.length(); i++) { //Loop through each character in the Form.
            char current = Form.charAt(i);
            if (!Character.isLetterOrDigit(current) && "+-*/()".indexOf(current) == -1) { //Check for invalid characters in the formula
                return false; //Invalid character found
            }
            if (current == '.') {
                if (i == 0 || !Character.isDigit(Form.charAt(i - 1))) {
                    return false;
                }
                int j = i + 1;
                while (j < Form.length() && Character.isDigit(Form.charAt(j))) {
                    j++;
                }
                if (j == i + 1) {
                    return false;
                }
                i = j - 1;
            }

            //Handle parentheses
            if (current == '(') { //Increase balance for '('
                balance++;
                insideParentheses = true;
            } else if (current == ')') {//Decrease balance for ')'
                balance--;
                if (balance < 0) { //If balance negative, it means there is an unmatched closing parenthesis
                    return false; //Parentheses must be balanced
                }
                if (insideParentheses && i > 0 && Form.charAt(i - 1) == '(') {
                    return false; //If it's an empty parentheses "()", return false
                }
                insideParentheses = false;
            }
            //Check for operators '+','-','*','/'
            if (current == '+' || current == '-' || current == '*' || current == '/') {
                if (lastOperator) {
                    return false; //If the previous character was also an operator, it's invalid
                }
                operatorCount++; //Count the operator
                lastOperator = true; //Mark that the current character is an operator
            } else {
                lastOperator = false; // Reset the flag if the current character isn't an operator
            }
            //Handle negative sign
            if (current == '-' && (i == 1 || (i > 0 && (Form.charAt(i - 1) == '(' || "+-*/".indexOf(Form.charAt(i - 1)) != -1)))) {
                isNegative = true;
                continue;
            }
            if (Character.isLetter(current)) { //Check for cell reference (e.g, A1, B2)
                int j = i + 1;
                StringBuilder cellRef = new StringBuilder();
                cellRef.append(current);
                if (j < Form.length() && Character.isDigit(Form.charAt(j))) {
                    while (j < Form.length() && Character.isDigit(Form.charAt(j))) {
                        cellRef.append(Form.charAt(j++));
                    }
                    if (cellRef.length() >= 2 && cellRef.length() <= 3 && Character.isLetter(cellRef.charAt(0))) {
                        //Check if cell reference is valid
                        CellEntry ce = new CellEntry(cellRef.toString());
                        if (ce.isValid()) {
                            cellReferences.append(cellRef.toString()).append(" ");
                        }
                        i = j - 1;
                    } else {
                        return false;
                    }
                }
            }
        }
        //Ensure all conditions for a valid formula are met - parentheses are balanced,at least one operator is present and the last character isn't an operator
        return (balance == 0 && !lastOperator);
    }
}


