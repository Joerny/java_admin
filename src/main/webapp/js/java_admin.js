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
