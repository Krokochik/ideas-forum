function getCsrfToken() {
      const cookieValue = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('XSRF-TOKEN='));

      if (cookieValue) {
        return cookieValue.split('=')[1];
      }

      return null;
}

document.addEventListener('DOMContentLoaded', function() {
    // remove spring hidden inputs
    var csrfElements = document.querySelectorAll('[name="_csrf"]');
    csrfElements.forEach(function(element) {
        element.parentNode.removeChild(element);
    });

    const forms = document.querySelectorAll('form');

    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            event.preventDefault();

            const xsrfToken = getCsrfToken();
            const xhr = new XMLHttpRequest();

            xhr.open('POST', form.action, true);
            xhr.setRequestHeader('X-XSRF-TOKEN', xsrfToken);
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

            xhr.onload = function() {
                if (xhr.status === 302) {
                    const redirectUrl = xhr.getResponseHeader('Location');
                    window.location.href = redirectUrl;
                }
            };

            xhr.onerror = function() {
                console.error('Произошла ошибка при выполнении запроса.');
            };

            const formData = new FormData(form);
            xhr.send(new URLSearchParams(formData));
        });
    });
});