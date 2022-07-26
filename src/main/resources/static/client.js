var requestUrl = 'https://ap-plication.herokuapp.com'
let numbers = [];
var minV = document.getElementById('minValue').value;
var maxV = document.getElementById('maxValue').value;

function generate() {
    var repeats = document.getElementById('repeats').value;
    numbers = [];
    minV = document.getElementById('minValue').value;
    maxV = document.getElementById('maxValue').value;
    send(0, Number(repeats));    
}

function send(i = 0, howMany) {
  if(i++ !== howMany) {
    sendRequest('POST', requestUrl + '/', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
    })
    .then(data => {
      numbers[i-1] = JSON.parse(JSON.stringify(data)).number;
      return send(i, howMany);
    })
  }
  else {
    for (var i = 0; i < numbers.length; i++) {
        var articleDiv = document.querySelector("div.numbers");
        var elem = document.createElement("a");
        elem.style = "margin-right: 10px;"
        var elemText = document.createTextNode(numbers[i]);
        elem.appendChild(elemText);
        articleDiv.appendChild(elem);
    }
    return;
  }
}

function sendRequest(method, url, body = null) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()

    xhr.open(method, url)

    xhr.responseType = 'json'
    xhr.setRequestHeader('Content-Type', 'application/json')

    xhr.onload = () => {
        resolve(xhr.response)
      }

    xhr.onerror = () => {
      resolve(xhr.response)
    }

    xhr.send(JSON.stringify(body))
  })
}

onLoad();

//sendRequest('GET', requestUrl)
//  .then(data => console.log(data))
//  .catch(err => console.log(err))