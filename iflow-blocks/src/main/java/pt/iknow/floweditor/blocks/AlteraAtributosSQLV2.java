package pt.iknow.floweditor.blocks;

/*****************************************************
 *
 *  Project FLOW EDITOR
 *
 *  class: AlteraAtributos
 *
 *  desc: dialogo para alterar atributos de um bloco
 *
 ****************************************************/

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang.StringUtils;

import pt.iknow.floweditor.Atributo;
import pt.iknow.floweditor.FlowEditorAdapter;

public class AlteraAtributosSQLV2 extends AlteraAtributos {
	
  private static final long serialVersionUID = 7353724963450325257L;
  private JScrollPane jWizardPanel = new JScrollPane();  
  protected static String[] _sqlTemplates = new String[] { "" };
  private JComboBox cbSQLTemplate;
  private static final String sqlTemplate = "sqlTemplate";
  
  public AlteraAtributosSQLV2(FlowEditorAdapter adapter) {
    super(adapter);
  }

  void jbInit() {

    super.jbInit();
    getContentPane().remove(jScrollPane1);
    getContentPane().add(getWizardPanel(), BorderLayout.CENTER);  
  }
  
  public void setDataIn(String title, List<Atributo> atributos){
    atributos = parseAtributos(atributos);
    List<Atributo> myAttrs = new ArrayList<Atributo>();
    for (Atributo a : atributos) {
      myAttrs.add(a.cloneAtributo());
    }
    super.setDataIn(title, myAttrs);
  }

  public String[][] getNewAttributes() {
    String[][] atributos = super.getNewAttributes();
    List<String[]> extra = this.getExtra();

    String[][] newAtributos = new String[atributos.length + extra.size()][];
    System.arraycopy(atributos, 0, newAtributos, 0, atributos.length);
    
    for (int i = 0; i < extra.size(); i++) {
      String[] item = extra.get(i);
      newAtributos[atributos.length + i] = item;
    }
    
    return newAtributos;
  }

  // get MYSQL template  
  private List<String[]> getExtra() {
    List<String[]> retObj = new ArrayList<String[]>();
    String[] cbSQL = new String[2];
    cbSQL[0] = sqlTemplate;
    cbSQL[1] = (String)cbSQLTemplate.getSelectedItem();          
    retObj.add(cbSQL);           
   return retObj;
  }
  
  private List<Atributo> parseAtributos(List<Atributo> atributos) {
   List<Atributo> retObj = new ArrayList<Atributo>();
    for (Atributo at : atributos) {        
        if (StringUtils.equalsIgnoreCase(AlteraAtributos.sDESCRIPTION, at.getNome())) {
          String valor = at.getValor();
          this._jtfDescription.setText(valor);}
        if (StringUtils.equalsIgnoreCase(AlteraAtributos.sRESULT, at.getNome())) {
            String valor = at.getValor();
            this._jtfResult.setText(valor);}
    	
    if (StringUtils.equalsIgnoreCase(sqlTemplate, at.getNome())) {    	     	      	  
    	  if(cbSQLTemplate==null) {
    		  cbSQLTemplate=new JComboBox(getSQLTemplates());    	      
    	  }
          String valor = at.getValor();                           
          boolean found = false;                  
          for (int i = 0; i < cbSQLTemplate.getItemCount(); i++) {
            String item = (String) cbSQLTemplate.getItemAt(i);
                              
            if (StringUtils.equals(valor, item)) {           	           	
            	cbSQLTemplate.setSelectedIndex(i);           	
              found = true;
              break;
            }
          }          
          if (!found) {
        	  cbSQLTemplate.setModel(new DefaultComboBoxModel(getSQLTemplates()));
          }
      } else {
        retObj.add(at);
      }
    }
    return retObj;
  }
  
  private JComponent getWizardPanel() {
    JComponent retObj = null;

    //ComboBox for SQL templates 
      if(cbSQLTemplate==null) {
      cbSQLTemplate=new JComboBox(getSQLTemplates());}      
                     
      JLabel jLabel = new JLabel("SQL Template");
      jLabel.setHorizontalAlignment(JLabel.LEFT);
      jLabel.setLabelFor(cbSQLTemplate);
     
      JPanel panel = new JPanel();
      panel.add(jLabel);
      panel.add(cbSQLTemplate);
                  
//      JPanel mainPanel = new JPanel(new BorderLayout());
//      mainPanel.add(panel, BorderLayout.NORTH);
      
      JPanel jPanel = new JPanel(new BorderLayout());
      jTable1.setVisible(false);
      DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
      renderer.setPreferredSize(new Dimension(0, 0));
      jTable1.getTableHeader().setDefaultRenderer(renderer);
      jPanel.add(jScrollPane1, BorderLayout.NORTH);
      jPanel.add(panel, BorderLayout.CENTER);
      jWizardPanel.getViewport().add(jPanel);
      retObj = this.jWizardPanel;
    return retObj;
  }
  
  private String[] getSQLTemplates() {
	    	    
	    String[] sqlTemplates = AlteraAtributosSQLV2._sqlTemplates;
	    if (adapter.isRepOn()) {
	      String[] repVals = adapter.getDesenho().getSQLTemplatesList();
	      if (null == repVals)
	        repVals = new String[0];
	      sqlTemplates = new String[repVals.length + 1];
	      sqlTemplates[0] = "";
	      for (int kk = 0; kk < repVals.length; kk++) {
	    	  sqlTemplates[kk + 1] = repVals[kk];
	      }	        
	    }
	    AlteraAtributosSQLV2._sqlTemplates = sqlTemplates;
	    return _sqlTemplates;
	  }
}
