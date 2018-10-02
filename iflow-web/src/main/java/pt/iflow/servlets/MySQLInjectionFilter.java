package pt.iflow.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.MySQLCodec;

public class MySQLInjectionFilter implements Filter {
	
	private List<String> filterException;
	private FilterConfig filterConfig;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		if ( filterException.contains(requestURI)){
			chain.doFilter(request, response);
			return;
		}
		chain.doFilter(new MySQLInjectionFilteredRequest(request), response);
	}

	public void destroy() {
	}

	public void init(FilterConfig fc) throws ServletException {
		Enumeration<?> parameterNames = fc.getInitParameterNames();
		filterException = new ArrayList<String>();
		
		while(parameterNames.hasMoreElements()){
			String aux = parameterNames.nextElement().toString();
			if (StringUtils.startsWith(aux, "pt.iflow.filter_sql_exception"))
				filterException.add(fc.getInitParameter(aux));
		}
		
		this.filterConfig = fc;
	}

	class MySQLInjectionFilteredRequest extends GenericSanitizedRequestWrapper {

		public MySQLInjectionFilteredRequest(ServletRequest request) {
			super((HttpServletRequest) request);
		}

		public String sanitize(String input) {
			return ESAPI.encoder().encodeForSQL(
					new MySQLCodec(MySQLCodec.Mode.ANSI), input);
		}
	}

}
