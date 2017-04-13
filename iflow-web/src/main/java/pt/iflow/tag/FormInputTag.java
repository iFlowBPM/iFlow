package pt.iflow.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringUtils;
import pt.iflow.api.msg.IMessages;

public class FormInputTag
  extends IknowTag
{
  private static final long serialVersionUID = 1710739291463847027L;
  private String type;
  private String name;
  private String value;
  private String labelkey;
  private String label;
  private boolean edit;
  private boolean required;
  private String maxlength;
  private String size;
  private String onchange;
  private String onblur;
  private boolean admin;
  
  public FormInputTag()
  {
    init();
  }
  
  public void release()
  {
    super.release();
    init();
  }
  
  private void init()
  {
    setType("text");
    setEdit(false);
    setRequired(false);
    setMaxlength("");
    setOnchange("");
    setOnblur("");
    setAdmin(false);
  }
  
  private static String generateInputField(String asType, String asName, String asValue, String asLabelKey, String asLabel, boolean abEdit, boolean abRequired, String maxlength, String size, String onchange, String onblur, IMessages msg, Boolean abAdmin)
  {
    if ((!abEdit) && ("challenge".equalsIgnoreCase(asType))) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    
    
   
    
    sb.append("<li class=\"form-group\">");
    if(onblur == "true"){
    sb.append("<label class=\"control-label col-sm-4\" for=\"");
    }else{
    	sb.append("<label class=\"control-label col-sm-2\" for=\"");
    }
    sb.append(asName);
    sb.append("\">");
    if ("challenge".equalsIgnoreCase(asType)) {
      sb.append("<img src=\"kaptcha?ts=" + System.currentTimeMillis() + "\" id=\"" + asName + "_kap\" alt=\"");
    }
    if ((null != asLabelKey) && (asLabelKey.trim().length() > 0)) {
      sb.append(msg.getString(asLabelKey));
    } else if ((null != asLabel) && (asLabel.trim().length() > 0)) {
      sb.append(asLabel);
    }
    if ("challenge".equals(asType))
    {
      sb.append("\" />");
      sb.append("<img src=\"images/icon_resync.png\" style=\"top: -34px; position: relative;\" onclick=\"document.getElementById('" + asName + "_kap').src=document.getElementById('" + asName + "_kap').src+'x';\"/>");
      asType = "text";
      asValue = "";
    }
    if ((abEdit) && (abRequired)) {
      sb.append("<em>*</em>");
    }
    sb.append("</label>");
    if ("checkbox".equals(asType)) {
      sb.append("<div class=\"col-sm-1\">");
    } else {
      sb.append("<div class=\"col-sm-5\">");
    }
    if ("logo".equalsIgnoreCase(asType))
    {
      sb.append("<img src=\"Logo?ts=" + System.currentTimeMillis() + "\" alt=\"logo\" />");
      if (abEdit) {
        sb.append("<input type=\"file\" name=\"").append(asName).append("\" id=\"").append(asName).append("\">");
      }
    }
    else if ("checkbox".equals(asType))
    {
      sb.append("<input type=\"checkbox\" class=\"form-control\" style=\"width:25%;\" name=\"").append(asName);
      sb.append("\" id=\"").append(asName).append("\" value=\"true");
      if (StringUtils.equalsIgnoreCase("true", asValue)) {
        sb.append("\" checked=\"checked");
      }
      if (!abEdit) {
        sb.append("\" disabled=\"disabled");
      }
      sb.append("\"");
      if (StringUtils.isNotEmpty(onchange)) {
        sb.append(" onchange=\"").append(onchange).append("\"");
      }
      if (StringUtils.isNotEmpty(onblur)) {
        sb.append(" onblur=\"").append(onblur).append("\"");
      }
      sb.append("/>");
    }
    else if (abEdit)
    {
      sb.append("<input type=\"");
      sb.append(asType);
      sb.append("\" name=\"");
      sb.append(asName);
      sb.append("\" id=\"");
      sb.append(asName);
      sb.append("\" class=\"form-control");
      sb.append("\" value=\"");
      sb.append(asValue);
      if ((("text".equals(asType)) || ("password".equals(asType))) && (!StringUtils.isEmpty(maxlength)))
      {
        sb.append("\" maxlength=\"");
        sb.append(maxlength);
      }
      if ((("text".equals(asType)) || ("password".equals(asType))) && (!StringUtils.isEmpty(size)))
      {
        sb.append("\" size=\"");
        sb.append(size);
      }
      sb.append("\"");
      if (StringUtils.isNotEmpty(onchange)) {
        sb.append(" onchange=\"").append(onchange).append("\"");
      }
      if (StringUtils.isNotEmpty(onblur)) {
        sb.append(" onblur=\"").append(onblur).append("\"");
      }
      sb.append("/>");
    }
    else
    {
      sb.append("<div class=\"control-label pull-left\">");
      sb.append(asValue);
      sb.append("</span>");
    }
    sb.append("</div>");
    sb.append("</li>");
    

    return sb.toString();
  }
  
  private String generateInputField()
  {
    IMessages msg = getUserMessages();
    return generateInputField(getType(), getName(), getValue(), getLabelkey(), getLabel(), isEdit(), isRequired(), getMaxlength(), getSize(), getOnchange(), getOnblur(), msg, isAdmin());
  }
  
  public int doEndTag()
    throws JspException
  {
    String result = generateInputField();
    try
    {
      this.pageContext.getOut().write(result);
    }
    catch (IOException e)
    {
      throw new JspException(e);
    }
    return 6;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public void setValue(String value)
  {
    this.value = value;
  }
  
  public boolean isEdit()
  {
    return this.edit;
  }
  
  public void setEdit(boolean edit)
  {
    this.edit = edit;
  }
  
  public String getLabelkey()
  {
    return this.labelkey;
  }
  
  public void setLabelkey(String labelkey)
  {
    this.labelkey = labelkey;
  }
  
  public String getLabel()
  {
    return this.label;
  }
  
  public void setLabel(String label)
  {
    this.label = label;
  }
  
  public void setAdmin(boolean admin){
	  this.admin = admin;
  }
  
  public boolean isAdmin()
  {
    return this.admin;
  }
  
  public boolean isRequired()
  {
    return this.required;
  }
  
  public void setRequired(boolean required)
  {
    this.required = required;
  }
  
  public String getMaxlength()
  {
    return this.maxlength;
  }
  
  public void setMaxlength(String maxlength)
  {
    this.maxlength = maxlength;
  }
  
  public void setValue(boolean v)
  {
    this.value = String.valueOf(v);
  }
  
  public void setValue(int v)
  {
    this.value = String.valueOf(v);
  }
  
  public void setValue(float v)
  {
    this.value = String.valueOf(v);
  }
  
  public void setValue(long v)
  {
    this.value = String.valueOf(v);
  }
  
  public void setValue(double v)
  {
    this.value = String.valueOf(v);
  }
  
  public String getSize()
  {
    return this.size;
  }
  
  public void setSize(String size)
  {
    this.size = size;
  }
  
  public String getOnchange()
  {
    return this.onchange;
  }
  
  public void setOnchange(String onchange)
  {
    this.onchange = onchange;
  }
  
  public String getOnblur()
  {
    return this.onblur;
  }
  
  public void setOnblur(String onblur)
  {
    this.onblur = onblur;
  }
}
