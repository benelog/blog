package org.apache.jsp.WEB_002dINF.views;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class home_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<html>\n");
      out.write("<head>\n");
      out.write("\t<meta charset=\"utf-8\" />\n");
      out.write("\t<title>Demo</title>\n");
      out.write("\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n");
      out.write("\t<link  href=\"/css/bootstrap.css\" rel=\"stylesheet\"></link>\n");
      out.write("\t<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery.min.js\"></script>\n");
      out.write("</head>\n");
      out.write("\n");
      out.write("<body>\n");
      out.write("\t<div class=\"container\">\n");
      out.write("\t\t<h1>Test image</h1>\n");
      out.write("\t\t<div>\n");
      out.write("\t\t\t<img id=\"icon\" src=\"http://placehold.it/64x64\"/>\n");
      out.write("\t\t</div>\n");
      out.write("\t\t<button id=\"computer\" onclick=\"requestImage('computer')\">Computer</button>\n");
      out.write("\t\t<button id=\"code\"  onclick=\"requestImage('code')\">Code </button>\n");
      out.write("\t\t<button id=\"cloud\" onclick=\"requestImage('cloud')\">Cloud</button>\n");
      out.write("\t\t<button id=\"phone\" onclick=\"requestImage('phone')\">Phone</button>\n");
      out.write("\n");
      out.write("\t</div>\n");
      out.write("\t<hr/>\n");
      out.write("</body>\n");
      out.write("<script  type=\"text/javascript\">\n");
      out.write("\n");
      out.write("\tfunction requestImage (imageId) {\n");
      out.write("\t\tvar requestUrl = \"/viewImage/\" + imageId + \".json\";\n");
      out.write("\n");
      out.write("\t\tvar icon =  $(\"#icon\");\n");
      out.write("\t\t$.ajax({\n");
      out.write("\t\t url : requestUrl,\n");
      out.write("\t\t type: \"GET\",\n");
      out.write("\t\t dataType :\"json\",\n");
      out.write("\t\t success:function(image){\n");
      out.write("\t\t\t icon.attr('src', image.src);\n");
      out.write("\t\t\t icon.attr('height',image.height);\n");
      out.write("\t\t\t icon.attr('width',image.width);\t\t\t \n");
      out.write("\t\t },\n");
      out.write("\t\t error:function(jqXHR, textStatus, errorThrown){\n");
      out.write("\t\t  alert(\"Error\");\n");
      out.write("\t\t }\n");
      out.write("\t\t});\n");
      out.write("\t}\n");
      out.write("</script>\n");
      out.write("</html>");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
