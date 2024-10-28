$(document).ready(function () {
    const authToken = sessionStorage.getItem('authToken');

    // Fetch user types and permissions when the page loads
    fetchUserTypes();
    fetchPermissions();

    // Handle form submission
    $('#assignPermissionForm').on('submit', function (event) {
        event.preventDefault(); // Prevent default form submission
        assignPermission();
    });

    function fetchUserTypes() {
        $.ajax({
            url: wamAutomationEngineBaseURL + 'usertypes',
            type: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
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

    function fetchPermissions() {
        $.ajax({
            url: wamAutomationEngineBaseURL + 'permissions',
            type: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'origin': 'localhost'
            },
            success: function (response) {
                populatePermissions(response.permissionDtoList);
            },
            error: function (request) {
                handleErrors(request);
            }
        });
    }

    function populateUserTypes(userTypes) {
        const userTypeSelect = $('#userTypeSelect');
        userTypes.forEach(userType => {
            const option = `<option value="${userType.id}">${userType.userTypeName}</option>`;
            userTypeSelect.append(option);
        });
    }

    function populatePermissions(permissions) {
        const permissionSelect = $('#permissionSelect');
        permissions.forEach(permission => {
            const option = `<option value="${permission.id}">${permission.permissionType}</option>`;
            permissionSelect.append(option);
        });
    }

    function assignPermission() {
        const selectedUserType = $('#userTypeSelect').val();
        const selectedPermission = $('#permissionSelect').val();

        if (!selectedUserType || !selectedPermission) {
            showError('Please select both user type and permission.');
            return;
        }

        $.ajax({
            url: `${wamAutomationEngineBaseURL}permissions/${selectedPermission}/usertypes/${selectedUserType}`,
            type: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'origin': 'localhost',
                'Content-Type': 'application/json'
            },
            data: JSON.stringify({ permissionListId: selectedPermission, userTypeId: selectedUserType }),
            success: function () {
                showSuccess('Permission assigned successfully!');
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
                case 'WAM-508':
                    errorMessage = 'User type or permission does not exist.';
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
