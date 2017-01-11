<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<%
    final String entityName = (String) request.getAttribute("entityName");
%>
<html>
<head>
    <title>Edit <%= entityName %></title>
</head>
<body>
<jsp:include page="include/menu.jsp" />
    <h1>Edit <%= entityName %></h1>
<jsp:include page="include/entity_form.jsp" />
    <script type='text/javascript' src="/js/java_admin.js" ></script>
</body>
</html>
