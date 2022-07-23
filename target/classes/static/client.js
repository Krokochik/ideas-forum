let input = document.querySelector('#input');
var i = 0;
var path = "";

function init () {
    if (window.innerWidth <= 740 && window.inner > 500)
        document.getElementById("emergencyImage").width = 350;
    else if (window.innerWidth <= 500)
        document.getElementById("emergencyImage").width = 200;
    else
        document.getElementById("emergencyImage").width = 500;

    window.addEventListener('resize', function(event) {
        if (window.innerWidth <= 740 && window.inner > 500)
            document.getElementById("emergencyImage").width = 350;
        else if (window.innerWidth <= 500)
            document.getElementById("emergencyImage").width = 200;
        else
            document.getElementById("emergencyImage").width = 500;
    }, true);

    if (location.pathname !== path)
    {
        path = location.pathname;
        i = 0;
    }
    if (checkError() === false)
        document.getElementById("emergencyImage").style.display = 'none';

    document.addEventListener("keyup", function(event)
    {
        if (event.key == "ArrowLeft")
        {
            document.getElementById('prev').click();
        }
        else if (event.key == "ArrowRight")
        {
            document.getElementById('next').click();
        }
        else if (event.key == "Enter")
        {
            document.querySelector('#inputButton').click();
        }
    });

    input.focus();
}

function scrollToTop (duration)
{
    // cancel if already on top
    if (document.scrollingElement.scrollTop === 0){input.focus(); return;}

    const cosParameter = document.scrollingElement.scrollTop / 2;
    let scrollCount = 0, oldTimestamp = null;

    function step (newTimestamp)
    {
        if (oldTimestamp !== null)
        {
            // if duration is 0 scrollCount will be Infinity
            scrollCount += Math.PI * (newTimestamp - oldTimestamp) / duration;
            if (scrollCount >= Math.PI) return document.scrollingElement.scrollTop = 0;
            document.scrollingElement.scrollTop = cosParameter + cosParameter * Math.cos(scrollCount);
        }
        oldTimestamp = newTimestamp;
        window.requestAnimationFrame(step);
    }

    window.requestAnimationFrame(step);
}

function addError(){
    if (++i === (location.pathname === "/" ? 1 : 6)){
        document.getElementById("emergencyImage").style.display = '';
        i = 0;
        return;
    }
    document.getElementById("emergencyImage").style.display = 'none';
}

function checkError(){
    if (i === (location.pathname === "/" ? 1 : 6)) { return true; }

    return false;
}

init();