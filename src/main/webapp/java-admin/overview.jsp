<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<html>
<head>
    <title>Overview</title>
</head>
<body>
<jsp:include page="include/menu.jsp" />
    <h1>Overview</h1>
<%
    final Iterable<String> entities = (Iterable<String>) request.getAttribute("entities");

    for (final String name : entities) {
%>
    <a href="/java-admin/list/<%= name %>"><%= name %></a><br>
<%
    }
%>
</body>
</html>
