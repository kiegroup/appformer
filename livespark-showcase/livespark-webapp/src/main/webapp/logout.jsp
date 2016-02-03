<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="java.util.Locale" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n-1.0" prefix="i18n" %>
<%
  request.logout();
  javax.servlet.http.HttpSession httpSession = request.getSession(false);
  if (httpSession != null) {
    httpSession.invalidate();
  }
  Locale locale = null;
  try {
    locale = new Locale( request.getParameter( "locale" ) );
  } catch ( Exception e ) {
    locale = request.getLocale();
  }
%>
<i18n:bundle id="bundle" baseName="org.livespark.client.resources.i18n.LoginConstants"
             locale='<%= locale%>'/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <link rel="stylesheet" href="styles/base.css">
  <link rel="stylesheet" href="styles/forms.css">
  <link rel="stylesheet" href="styles/login-screen.css">
  <link rel="shortcut icon" href="favicon.ico" />
  <title><i18n:message key="LoginTitle">LiveSpark</i18n:message></title>
</head>

<body id="login">
<div id="pfly-login-screen">
  <img id="logo" src="images/kie-ide.png" alt="KIE IDE Logo" title="Powered By Drools/jBPM"/>
  <div id="login-wrapper" class="png_bg">
    <div id="login-top">
    </div>

    <div id="login-content" class="png_bg">        
      <form action="<%= request.getContextPath() %>" method="POST">
        <fieldset>
          <h3 id="logout"><i18n:message key="logoutSuccssful">Logout successful</i18n:message></h3>
          <p>
            <% if (request.getParameter("gwt.codesvr") != null) { %>
            <input type="hidden" name="gwt.codesvr" value="<%= org.owasp.encoder.Encode.forHtmlAttribute(request.getParameter("gwt.codesvr")) %>"/>
            <% } %>

            <input class="button" type="submit" value='<i18n:message key="loginAgain">Login again</i18n:message>'/>
          </p>
        </fieldset>
      </form>
    </div>
  </div>
</div>
</body>
</html>
