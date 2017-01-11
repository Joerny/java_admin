<!DOCTYPE html>
<%@ page import="com.joerny.javaadmin.controller.JavaAdminListCommand,
                 java.util.List,
                 java.util.Map" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    final JavaAdminListCommand command = (JavaAdminListCommand) request.getAttribute("command");
%>
<html>
<head>
    <title>List of <%= command.getEntityName() %>s</title>
</head>
<body>
<jsp:include page="include/menu.jsp" />
    <h1>List of <%= command.getEntityName() %>s</h1>
    <a href="/java-admin/create/<%= command.getEntityName() %>">Create</a><br>
    <table>
        <thead>
            <tr>
<%
    for (final String attributeName : command.getAttributeNames()) {
%>
                <th><%= attributeName %></th>
<%
    }
%>
            </tr>
        </thead>
        <tbody>
<%
    for (final Map.Entry<String, Map<String, List<String>>> entity : command.getEntities().entrySet()) {
%>
            <tr>
<%
        for (final String attributeName : command.getAttributeNames()) {
            final List<String> values = entity.getValue().get(attributeName);
%>
                <td>
<%
            for (final String value : values) {
%>
                    <%= value %><br />

<%
            }
%>
                </td>
<%
        }
%>
                <td>
                    <a href="/java-admin/edit/<%= command.getEntityName() %>/<%= entity.getKey() %>">Edit</a><br>
                    <a href="/java-admin/delete/<%= command.getEntityName() %>/<%= entity.getKey() %>" onclick="return confirm('Are you sure to delete that item?')">Delete</a>
                </td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
</body>
</html>
