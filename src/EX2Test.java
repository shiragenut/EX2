import org.junit.jupiter.api.Test;

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
        String[] validForm = {"=1", "=(10.5)", "=1.2", "=A9", "=(1+A2)*(8/5)", "=A9+C4", "=(1+2)*((3))-1"};
        String[] unValidForm = {"1+4","a","AB", "@2", "2+)", "=(3+1*2)-", "=()", "=1+*8", "=(1+2","=+", " "};
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
    public void SCell() {
        SCell numberCell = new SCell("42.5");
        assertEquals(Ex2Utils.NUMBER, numberCell.getType());

        SCell textCell = new SCell("Hello");
        assertEquals(Ex2Utils.TEXT, textCell.getType());

        SCell formulaCell = new SCell("=A1+B2");
        assertEquals(Ex2Utils.FORM, formulaCell.getType());

        SCell nullCell = new SCell(null);
        assertEquals(Ex2Utils.TEXT, nullCell.getType());
        assertEquals(" ", nullCell.getData());
    }

    @Test
    public void testGetOrder() {
        SCell numberCell = new SCell("42");
        assertEquals(0, numberCell.getOrder());

        SCell textCell = new SCell("Hello");
        assertEquals(0, textCell.getOrder());

        SCell formulaCell = new SCell("=A1+B2");
        assertEquals(1, formulaCell.getOrder());
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
    public void testSetOrder() {
        SCell cell = new SCell("Test");
        cell.setOrder(5);
        cell.setOrder(-3);
        assertEquals(0, cell.getOrder());
    }

    @Test
    public void testComplexFormulas() {
        assertTrue(SCell.isForm("=A1+B2*C3"));
        assertTrue(SCell.isForm("=100*(A1-B2)"));
        assertTrue(SCell.isForm("=-A10"));

        assertFalse(SCell.isForm("=++A1"));
        assertFalse(SCell.isForm("=A1++B2"));
    }
    @Test
    void computeForm() {
        assertEquals(5.0, SCell.computeForm("=5"), 0.001);

        assertEquals(3.0, SCell.computeForm("=1+2"), 0.001);
        assertEquals(5.0, SCell.computeForm("=1+2*2"), 0.001);
        assertEquals(3.0, SCell.computeForm("=(1+2)"), 0.001);

        assertEquals(6.0, SCell.computeForm("=(1+2)*2"), 0.001);
        assertEquals(2.5, SCell.computeForm("=5/2"), 0.001);
    }
    @Test
    void depth() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);  // 5X5 sheet

        sheet.set(0, 0, "5");
        sheet.set(0, 1, "hello");
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]);
        assertEquals(0, depths[0][1]);


        sheet.set(1, 0, "=A1");        // B1 = A1
        depths = sheet.depth();
        assertEquals(1, depths[1][0]);

        sheet.set(2, 0, "=B1+A1");     // C1 = B1+A1
        depths = sheet.depth();
        assertEquals(2, depths[2][0]);

        sheet.set(3, 0, "=D2");
        sheet.set(3, 1, "=D1");
        depths = sheet.depth();
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, depths[3][0]);
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, depths[3][1]);
    }

    @Test
    void testEdgeCaseFormulas() {
        assertTrue(SCell.isForm("=A10"));
        assertTrue(SCell.isForm("=(A1)"));
        assertFalse(SCell.isForm("=A100"));
    }

    @Test
    void testComputeFormComplex() {
        assertEquals(10.0, SCell.computeForm("=2*5"), 0.001);
        assertEquals(7.0, SCell.computeForm("=1+2*3"), 0.001);
        assertEquals(9.0, SCell.computeForm("=(1+2)*3"), 0.001);
    }
}
