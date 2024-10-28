$(document).ready(function () {
    const authToken = sessionStorage.getItem('authToken');

    // Handle form submission
    $('#resetPasswordForm').on('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        resetPassword(); // Call reset password function
    });

    // Trigger resetPassword function on Enter key press
    $('#resetPasswordForm').on('keydown', function (event) {
        if (event.key === 'Enter' || event.keyCode === 13) {
            event.preventDefault();
            $('#resetPasswordBtn').click(); // Trigger button click
        }
    });

    function resetPassword() {
        const currentPassword = $('#currentPassword').val().trim();
        const newPassword = $('#newPassword').val().trim();
        const confirmPassword = $('#confirmPassword').val().trim();

        // Clear previous errors and success messages
        $('#errorContainer').hide().removeClass('alert-danger alert-success').text('');

        if (!currentPassword || !newPassword || !confirmPassword) {
            showError('All fields are required.');
            return;
        }

        if (newPassword !== confirmPassword) {
            showError('New password and confirm password do not match.');
            return;
        }

        const passwordData = {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        };

        $.ajax({
            url: wamAutomationEngineBaseURL + 'users/reset-password',
            type: 'PUT',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            data: JSON.stringify(passwordData),
            success: function () {
                showSuccess('Password reset successfully!');

                // Clear the form fields after successful submission
                $('#resetPasswordForm')[0].reset();
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
                case 'WAM-505':
                case 'WAM-506':
                case 'WAM-501':
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
