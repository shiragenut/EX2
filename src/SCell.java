// Add your documentation below:

public class SCell implements Cell {
    private String line;
    private int type;
    private int order;

    public SCell(String s) {
        setData(s);
    }

    @Override
    public int getOrder() {
        if (type == Ex2Utils.NUMBER || type == Ex2Utils.TEXT) {
            return 0;
        }
        if (type == Ex2Utils.FORM) {
            int maxOrder = 0;
            String[] dependentCells = extractDependentCells(line);
            for (String dependentCell : dependentCells) {
                int cellOrder = getOrderFromSpreadsheet(cell);
                maxOrder = Math.max(maxOrder, cellOrder);
            }
            return 1 + maxOrder;
        }
        return 0;
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
public void setData(String s) {
        // Add your code here
        line = s;
        /////////////////////
    }
    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here

    }
}
