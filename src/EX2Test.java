import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class EX2Test {

    @Test
    void isNumber() {
        String[] validNumbers = {"1", "2.005", "0", "-55", "1e3"};
        String[] unValidNumbers = {"3.2.1", " ", "null", "abc"};
        for (String num : validNumbers) {
            assertTrue(SCell.isNumber(num), num + " Should be a valid number");
        }
        for (String num : unValidNumbers) {
            assertFalse(SCell.isNumber(num), num + " Should NOT be a valid number");
        }
    }



    @Test
    void isForm() {
        String[] validForm = {"=1", "=A9", "=(1+A2)*(8/5)", "=A9+C4", "=(1+2)*((3))-1", "=-5"};
        String[] unValidForm = {"1+4","AB", "@2", "2+)","=2 + 2", "=(3+1*2)-", "=()", "=1+*8", "=(1+2","=+", " "};
        for (String Form : validForm) {
            assertTrue(SCell.isForm(Form), Form + " Should be a valid form");
        }
        for (String Form : unValidForm) {
            assertFalse(SCell.isForm(Form), Form + " Should NOT be a valid form");
        }
    }

    @Test
    void isText() {
        String[] validText = {"SHIRA", "shalom", "HELlOW123", "@#$", "ABC XYZ", "2a", "{2}"};
        String[] unValidText = {"123", "=SHIRA", "+", "=A1"};
        for (String Text : validText) {
            assertTrue(SCell.isText(Text), Text + " Should be a valid text");
        }
        for (String Text : unValidText) {
            assertFalse(SCell.isText(Text), Text + " Should NOT be a valid text");
        }
    }

    @Test
    public void testSetAndGetData() {
        SCell cell = new SCell("Initial");
        assertEquals("Initial", cell.getData());

        cell.setData("Updated");
        assertEquals("Updated", cell.getData());
    }

    @Test
    public void testSetAndGetType() {
        SCell cell = new SCell("Test");
        cell.setType(Ex2Utils.NUMBER);
        assertEquals(Ex2Utils.NUMBER, cell.getType());
    }

    @Test
    public void testToString() {
        SCell cell = new SCell("Hello");
        assertEquals("Hello", cell.toString());
    }


    @Test
    public void testComplexFormulas() {
        assertTrue(SCell.isForm("=A1+B2*C3"));
        assertTrue(SCell.isForm("=100*(A1-B2)"));
        assertTrue(SCell.isForm("=-A10"));

        assertFalse(SCell.isForm("=++A1"));
        assertFalse(SCell.isForm("=A1++B2"));
        assertFalse(SCell.isForm("=8++7"));
    }

    @Test
    void testEvalEmptyCell() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "");  // תא ריק
        String result = sheet.eval(0, 0);
        assertEquals(Ex2Utils.EMPTY_CELL, result); // צריך להחזיר EMPTY_CELL
    }

    @Test
    void testEvalTextCell() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "Hello");  // תא עם טקסט
        String result = sheet.eval(0, 0);
        assertEquals("Hello", result); // חייב להחזיר את המילה "Hello"
    }

    @Test
    void testEvalNumberCell() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "10");  // תא עם מספר
        String result = sheet.eval(0, 0);
        assertEquals("10.0", result);  // מספר אמור להיות מומר ל-10.0
    }

    @Test
    void testEvalCycleForm() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B1");  // A1 תלוי ב-B1
        sheet.set(1, 0, "=A1");  // B1 תלוי ב-A1
        String result = sheet.eval(0, 0);  // חישוב של A1
        assertEquals(Ex2Utils.ERR_CYCLE, result);  // תלות מעגלית
    }

    @Test
    void testComputeFormSimpleExpression() {
        Sheet sheet = new Ex2Sheet();
        String form = "=3+2";  // ביטוי פשוט
        double result = Ex2Sheet.computeForm(form, new HashSet<>(), sheet);
        assertEquals(5.0, result);  // התוצאה הצפויה היא 5.0
    }

    @Test
    void testComputeFormComplexExpression() {
        Sheet sheet = new Ex2Sheet();
        String form = "=(3+2)*2";  // ביטוי מורכב עם סוגריים
        double result = Ex2Sheet.computeForm(form, new HashSet<>(), sheet);
        assertEquals(10.0, result);  // התוצאה הצפויה היא 10.0
    }

    @Test
    void testComputeFormInvalidExpression() {
        Sheet sheet = new Ex2Sheet();
        String form = "=3+a";  // ביטוי לא תקני (אין משתנה a)
        double result = Ex2Sheet.computeForm(form, new HashSet<>(), sheet);
        assertEquals(Ex2Utils.ERR, result);  // שגיאה בהבנת הביטוי
    }
    @Test
    void testDepthBasicFormula() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1");  // A1 = 1
        int[][] depths = sheet.depth();
        assertEquals(1, depths[0][0]);  // נוסחה פשוטה, עומק 1
    }

    @Test
    void testDepthReferenceAnotherCell() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1");  // A1 = 1
        sheet.set(1, 0, "=A1"); // B1 = A1
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // A1 עומק 0
        assertEquals(1, depths[1][0]); // B1 עומק 1
    }

    @Test
    void testDepthMultipleDependencies() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1");  // A1 = 1
        sheet.set(1, 0, "=1");  // B1 = 1
        sheet.set(2, 0, "=A1+B1");  // C1 = A1 + B1
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // A1 עומק 0
        assertEquals(0, depths[1][0]); // B1 עומק 0
        assertEquals(1, depths[2][0]); // C1 עומק 1
    }

    @Test
    void testDepthCyclicReference() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B1");  // A1 תלוי ב-B1
        sheet.set(1, 0, "=A1");  // B1 תלוי ב-A1
        int[][] depths = sheet.depth();
        assertEquals(-1, depths[0][0]); // תלות מעגלית
        assertEquals(-1, depths[1][0]); // תלות מעגלית
    }

    @Test
    void testDepthEmptyCells() {
        Sheet sheet = new Ex2Sheet();
          // יצירת טבלה חדשה וריקה
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]); // תא ריק, עומק 0
    }


    @Test
    void testEdgeCaseFormulas() {
        assertTrue(SCell.isForm("=A10"));
        assertTrue(SCell.isForm("=(A1)"));
        assertFalse(SCell.isForm("=A100"));
    }
}
