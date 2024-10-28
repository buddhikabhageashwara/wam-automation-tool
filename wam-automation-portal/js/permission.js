$(document).ready(function () {
    // Function to handle form submission
    $('#permissionForm').on('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        addPermission(); // Call add permission function
    });

    function addPermission() {
        const authToken = sessionStorage.getItem('authToken'); // Retrieve token from sessionStorage

        // Clear previous errors and success messages
        $('#errorContainer').hide().removeClass('alert-danger alert-success').text('');

        $.ajax({
            url: wamAutomationEngineBaseURL + 'permissions',
            type: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            // No data is sent in the body
            success: function () {
                showSuccess('Permission added successfully!');
            },
            error: function (request) {
                handleErrors(request);
            }
        });
    }

    function handleErrors(request) {
        let errorMessage = 'An unexpected error occurred.';
        if (request.responseText) {
            const responseText = JSON.parse(request.responseText);

            switch (responseText.code) {
                case 'WAM-502':
                case 'WAM-503':
                case 'WAM-504':
                    errorMessage = responseText.errors[0].message;
                    break;
            }
        }
        showError(errorMessage);
    }

    function showError(message) {
        $('#errorContainer').show().addClass('alert-danger').text(message);
    }

    function showSuccess(message) {
        $('#errorContainer').show().addClass('alert-success').text(message);
    }
});
