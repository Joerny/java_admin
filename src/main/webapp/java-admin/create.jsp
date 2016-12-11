<!DOCTYPE html>
<%@ page import="java.util.List,
                 java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<%
    final String entityName = (String) request.getAttribute("entityName");
    final Map<String, List<String>> fields = (Map<String, List<String>>) request.getAttribute("fields");
%>
<html>
<head>
    <title>Create <%= entityName %></title>
</head>
<body>
<h1>Edit <%= entityName %></h1>
<form method="post">
    <%
        for (Map.Entry<String, ?> field : fields.entrySet()) {
    %>
    <div id="<%= entityName %>.<%= field.getKey() %>">
        <%= field.getKey() %>:<br>
        <%
            if (List.class.isAssignableFrom(field.getValue().getClass())) {
                int i = 0;
                for (String value : (List<String>) field.getValue()) {
        %>
        <div id="<%= entityName %>.<%= field.getKey() %>.<%= i %>">
            <input name="<%= entityName %>.<%= field.getKey() %>" type="text" value="<%= value %>"/>
            <a href="#" onclick="deleteField('<%= entityName %>.<%= field.getKey() %>', '<%= entityName %>.<%= field.getKey() %>.<%= i %>')">Delete</a><br>
        </div>
        <%
                i++;
            }
        %>
    </div>
    <a href="#" onclick="addField('<%= entityName %>.<%= field.getKey() %>', <%= i %>)">Add field</a><br>
    <%
    } else {
    %>
    <input name="<%= entityName %>.<%= field.getKey() %>" type="text" value="<%= field.getValue() %>"/><br>
    <%
            }
        }
    %>
    <input type="submit" value="Save Changes"/>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>

<script type='text/javascript'>
    function deleteField(fieldName, fieldId) {
        var inputField = document.getElementById(fieldId);

        var container = document.getElementById(fieldName);
        container.removeChild(inputField)
    }

    function addField(fieldName, i){
        var input = document.createElement("input");
        input.type = "text";
        input.name = fieldName;

        var inputName = fieldName + "." + i;

        var deleteLink = document.createElement("a");
        deleteLink.href = "#";
        deleteLink.setAttribute("onclick", "deleteField(\"" + fieldName + "\", \"" + inputName + "\")");
        deleteLink.innerHTML = "Delete";

        var inputContainer = document.createElement("div");
        inputContainer.setAttribute("id", inputName);
        inputContainer.appendChild(input);
        inputContainer.appendChild(document.createTextNode("\n"));
        inputContainer.appendChild(deleteLink);
        inputContainer.appendChild(document.createElement("br"));

        var container = document.getElementById(fieldName);
        container.appendChild(inputContainer);
    }
</script>
</body>
</html>
