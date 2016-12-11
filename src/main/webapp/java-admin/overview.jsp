<!DOCTYPE html>
<%@ page import="javax.persistence.metamodel.EntityType,
                 java.util.Set" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<html>
<head>
    <title>Overview</title>
</head>
<body>
    <h1>Overview</h1>
<%
    final Set<EntityType<?>> entities = (Set<EntityType<?>>) request.getAttribute("entities");

    for (EntityType<?> entity : entities) {
%>
    <a href="/java-admin/list/<%= entity.getName() %>"><%= entity.getName() %></a><br>
<%
    }
%>
</body>
</html>
