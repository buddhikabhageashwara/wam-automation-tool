(function() {
    'use strict';

    // Clear authToken from session storage when the login page is loaded
    sessionStorage.removeItem("authToken");

    window.addEventListener('load', function() {
        // Get the login form element
        var form = document.getElementById('loginForm');

        // On form submission
        form.addEventListener('submit', function(event) {
            // Prevent default behavior
            event.preventDefault();
            event.stopPropagation();
            // Check if form is valid
            if (form.checkValidity()) {
                // Call login function if form is valid
                loginUser();
            } else {
                // Add the Bootstrap 'was-validated' class to show validation feedback
                form.classList.add('was-validated');
            }
        }, false);

        // Add a keydown event listener to trigger login on pressing "Enter"
        form.addEventListener('keydown', function(event) {
            // Check if the "Enter" key (keyCode 13) is pressed
            if (event.key === 'Enter' || event.keyCode === 13) {
                // Prevent default form submission behavior
                event.preventDefault();
                // Trigger form submission
                form.requestSubmit(); // Submits the form, triggering the 'submit' event handler
            }
        });
    }, false);
})();

function loginUser() {
    var email = document.getElementById('emailField').value;
    var password = document.getElementById('passwordField').value;
    var userLoginRequest = {
        "userEmail": email,
        "userPassword": password
    };

    $.ajax({
        url: wamAutomationEngineBaseURL + "users/login",
        type: 'POST',
        dataType: 'json',
        data: JSON.stringify(userLoginRequest),
        contentType: "application/json",
        accept: "application/json",
        success: function(response) {
            sessionStorage.setItem("authToken", response.token);
            window.location.href = "dashboard.html";
        },
        error: function(request, status, error) {
            var responseText = JSON.parse(request.responseText);
            if (responseText.code === "WAM-501") {
                $(".errorLabel").show().text(responseText.errors[0].message);
            } else if (responseText.code === "WAM-505") {
                $(".errorLabel").show().text(responseText.errors[0].message);
            } else {
                $(".errorLabel").show().text("An unexpected error occurred.");
            }
        }
    });
}
