var requestUrl = 'https://ap-plication.herokuapp.com'

function onLoad() {
    var minV = document.getElementById('minValue').value;
    var maxV = document.getElementById('maxValue').value;
    var repeats = document.getElementById('repeats').value;
    let numbers = [];
    /*var j = 0;
    while (j < Number(repeats) + 1) {
        sendRequest('POST', requestUrl + '/', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
        })
        .then(data => {
          numbers[j] = JSON.parse(JSON.stringify(data)).number;
          j++;
        })
    }*/
    sendRequest('POST', requestUrl + '/', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
        })
        .then(data => {
          numbers[0] = JSON.parse(JSON.stringify(data)).number;
        })
    for (var i = 0; i < numbers.length; i++) {
      var articleDiv = document.querySelector("div.numbers");
      var elem = document.createElement("a");
      var elemText = document.createTextNode(numbers[i]);
      elem.appendChild(elemText);
      articleDiv.appendChild(elem);
      alert("ok");
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