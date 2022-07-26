var requestUrl = 'https://ap-plication.herokuapp.com'

function onLoad() {
    var minV = document.getElementById('minValue').value;
    alert(minV)
    var maxV = document.getElementById('maxValue').value;
    var repeats = document.getElementById('repeats').value;
    var numbers = {};
    var j = 0;
    sendRequest('POST', requestUrl + '/', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
        })
        .then(data => {
          alert(JSON.parse(JSON.stringify(data)).number);
        })

    /*while(j < repeats + 1) {
        sendRequest('POST', requestUrl + 'repositories/0', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
        })
        .then(data => {
          numbers[j] = JSON.stringify.parse(data).number;
          j++;
        })
    }
    for (var i = 0; i < numbers.size() + 1; i++) {
      var articleDiv = document.querySelector("div.numbers");
      var elem = document.createElement("a");
      var elemText = document.createTextNode(numbers[i]);
      elem.appendChild(elemText);
      articleDiv.appendChild(elem);
    }*/
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