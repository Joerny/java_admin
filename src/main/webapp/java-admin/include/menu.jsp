<a href="/java-admin/overview">Overview</a>
<%
    final Iterable<String> entityNames = (Iterable<String>) request.getAttribute("entityNames");
    for (final String name : entityNames) {
%>
| <a href="/java-admin/list/<%= name %>"><%= name %></a>
<%
    }
%>
<br>