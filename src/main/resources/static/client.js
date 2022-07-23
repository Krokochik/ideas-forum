var requestUrl = 'https://ap-plication.herokuapp.com'

function onLoad() {
    sendRequest('POST', requestUrl + "/repositories/0", {
       command: 'getVariableValue',
       name: "number"
       })
      .then(data => {
          if (JSON.parse(JSON.stringify(data)).status === 404) {
              sendRequest('POST', requestUrl + "/", {
                 command: 'generateRandomNumber',
              })
              .then(randNum => {
                  sendRequest('POST', requestUrl + "/repositories/0", {
                     command: 'addVariable',
                     name: "number",
                     value: randNum
                  })
              })
          }
          onLoad()
      })
      .catch(err => console.log(err))
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