package com.redhat.gss;

import javax.xml.parsers.SAXParserFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import org.jboss.logging.Logger;
import org.jboss.logmanager.LogContext;
import java.io.PrintWriter;
import static com.redhat.gss.LogProtectorServlet.ProtectionState.*;

@WebServlet({"/*"})
public class LogProtectorServlet extends HttpServlet {
  private static Logger log = Logger.getLogger(LogProtectorServlet.class);

  private static final boolean protectUponInit = Boolean.getBoolean("com.redhat.gss.protectUponInit");
  private static Object protectionKey = new Object();
  private static ProtectionState protectionState = UNPROTECTED;

  enum ProtectionState {
    PROTECTED("Unprotect"),
    UNPROTECTED("Protect");
    public String changeAction;
    private ProtectionState(String changeAction) {
      this.changeAction = changeAction;
    }
  }

  @Override
  public void init() {
    if(protectUponInit) {
      synchronized(LogProtectorServlet.class) {
        if(protectionState.equals(UNPROTECTED)) {
          log.warn("Protecting LogManager");
          LogContext logCtx = LogContext.getSystemLogContext();
          logCtx.protect(protectionKey);
        }
      }
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    PrintWriter writer = response.getWriter();
    writer.println("<html><head><title>JBoss LogManager Protector</title></head><body><h1>JBoss LogManager Protector</h1>");
    writer.printf("The system log context is currently <b>%s</b><br/>\n", protectionState);
    writer.printf("<form method=\"post\" action=\"\"><input type=\"submit\" name=\"submit\" value=\"%s\"/></form>\n", 
      protectionState.changeAction);
    writer.println("</body></html>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String action = request.getParameter("submit");
    synchronized(LogProtectorServlet.class) {
      if("Unprotect".equals(action)) {
        if(protectionState.equals(PROTECTED)) {
          LogContext.getSystemLogContext().unprotect(protectionKey);
          protectionState = UNPROTECTED;
        }
      } else if("Protect".equals(action)) {
        if(protectionState.equals(UNPROTECTED)) {
          log.warn("Protecting LogManager");
          LogContext.getSystemLogContext().protect(protectionKey);
          protectionState = PROTECTED;
        }
      }
    }
    doGet(request, response);
  }
}
