function deleteField(fieldName, fieldId) {
    var inputField = document.getElementById(fieldId);

    var container = document.getElementById(fieldName);
    container.removeChild(inputField);
}

function addField(fieldName, i) {
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

function modifyInput(fieldName) {
    var checkbox = document.getElementsByName(fieldName + ".null_value").item(0);

    var input = document.getElementsByName(fieldName).item(0);
    input.disabled = checkbox.checked;
    if (input.disabled) {
        input.value = "";
    }
}