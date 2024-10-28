$(document).ready(function () {
    const authToken = sessionStorage.getItem('authToken');
    const userTypeSelect = $('#userType');

    // Fetch user types when the page loads
    fetchUserTypes();

    // Handle form submission
    $('#userForm').on('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        addUser(); // Call add user function
    });

    // Trigger addUser function on Enter key press
    $('#userForm').on('keydown', function (event) {
        if (event.key === 'Enter' || event.keyCode === 13) {
            event.preventDefault();
            $('#addUserBtn').click(); // Trigger button click
        }
    });

    // Function to fetch user types from API
    function fetchUserTypes() {
        $.ajax({
            url: wamAutomationEngineBaseURL + 'usertypes',
            type: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            success: function (response) {
                populateUserTypes(response.userTypeDtoList);
            },
            error: function (request) {
                handleErrors(request);
            }
        });
    }

    // Function to populate user types dropdown
    function populateUserTypes(userTypes) {
        userTypes.forEach(function (userType) {
            const option = $('<option>').val(userType.id).text(userType.userTypeName);
            userTypeSelect.append(option);
        });
    }

    // Function to add a user
    function addUser() {
        const userTypeId = $('#userType').val();
        const userEmail = $('#userEmail').val().trim();
        const userPassword = $('#userPassword').val().trim();
        const confirmPassword = $('#confirmPassword').val().trim();
        const firstName = $('#firstName').val().trim();
        const lastName = $('#lastName').val().trim();

        // Clear previous errors and success messages
        $('#errorContainer').hide().removeClass('alert-danger alert-success').text('');

        if (!firstName || !lastName || !userEmail || !userPassword || !confirmPassword || !userTypeId) {
            showError('All fields are required.');
            return;
        }

        if (userPassword !== confirmPassword) {
            showError('Password and confirm password do not match.');
            return;
        }

        const userData = {
            userEmail: userEmail,
            userPassword: userPassword,
            confirmPassword: confirmPassword,
            firstName: firstName,
            lastName: lastName,
            userTypeId: userTypeId
        };

        $.ajax({
            url: wamAutomationEngineBaseURL + 'users',
            type: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            data: JSON.stringify(userData),
            success: function () {
                showSuccess('User added successfully!');
                $('#userForm')[0].reset(); // Reset form after successful submission
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
