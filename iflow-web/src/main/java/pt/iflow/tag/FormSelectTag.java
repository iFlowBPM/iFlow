package pt.iflow.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringUtils;
import pt.iflow.api.msg.IMessages;

public class FormSelectTag
  extends IknowTag
{
  private static final long serialVersionUID = 1710739291463847027L;
  private String name;
  private String value;
  private String labelkey;
  private String label;
  private String onchange;
  private boolean edit;
  private boolean required;
  private boolean noli;
  private boolean multiple;
  private int size;
  
  public FormSelectTag()
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
    setEdit(false);
    setRequired(false);
    setOnchange(null);
    setMultiple(false);
    setSize(1);
  }
  
  private static boolean isNotClean(String str)
  {
    return (null != str) && (str.trim().length() > 0);
  }
  
  private String genereateOpenSelect()
  {
    IMessages msg = getUserMessages();
    
    StringBuffer sb = new StringBuffer();
    if (!isNoli()) {
      sb.append("<li class=\"form-group\" style=\"height:3rem;\">");
    }
    boolean doLabel = true;
    if (isNoli()) {
      doLabel = (isNotClean(getLabelkey())) || (isNotClean(getLabel()));
    }
    if (doLabel)
    {
      sb.append("<label class=\"control-label col-sm-2\" for=\"");
      sb.append(getName());
      sb.append("\">");
      if (isNotClean(getLabelkey())) {
        sb.append(msg.getString(getLabelkey()));
      } else if (isNotClean(getLabel())) {
        sb.append(getLabel());
      }
      if ((isEdit()) && (isRequired())) {
        sb.append("<em>*</em>");
      }
      sb.append("</label>");
    }
    
    if (isEdit())
    {
    	
      sb.append("<div class = \"control-label col-sm-5\">");
      sb.append("<select name=\"").append(getName()).append("\"  class = \"form-control\" id=\"").append(getName()).append("\"");
      if (StringUtils.isNotEmpty(this.onchange)) {
        sb.append(" onchange=\"").append(this.onchange).append("\"");
      }
      if (this.multiple) {
        sb.append(" MULTIPLE ");
      }
      if (this.size > 1) {
        sb.append(" SIZE=").append(this.size);
      }
      sb.append(">");
    }
    else{
    	sb.append("<div class = \"col-sm-5\"><div class=\"control-label pull-left \">");
    }
    return sb.toString();
  }
  
  public String generateCloseSelect()
  {
    StringBuffer sb = new StringBuffer();
    if (isEdit()) {
      sb.append("</select>");
    }
    else {
    	sb.append("</div>");
    }
    sb.append("</div>");
    if (!isNoli()) {
      sb.append("</li>");
    }
    return sb.toString();
  }
  
  public int doStartTag()
    throws JspException
  {
    String openSelect = genereateOpenSelect();
    try
    {
      this.pageContext.getOut().write(openSelect);
    }
    catch (IOException e)
    {
      throw new JspException(e);
    }
    return 1;
  }
  
  public int doEndTag()
    throws JspException
  {
    String closeSelect = generateCloseSelect();
    try
    {
      this.pageContext.getOut().write(closeSelect);
    }
    catch (IOException e)
    {
      throw new JspException(e);
    }
    return 6;
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
  
  public boolean isRequired()
  {
    return this.required;
  }
  
  public void setRequired(boolean required)
  {
    this.required = required;
  }
  
  public String getOnchange()
  {
    return this.onchange;
  }
  
  public void setOnchange(String onchange)
  {
    this.onchange = onchange;
  }
  
  public boolean isNoli()
  {
    return this.noli;
  }
  
  public void setNoli(boolean noli)
  {
    this.noli = noli;
  }
  
  public boolean isMultiple()
  {
    return this.multiple;
  }
  
  public void setMultiple(boolean multiple)
  {
    this.multiple = multiple;
  }
  
  public int getSize()
  {
    return this.size;
  }
  
  public void setSize(int size)
  {
    this.size = size;
  }
}
