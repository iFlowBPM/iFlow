package pt.iknow.floweditor.blocks;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import pt.iflow.api.datatypes.DataTypeInterface;
import pt.iflow.api.xml.FlowMarshaller;
import pt.iflow.api.xml.codegen.flow.XmlAttribute;
import pt.iflow.api.xml.codegen.flow.XmlCatalogVarAttribute;
import pt.iflow.api.xml.codegen.flow.XmlCatalogVars;
import pt.iknow.floweditor.FlowEditorAdapter;

/**
 * Class that contains the jsp text area data [for subFlowFieldData] (only contains single properties)
 *
 * @see JSPFieldData
 */
public  class JSPPopupFormFieldData extends JSPFieldData {
  // id constructor
  public JSPPopupFormFieldData(FlowEditorAdapter adapter, int anID) {
    super(adapter, anID);
  }

  // simple constructor
  public JSPPopupFormFieldData(FlowEditorAdapter adapter) {
    this(adapter, -1);
  }

  // full constructor
  public JSPPopupFormFieldData(FlowEditorAdapter adapter, int anID, int anPosition, String asText, String asVarName, int anCols, int anRows) {
    this(adapter, anID);
    this._nPosition = anPosition;

    // now set all field properties
    this.setProperty(JSPFieldData.nPROP_TEXT,asText);
    this.setProperty(JSPFieldData.nPROP_VAR_NAME,asVarName);
  }

  public JSPPopupFormFieldData(JSPFieldData afdData) {
    super(afdData);
  }

  protected void init() {
    this._nFieldType = JSPFieldTypeEnum.FIELD_TYPE_SUB_FLOW_FORM_FIELD;

    // add text single properties
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_TEXT));
    //this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_VAR_NAME));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_LIST_OF_POPUP_FLOWS));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_DISABLE_COND));

    // INI - DIMENSIONS
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_WIDTH));
    this._alEditSingleProps.add(new Integer(JSPFieldData.nPROP_HEIGHT));
    // FIM - DIMENSIONS

    // add select multiple properties

    // TODO HM Variaveis no fluxo
    this._alEditMultipleProps.add(new Integer(JSPFieldData.nPROP_POPUP_CALLER_VARIABLE));
    this._alEditMultipleProps.add(new Integer(JSPFieldData.nPROP_POPUP_VARIABLES));

    // add static/constant properties
    DataTypeInterface dti = loadDataType(adapter, "pt.iflow.api.datatypes.Text");
    if(dti != null) {
      this.setStaticProperty(JSPFieldData.nPROP_DATA_TYPE, dti.getDescription());
    } else {
      this.setStaticProperty(JSPFieldData.nPROP_DATA_TYPE, "Text");
    }

    // add required properties
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_TEXT));
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_VAR_NAME));
    this._alRequiredProps.add(new Integer(JSPFieldData.nPROP_LIST_OF_POPUP_FLOWS));

    // add prop dependencies

  }

  public static String[][] loadPopupvariables(FlowEditorAdapter flowEditorAdapter, String selectedPopupFlow) {
    pt.iflow.api.xml.codegen.flow.XmlFlow _xmlflow = null;
    String[][] inputFields = new String [0][2];
    try {
      byte[] subflow = flowEditorAdapter.getRepository().getSubFlow(selectedPopupFlow);
      _xmlflow = FlowMarshaller.unmarshal(subflow);
      XmlCatalogVars xmlcv = _xmlflow.getXmlCatalogVars();

      if(xmlcv.getXmlAttributeCount() > 0 && xmlcv.getXmlCatalogVarAttributeCount() == 0) {
        inputFields = new String[xmlcv.getXmlAttributeCount()][2];
        for (int i = 0; i < xmlcv.getXmlAttributeCount(); i++) {
          XmlAttribute attr = xmlcv.getXmlAttribute(i);
          inputFields[i][0] = attr.getName();
          inputFields[i][1] = attr.getDescription();
        }
      } else {
        inputFields = new String[xmlcv.getXmlCatalogVarAttributeCount()][2];
        for (int i = 0; i < xmlcv.getXmlCatalogVarAttributeCount(); i++) {
          XmlCatalogVarAttribute attr = xmlcv.getXmlCatalogVarAttribute(i);
          inputFields[i][0] = attr.getName();
          inputFields[i][1] = attr.getFormat();
        }
      }
    } catch (ValidationException ve) {
    } catch (MarshalException me) {
    }
    return inputFields;
  }
}