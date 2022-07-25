var requestUrl = 'https://ap-plication.herokuapp.com'

function onLoad() {
    sendRequest('POST', requestUrl + '/repositories/0', {
            command: "getVariableValue",
            name: "number"
    }).then(data => {
        data = JSON.parse(JSON.stringify(data))
        if(data.status === "404") {
            sendRequest('POST',  requestUrl + "/", {
                command: "generateRandomNumber"
            }).then(rand => {
               if(rand.status === "200"){
                   sendRequest('POST', requestUrl + "/repositories/0", {
                       command: "addVariable",
                       name: "number",
                       value: rand.number
                   }).then(add => { onLoad(); })
               }
            })
        }
        else {
            console.log(data.value);
        }
    })
    sendRequest('GET', requestUrl + "/repositories/1")
      .then(data => console.log(data))
      .catch(err => console.log(err))

    sendRequest('POST', requestUrl + "/", {
        command: "generateRandomNumber"
    })
          .then(data => console.log(data))
          .catch(err => console.log(err))
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