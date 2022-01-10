
import java.awt.Component;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public class Table extends JTable{
   boolean isEditable;        

    private class AlignRenderer extends DefaultTableCellRenderer {   
        protected  AlignRenderer(int align) {  
            setHorizontalAlignment(align);  
        }   
    }
    
    public Table(Object[][] data, Object[] columns, boolean isEditable) {
        super(data, columns);
        isEditable=isEditable;        
        setAutoCreateRowSorter(true); 
    }
    
    public Table(Vector data, Vector columns, boolean isEditable) {
        super(data, columns);
        isEditable=isEditable;        
        setAutoCreateRowSorter(true);        
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return isEditable;
    }                

    public void setAlignment(int column, int align) {
        getColumnModel().getColumn(column).setCellRenderer(new AlignRenderer(align));        
    }
    
    public void sizeToFit()  {
        TableModel model = getModel();
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) getColumnModel();
        int margin=10, width;
        for (int vColIndex=0; vColIndex < getColumnCount();  vColIndex++) {
            TableColumn col = colModel.getColumn(vColIndex);            

            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(this, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
            // Get maximum width of column data
            for (int r=0; r<getRowCount(); r++) {
                renderer = getCellRenderer(r, vColIndex);
                comp = renderer.getTableCellRendererComponent(this, getValueAt(r, vColIndex), false, false, r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2*margin;
            // Set the width
            col.setPreferredWidth(width);
        }
    }    
}
