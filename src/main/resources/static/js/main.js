// Main JavaScript for Blood Bank Management System

document.addEventListener('DOMContentLoaded', function() {
    
    // 1. Sidebar Toggle for Mobile Layouts
    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }

    // 2. Proactive Alert Notification Autoclose
    const alertList = document.querySelectorAll('.alert-dismissible');
    alertList.forEach(function(alert) {
        setTimeout(function() {
            if (typeof bootstrap !== 'undefined') {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } else {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 500);
            }
        }, 5000);
    });

    // 3. Double-check Client Side Password Validation on Donor Registration
    const regForm = document.getElementById('registerForm');
    if (regForm) {
        regForm.addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPass = document.getElementById('confirmPassword')?.value;
            
            if (confirmPass && password !== confirmPass) {
                e.preventDefault();
                alert('Passwords do not match! Please check password confirmation.');
            }
        });
    }
});

/**
 * Renders a Doughnut/Bar Chart for Blood Inventory Overview
 * @param {string} canvasId 
 * @param {Array<string>} keys 
 * @param {Array<number>} values 
 */
function initializeInventoryChart(canvasId, keys, values) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    const colors = [
        '#ef4444', // A+
        '#f87171', // A-
        '#dc2626', // B+
        '#fca5a5', // B-
        '#991b1b', // AB+
        '#fecaca', // AB-
        '#7f1d1d', // O+
        '#fee2e2'  // O-
    ];

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: keys,
            datasets: [{
                label: 'Blood Stock (Units)',
                data: values,
                backgroundColor: colors,
                borderColor: '#b91c1c',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}
