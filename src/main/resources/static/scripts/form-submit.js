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
        form.addEventListener('submit', async function(event) {
            event.preventDefault();

            const xsrfToken = getCsrfToken();
            const formData = new FormData(form);

            try {
                const response = await fetch(form.action, {
                    method: 'POST',
                    headers: {
                        'X-XSRF-TOKEN': xsrfToken,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams(formData),
                    redirect: 'manual'
                });

                if (response.redirected) {
                    const redirectUrl = response.url;
                    window.location.href = redirectUrl;
                } else if (response.status === 302) {
                    const redirectUrl = response.headers.get('Location');
                    window.location.href = redirectUrl;
                } else if (response.ok) {
                    console.log('Запрос успешно выполнен.');
                    console.log(await response.text());
                } else {
                    console.error('Произошла ошибка при выполнении запроса.');
                }
            } catch (error) {
                console.error('Произошла ошибка при выполнении запроса.', error);
            }
        });
    });
});