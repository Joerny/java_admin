<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<html>
<head>
    <title>Overview</title>
</head>
<body>
    <h1>Overview</h1>
<%
    final Iterable<String> entityNames = (Iterable<String>) request.getAttribute("entities");

    for (final String name : entityNames) {
%>
    <a href="/java-admin/list/<%= name %>"><%= name %></a><br>
<%
    }
%>
</body>
</html>
