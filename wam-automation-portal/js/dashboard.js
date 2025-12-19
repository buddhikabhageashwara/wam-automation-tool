$(document).ready(function () {
    fetchHomeDetails();

    function fetchHomeDetails() {
        const authToken = sessionStorage.getItem("authToken");
        $.ajax({
            url: wamAutomationEngineBaseURL + "homedetails",
            type: 'GET',
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json',
                'origin': 'localhost'
            },
            success: function (response) {
                populateMenu(response.homeDetails);
            },
            error: function () {
                alert("Failed to load menu items.");
            }
        });
    }

    function populateMenu(homeDetails) {
        const menuList = $("#menuList");
        homeDetails.forEach(item => {
            const listItem = $(`<li class="list-group-item menu-item" data-page="${item.page}">${item.name}</li>`);
            menuList.append(listItem);
        });
        // Highlight and load the first menu item by default
        if (homeDetails.length > 0) {
            $(".menu-item").first().addClass("active"); // Set first item as active
            loadPage($(".menu-item").first().data("page")); // Load the first page
        }
        // Event listener for menu item click
        $(".menu-item").on("click", function () {
            $(".menu-item").removeClass("active");
            $(this).addClass("active");
            loadPage($(this).data("page"));
        });
    }

    // Function to load selected page content
    function loadPage(page) {
        $("#contentArea").load(page);
    }

    // Logout button functionality
    $("#logoutButton").on("click", function () {
        sessionStorage.removeItem("authToken"); // Remove the authToken
        window.location.href = "login.html"; // Redirect to login page
    });
});
