document.addEventListener('DOMContentLoaded', function () {
    // Update current time in header
    var timeEl = document.getElementById('currentTime');
    if (timeEl) {
        function updateTime() {
            var now = new Date();
            timeEl.textContent = now.toLocaleString('ko-KR', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit'
            });
        }
        updateTime();
        setInterval(updateTime, 1000);
    }
});
