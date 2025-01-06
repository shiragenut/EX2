public class EX2 {
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