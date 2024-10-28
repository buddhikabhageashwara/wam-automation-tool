$(document).ready(function () {
    // Function to handle form submission
    $('#userTypeForm').on('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        addUserType(); // Call add user type function
    });

    // Trigger addUserType function on Enter key press
    $('#userTypeForm').on('keydown', function (event) {
        if (event.key === 'Enter' || event.keyCode === 13) {
            event.preventDefault();
            $('#addUserTypeBtn').click(); // Trigger button click
        }
    });

    function addUserType() {
        const authToken = sessionStorage.getItem('authToken');
        const userTypeName = $('#userTypeName').val().trim();
        const description = $('#description').val().trim();

        // Clear previous errors and success messages
        $('#errorContainer').hide().removeClass('alert-danger alert-success').text('');

        if (!userTypeName) {
            showError('User type name is required and cannot be left blank.');
            return;
        }

        const userTypeData = {
            userTypeName: userTypeName,
            description: description
        };

        $.ajax({
            url: wamAutomationEngineBaseURL + 'usertypes',
            type: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            data: JSON.stringify(userTypeData),
            success: function () {
                showSuccess('User Type added successfully!');
                $('#userTypeForm')[0].reset(); // Reset form after successful submission
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
                case 'WAM-505':
                case 'WAM-507':
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
