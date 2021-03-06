<%@ page import="java.util.List,
                 java.util.Map,
                 java.util.Objects,
                 com.joerny.javaadmin.controller.EntityInformation" %>
<%
    final String entityName = (String) request.getAttribute("entityName");
    final Map<String, List<String>> fields = (Map<String, List<String>>) request.getAttribute("fields");
    final Map<String, List<EntityInformation>> childEntities = (Map<String, List<EntityInformation>>) request.getAttribute("childEntities");
%>
<form method="post">
<%
    for (final Map.Entry<String, ?> field : fields.entrySet()) {
        final String fieldName = field.getKey();
        final String fieldIdentifier = entityName + '.' + fieldName;
%>
    <label for="<%= fieldIdentifier %>"><%= fieldName %></label>:<br>
<%
        final Object fieldValue = field.getValue();
        if (fieldValue != null && List.class.isAssignableFrom(fieldValue.getClass())) {
%>
    <div id="<%= fieldIdentifier %>">
<%
            int i = 0;
            for (final String value : (List<String>) fieldValue) {
%>
        <div id="<%= fieldIdentifier %>.<%= i %>">
            <input name="<%= fieldIdentifier %>" type="text" value="<%= value %>"/>
            <a href="#" onclick="deleteField('<%= fieldIdentifier %>', '<%= fieldIdentifier %>.<%= i %>')">Delete</a><br>
        </div>
<%
                i++;
            }
%>
    </div>
    <a href="#" onclick="addField('<%= fieldIdentifier %>', <%= i %>)">Add field</a><br>
<%
        } else if (childEntities.containsKey(fieldName)) {
%>
    <select name="<%= fieldIdentifier %>">
<%
            for (final EntityInformation information : childEntities.get(fieldName)) {
%>
        <option value="<%= information.getId() %>"><%= information.getDescription() %></option>
<%
            }
%>
    </select><br>
<%
        } else {
            final String disabled;
            final String checked;
            if (fieldValue == null) {
                disabled = " disabled";
                checked = "checked";
            } else {
                disabled = "";
                checked = "";
            }
%>
    <input name="<%= fieldIdentifier %>" type="text" value="<%= Objects.toString(fieldValue, "") %>"<%= disabled %>/>
    <label for="<%= fieldIdentifier %>.null_value">NULL</label>:
    <input type="checkbox" name="<%= fieldIdentifier %>.null_value" onclick="modifyInput('<%= fieldIdentifier %>')"<%= checked %>/><br>
<%
        }
    }
%>
    <input type="submit" value="Save Changes"/>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>
