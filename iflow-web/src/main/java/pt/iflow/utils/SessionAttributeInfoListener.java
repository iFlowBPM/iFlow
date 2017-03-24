package pt.iflow.utils;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import pt.iflow.api.utils.Logger;

public class SessionAttributeInfoListener
  implements HttpSessionAttributeListener
{
  public void attributeAdded(HttpSessionBindingEvent arg0)
  {
    Logger.warning("", this, "attributeAdded", "SessionAttributeInfoListener: attribute name: " + arg0.getName() + ", class: " + arg0.getValue().getClass());
  }
  
  public void attributeRemoved(HttpSessionBindingEvent arg0) {}
  
  public void attributeReplaced(HttpSessionBindingEvent arg0) {}
}

