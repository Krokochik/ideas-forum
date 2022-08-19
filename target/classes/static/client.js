let requestUrl = 'https://ap-plication.herokuapp.com'
let numbers = [];
let minV = document.getElementById('minValue').value;
let maxV = document.getElementById('maxValue').value;

function generate() {
    if ((document.getElementById('checkbox').checked == true && document.getElementById('repeats').value <= document.getElementById('maxValue').value)
      || document.getElementById('checkbox').checked == false) { 
      let repeats = document.getElementById('repeats').value;
      numbers.length = 0;
      minV = document.getElementById('minValue').value;
      maxV = document.getElementById('maxValue').value;
      document.getElementById('numbers').innerHTML = "";
      send(0, Number(repeats));  
    }
    else 
    alert("Количество повторов превышает маскимальное значение, невозможно сгенерировать без дубликтов");
}

function send(i = 0, howMany) {
  if(i++ !== howMany) {
    sendRequest('POST', requestUrl + '/', {
          command: 'generateRandomNumber',
          min: minV,
          max: maxV
    })
    .then(data => {
      if (document.getElementById('checkbox').checked) {
        if (!numbers.includes(JSON.parse(JSON.stringify(data)).number)) {
          numbers[i-1] = JSON.parse(JSON.stringify(data)).number;
        }
        else 
          i--;
      }
      else
        numbers[i-1] = JSON.parse(JSON.stringify(data)).number;
      return send(i, howMany);
    })
  }
  else {
    for (let i = 0; i < numbers.length; i++) {
        let articleDiv = document.querySelector("div.numbers");
        let elem = document.createElement("a");
        elem.style = "margin-right: 10px;"
        let elemText = document.createTextNode(numbers[i]);
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

//sendRequest('GET', requestUrl)
//  .then(data => console.log(data))
//  .catch(err => console.log(err))