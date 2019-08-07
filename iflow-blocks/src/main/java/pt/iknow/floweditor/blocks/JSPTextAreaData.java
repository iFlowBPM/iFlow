package pt.iknow.floweditor.blocks;

import pt.iflow.api.datatypes.DataTypeInterface;
import pt.iknow.floweditor.FlowEditorAdapter;


/**
 * Class that contains the jsp text area data (only contains single properties)
 *
 * @see JSPFieldData
 */
public  class JSPTextAreaData extends JSPFieldData {


  // id constructor
  public JSPTextAreaData(FlowEditorAdapter adapter, int anID) {
    super(adapter, anID);
  }

  // simple constructor
  public JSPTextAreaData(FlowEditorAdapter adapter) {
    this(adapter, -1);
  }

  
  // full constructor
  public JSPTextAreaData(FlowEditorAdapter adapter, int anID,
			 int anPosition,
			 String asText,
			 String asVarName,
			 int anCols,
			 int anRows) {
    this(adapter, anID);
    this._nPosition = anPosition;

    // now set all field properties
    this.setProperty(JSPFieldData.nPROP_TEXT,asText);
    this.setProperty(JSPFieldData.nPROP_VAR_NAME,asVarName);
    this.setProperty(JSPFieldData.nPROP_COLS,String.valueOf(anCols));
    this.setProperty(JSPFieldData.nPROP_ROWS,String.valueOf(anRows));
  }


  public JSPTextAreaData(JSPFieldData afdData) {
    super(afdData);
  }




  protected void init() {
    this._nFieldType = JSPFieldTypeEnum.FIELD_TYPE_TEXT_AREA;

    // add text single properties
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_TEXT));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_VAR_NAME));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_COLS));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_ROWS));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_DISABLE_COND));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_OUTPUT_ONLY));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_OBLIGATORY_FIELD));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_VALIDATION_EXPR));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_VALIDATION_MSG));


    // add static/constant properties
    DataTypeInterface dti = loadDataType(adapter, "pt.iflow.api.datatypes.Text");
    if(dti != null)
      this.setStaticProperty(JSPFieldData.nPROP_DATA_TYPE, dti.getDescription());
    else
      this.setStaticProperty(JSPFieldData.nPROP_DATA_TYPE, "Text");
    
    // add required properties
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_TEXT));
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_VAR_NAME));
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_COLS));
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_ROWS));
   
    // set non-string properties types
    this._hmPropTypes.put(new Integer(JSPFieldData.nPROP_COLS),
			  new Integer(JSPFieldData.nPOSITIVE_NUMBER_PERCENT));
    this._hmPropTypes.put(new Integer(JSPFieldData.nPROP_ROWS),
			  new Integer(JSPFieldData.nPOSITIVE_NUMBER));

    // add prop dependencies
    PropDependency pd = new PropDependency(JSPFieldData.nPROP_OUTPUT_ONLY, 
                       PropDependency.nENABLE, 
                       PropDependency.nTRUE);
    PropDependencyItem pdi = null;
    pdi = new PropDependencyItem(JSPFieldData.nPROP_OBLIGATORY_FIELD, PropDependency.nDISABLE);
    pd.addDependency(pdi);
    this._hmPropDependencies.put(new Integer(JSPFieldData.nPROP_OUTPUT_ONLY), pd);
    
    pd = new PropDependency(JSPFieldData.nPROP_OBLIGATORY_FIELD,
            PropDependency.nENABLE,
            PropDependency.nTRUE);
    pdi = new PropDependencyItem(JSPFieldData.nPROP_VALIDATION_EXPR, PropDependency.nENABLE);
    pd.addDependency(pdi);
    pdi = new PropDependencyItem(JSPFieldData.nPROP_VALIDATION_MSG, PropDependency.nENABLE);
    pd.addDependency(pdi);
    this._hmPropDependencies.put(new Integer(JSPFieldData.nPROP_OBLIGATORY_FIELD), pd);
    
  }


}
