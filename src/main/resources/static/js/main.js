// Header Scroll
const nav = document.querySelector(".navbar");
window.onscroll = function () {
    if (document.documentElement.scrollTop > 50) {
        nav.classList.add("header-scrolled");
    } else {
        nav.classList.remove("header-scrolled");
    }
}

// Nav Hide
const nabBar = document.querySelectorAll(".nav-link");
const navCollapse = document.querySelector(".navbar-collapse.collapse");

nabBar.forEach(function (a) {
    a.addEventListener("click", function () {
        navCollapse.classList.remove("show");
    })
})

// Swiper Slider
var swiper = new Swiper(".mySwiper", {
    direction: "vertical",
    loop: true,
    pagination: {
        el: ".swiper-pagination",
        clickable: true,
    },
    autoplay: {
        delay: 3500,
    },
});

function toggleBookingDetails() {
		const bookingDetails = document.getElementById('booking-details');
		if (bookingDetails.style.display === 'none') {
			bookingDetails.style.display = 'block';
		}else{
			bookingDetails.style.display = 'none';
		}
	}

// Counter Design
document.addEventListener("DOMContentLoaded", () => {
    function counter(id, start, end, duration) {
        let obj = document.getElementById(id);
        if (obj) {  // Ensure the element exists
            let current = start;
            let range = end - start;
            let increment = end > start ? 1 : -1;
            let step = Math.abs(Math.floor(duration / range));
            let timer = setInterval(() => {
                current += increment;
                obj.textContent = current;
                if (current == end) {
                    clearInterval(timer);
                }
            }, step);
        }
    }
    counter("count1", 0, 1287, 3000);
    counter("count2", 100, 2087, 3000);
    counter("count3", 0, 2088, 3000);
    counter("count4", 0, 2277, 3000);
})

// Our Partner
var swiper = new Swiper(".our-partner", {
    slidesPerView: 5,
    spaceBetween: 30,
    loop: true,
    autoplay: {
        delay: 2000,
    },
    breakpoints: {
        '991': {
            slidesPerView: 5,
            spaceBetween: 10,
        },
        '767': {
            slidesPerView: 3,
            spaceBetween: 10,
        },
        '320': {
            slidesPerView: 2,
            spaceBetween: 8,
        },
    },
});

$(document).ready(function () {
    const roomSelect = $(".room-select");
    const reserveButton = $("#reserve-button");
    const thumbnails = document.querySelectorAll('.thumbnail');

    // Reset the room selection
    function resetRoomSelection() {
        roomSelect.each(function () {
            $(this).val(0);
        });
    }

    // Function to format the price with two decimal places and thousand separators
    function formatPrice(price) {
        return price.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ",") + ' VND';
    }

    // Calculate the total cost
    function calculateTotal() {
        let total = 0;
        roomSelect.each(function () {
            let roomDurationPrice = parseFloat($(this).data("duration-price"));
            let roomCount = parseInt($(this).val(), 10);
            total += roomDurationPrice * roomCount;
        });

        $("#totalPrice").html('<strong>' + formatPrice(total) + '</strong>');
        $("#totalPriceInput").val(total.toFixed(2));

        if (total === 0) {
            reserveButton.prop('disabled', true);
            reserveButton.removeClass('btn-primary').addClass('btn-secondary');
        } else {
            reserveButton.prop('disabled', false);
            reserveButton.removeClass('btn-secondary').addClass('btn-primary');
        }
    }

    // Reset room selection whenever page is shown
    $(window).on("pageshow", function () {
        resetRoomSelection();
        calculateTotal();
    });

    // Update the room count and calculate the total when room selection changes
    roomSelect.on("change", function () {
        let count = $(this).val();
        $(this).siblings("#roomCountInput").val(count);
        calculateTotal();
    });

    // Event for clicking the reserve button
    reserveButton.click(function () {
        $("#booking-form").submit();
    });

    // Initial function calls
    calculateTotal();
});
