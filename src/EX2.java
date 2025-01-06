import java.util.Set;
import java.util.Stack;

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

    public static boolean isForm(String Text) { //Function to check if the string is a valid formula
        if (Text == null || Text.isEmpty()) {
            return false; //Check if string is empty or null
        }
        if (!Text.startsWith("=")) {
            return false; //A valid formula must start with char "="
        }
        String Form = Text.substring(1); //Remove the leading "=" for further checks
        if (Form.isEmpty()) {
            return false; //If the formula part is empty, it's not a valid formula
        }
        int balance = 0;
        int operatorCount = 0;
        boolean lastWasOperator = false;
        for (int i = 0; i < Form.length(); i++) {
            char current = Form.charAt(i);
            if (current == '(') {
                balance++;
            } else if (current == ')') {
                balance--;
            }
            if (balance < 0) {
                return false; //Parentheses must be balanced
            }
            if (current == '+' || current == '-' || current == '*' || current == '/') {
                if (lastWasOperator) {
                    return false;
                }
                operatorCount++;
                lastWasOperator = true;
            } else { lastWasOperator =false;
            }
        }
        return balance == 0 && operatorCount > 0 && !lastWasOperator;
    }

    private static boolean isText(String s) { //Function to Check if string is a text
        return !(isForm(s) || isNumber(s)); //If the string is neither a number nor a formula, it is text
    }
}