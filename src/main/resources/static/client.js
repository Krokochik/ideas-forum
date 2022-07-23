var requestUrl = 'https://ap-plication.herokuapp.com'

function onLoad() {
    var number = JSON.parse(JSON.stringify(sendRequest('POST', requestUrl + '/repositories/0', {
        command: "getVariableValue",
        name: "number"
    })));
    console.log(number);
    console.log(number.status)
    console.log(number.status == "404")
    if(number.status === "404") {
        var randNum = sendRequest('POST',  requestUrl + "/", {
            command: "generateRandomNumber"
        })
        console.log(randNum)
    }
}


function sendRequest(method, url, body = null) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()

    xhr.open(method, url)

    xhr.responseType = 'json'
    xhr.setRequestHeader('Content-Type', 'application/json')

    xhr.onload = () => {
      if (xhr.status >= 400) {
        reject(xhr.response)
      } else {
        resolve(xhr.response)
      }
    }

    xhr.onerror = () => {
      reject(xhr.response)
    }

    xhr.send(JSON.stringify(body))
  })
}

onLoad();

//sendRequest('GET', requestUrl)
//  .then(data => console.log(data))
//  .catch(err => console.log(err))