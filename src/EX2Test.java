import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EX2Test {

    @Test
    void isNumber() {
        String[] validNumbers = {"1", "2.005", "0", "-55", "1e3"};
        String[] unValidNumbers = {"3.2.1", " ", "null", "abc"};
        for (String num : validNumbers) {
            assertTrue(EX2.isNumber(num), num + " Should be a valid number");
        }
        for (String num : unValidNumbers) {
            assertFalse(EX2.isNumber(num), num + " Should NOT be a valid number");
        }
    }



    @Test
    void isForm() {
        String[] validForm = {"=1", "=(10.5)", "=1.2", "=A9", "=(1+A2)*(8/5)", "=A9+C4", "=(1+2)*((3))-1"};
        String[] unValidForm = {"1+4","a","AB", "@2", "2+)", "=(3+1*2)-", "=()", "=1+*8", "=(1+2","=+", " "};
        for (String Form : validForm) {
            assertTrue(EX2.isForm(Form), Form + " Should be a valid form");
        }
        for (String Form : unValidForm) {
            assertFalse(EX2.isForm(Form), Form + " Should NOT be a valid form");
        }
    }

    @Test
    void isText() {
        String[] validText = {"SHIRA", "shalom", "HELlOW123", "@#$", "ABC XYZ", "2a", "{2}"};
        String[] unValidText = {"123", "=SHIRA", "+", "=A1"};
        for (String Text : validText) {
            assertTrue(EX2.isText(Text), Text + " Should be a valid text");
        }
        for (String Text : unValidText) {
            assertFalse(EX2.isText(Text), Text + " Should NOT be a valid text");
        }
    }
}