// Add your documentation below:

public class SCell implements Cell {
    private String line; //Initialize cell content
    private int type; //Initialize cell type (text=1, number=2, form=3)
    private int order; //Initialize cell order of calculation

    public SCell(String s) {
        if (s == null) {
            s = " ";
    }
        setData(s); //Stores the received information
        computeType(); //Calculate the cell type (number,form or text)
    }

    @Override
    public int getOrder() {
        if (type == Ex2Utils.NUMBER || type == Ex2Utils.TEXT) { //If the cell type is number or text, order=0
            return 0;
        }
        return 1; //If the cell type is a form, order=1
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
        this.order = Math.max(0,t);

    }
    private void computeType() {
        if (line.isEmpty()) {
            type = Ex2Utils.TEXT; //If line is null or empty, the type cell is text
            return;
        } //Check the type cell by the function isNumber, isForm and isText
        if (isNumber(line)) {
            type = Ex2Utils.NUMBER;
        } else if (isForm(line)) {
            type = Ex2Utils.FORM;
        } else {
            type = Ex2Utils.TEXT;
        }
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
        String Form = Text.substring(1);//Remove the leading "=" for further checks
        if (Form.matches("[A-Z]+[0-9]+")) {
            return true;
        }
        if (Form.isEmpty()) {
            return false; //If the formula part is empty, it's not a valid formula
        }
        //Initialize variables to track balance, operators and consecutive operators
        int balance = 0;
        int operatorCount = 0;
        boolean lastOperator = false;
        boolean insideParentheses = false;
        for (int i = 0; i < Form.length(); i++) { //Loop through each character in the Form.
            char current = Form.charAt(i);
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
            if (insideParentheses && !Character.isWhitespace(current)) {
                continue;
            }
            //Check for operators '+','-','*','/'
            if (current == '+' || current == '-' || current == '*' || current == '/') {
                if (lastOperator) { //If the previous character was also an operator, it's invalid
                    return false;
                }
                operatorCount++; //Count the operator
                lastOperator = true; //Mark that the current character is an operator
            } else {
                lastOperator = false; // Reset the flag if the current character isn't an operator
            }
        }
        //Ensure all conditions for a valid formula are met - parentheses are balanced,at least one operator is present and the last character isn't an operator
        return (balance == 0 && operatorCount >= 0 && !lastOperator);
    }
}

