# Dynamic REST services Emulator
Emulito is used for emulation of services to allow developers/testers to remove dependencies on services that are external to the application under development/test.

The Emulito emulator provides the ability to setup dynamic responses in real time for any external service so that a developer can force any scenario they wish to happen in order to verify that the application under development handles this particular external response/behaviour successfully.

## Summary of emulator behaviour:
There are 3 special emulator specific URLs:
1. /presetResponse (HTTP POST)
2. /applicationRequest (HTTP GET)
3. /reset (HTTP DELETE)

The emulator is designed so that it will answer any and all requests which it receives on the port which it happens to be running on. When a request comes in (which is not one of the 3 emulator specific URLs listed above), the emulator will search in its internal storage for a pre-set response and reply with that if it exists. Before sending the response the emulator first saves the incoming request (and its associated details) in an internal storage queue which is queried for the **/applicationRequest** emulator specific URL. Each application request will be stored in this queue until either it is returned in the **/applicationRequest** call or until the emulator state is reset (via the **/reset** call). 


## Setting up responses
Dynamic responses are set up via sending a HTTP POST request to the **/presetResponse** emulator specific URL with a body following the sample below:


```json
{
  "response": {
    "statusCode": "205", 
    "headers": { 
      "content-type": [
        "application/json"
      ]
    },
    "body": "{\“greeting\”:\”Hello World\”}"
  },
  "delay" :{
    "delayTimeInMillis": 10000 
  },
  "timeToLive": "UNTIL_RESET", 
  "priority": 2, 
  "predicate": {
    "requestType": "books_create_token",
    "rules": [
      "url is '/token'",
      "body contains 'grant_type=password'",
      "url-params contain parameter 'api_key' whose value is 'test'",
      "headers contain entry 'header' whose value is 'test'"
    ]
  }
}
```

**statusCode:** The HTTP status code to be returned in the response to the application  
**headers:** The HTTP headers to be returned in the response to the application  
**body:** The actual response body to be returned by the emulator (note that in this example the response is in JSON format and so the quotes must be escaped by '\\)  
**waitTimeInMillis _(Optional)_:** Defaults to 0. The emulator will wait this long before returning this response. This can be used to test scenarios where external services timeout or are very slow.  
**timeToLive _(Optional)_:** Accepted values are [SINGLE_USE, UNTIL_RESET, FOREVER] Defaults to **SINGLE_USE**. **SINGLE_USE** means that after returning this response the emulator will immediately purge it from its internal storage. If set to **UNTIL_RESET** the emulator will only purge this response from its internal storage when the emulator specific URL **‘/reset’** is called. If set to **FOREVER** the emulator will never purge it from its internal storage. The **FOREVER** value is intended to be used for default response setups that would be carried out at the beginning of use or the beginning of each full test suite run (if Emulito is being used for test automation). These response setups would comprise of a “happy path” of responses which will last forever (until emulator shutdown) to provide basic successful functionality for the application under development.  
**priority _(Optional)_:** Defaults to 1. For application requests which match multiple setup responses the highest priority responses will take precedence  
**predicate:** Discussed in next section  

### Predicates
The predicate node contains 2 attributes:  
**requestType:** This is a name defined by the test client to describe this response. This name is then used in the **/applicationRequest** call to return the last application request which matched this predicate.  
**rules:** This is a list of all predicates which are to be tested in order to match an incoming request to this dynamic response. If multiple rules are supplied they must all match for the specified response to be returned. 
The following language is currently supported but can be extended:  
**_Supported request attributes:_** _http method, url, url parameters, headers, body_  
**_Supported predicate matching values:_** _is, contains, starts with, ends with, matches_  
Please see [appendix 1](#user-content-appendix-1) for the full list of possible predicates with examples

Any combination of request attributes and predicate matchers can be used to define the rules for response returning. If multiple predicates are supplied then they must all match for the specified response to be returned. For example if an automated test needed to verify that the application under test handled a HTTP 401 unauthorized response from an external service it would call the emulator specific URL **/presetResponse** with the following request body before initiating the test action on the application:

```json
{
  "response": {
    "statusCode": "401", 
    "headers": {       "content-type": [
        "application/json"
      ]
    },
    "body": "{\“errorCode\”:\”1\”,\”errorMessage\”:\”You are not authorized to access the requested resource\”}"
  } 
  "timeToLive": “SINGLE_USE”, 
  "priority": 2, 
  "predicate": {
    "requestType": "acme_service_ltd_get_book_request",
    "rules": [
      "url is '/books'",
      "url parameters contain parameter 'bookId' whose value is 'testBook'”
    ]
  }
}
```

After this request is dynamically setup in the emulator, the next time a request is received by the emulator from the application for the following URL: **‘/books?bookId=testBook’** then a HTTP 401 response will be returned with the following response body:
```json
{
  “errorCode”:”1”,
  ”errorMessage”:”You are not authorized to access the requested resource”
}
```
Along with http header **content-type:application/json**

This then will allow the developer (or automated test client) to assert that the application handles this external response in the correct manner. Since the **timeToLive** attribute is set to **SINGLE_USE** the emulator will purge this preset response from its internal storage after returning it, so that it will not be returned again which would result in interference with subsequent automated tests/behavior of the application. Also notice that the priority attribute is set to 2 which means that this will take precedence over any other response which may match these predicate rules (notably any default responses which used a timeToLive of **FOREVER**) and which have a priority of 1 (which the default responses should have by convention).


## Dynamically retrieving outgoing application requests:
This section details the ability for an developer (or automated test) to dynamically retrieve the last outgoing application request of the specified type (or more accurately, that matched a particular setup response) which was received by the emulator. Application requests are dynamically retrieved via sending a HTTP GET request to the emulator specific URL **/applicationRequest** with the URL parameter **requestType** set to a valid value.  
E.g. Continuing on from the previous example, if we wanted to retrieve the last outgoing application request which was sent to the **/books** URL we would send a HTTP GET request to the URL **/applicationRequest?requestType=acme_service_ltd_get_book_request**
(where acme_service_ltd_get_book_request was specified in the predicate node “requestType” attribute in the response setup). 
The response body received from the emulator (to represent the outgoing application request) is in the following format:

```json
{
  "httpMethod": "POST", 
  "url": "fdsa", 
  "urlParameters": { 
    "SomeUrlParamKey": [
      "UrlParamValue1",
      "UrlParamValue2"
    ]
  },
  "headers": { 
    "SomeHeaderKey": [
      "HeaderValue1",
      "HeaderValue2"
    ]
  },
  "body": "The actual HTTP request body contents sent by the application"
}
```

**httpMethod:** The HTTP method that the application used  
**url:** The HTTP URL that the application sent the outgoing request to  
**urlParameters:** The list of HTTP URL parameters that were set in the URL that the application sent the outgoing request to  
**headers:** The HTTP headers that were set in the outgoing application request  
**body:** The HTTP request body contents of the outgoing application request  

Using this emulator response the developer (or automated test) can assert that all of the required HTTP attributes were sent out by the application with the correct values. This is analogous to asserting that mock objects have been called correctly in unit testing.


## Resetting the emulator
The following section details the ability to dynamically reset the internal emulator state.  
The internal emulator state is dynamically reset via sending a HTTP DELETE request to the emulator specific URL **/reset**. After this call is made all previously stored application requests are deleted from internal storage and any dynamically setup responses which do not have a timeToLive of **FOREVER** will be deleted from internal storage. This call is intended to be used by automation test clients before each automated testcase so that the emulator state starts fresh and only actions recorded/setup for that particular testcase will end up being present.


<a name="appendix1"></a>
# Appendix 1
## Rules
### Http Method
The “http method” rule attribute can be used to create a rule which matches the HTTP method used in the incoming request. For example the below rule definitions would match an incoming request that used a HTTP ‘POST’ method:  
http method is ‘POST’  
http method contains ‘OS’  
http method starts with ‘P’  
http method ends with ‘T’  
http method matches ‘/P([a-Zaz0-9]*)ST’  

### URL
The “url” rule attribute can be used to create a rule which matches the URL that the incoming request was sent to. For example the below rule definitions would match an incoming request that was sent to the ‘/books’ URL:  
url is ‘/books’  
url contains ‘ok’  
url starts with ‘/boo’  
url ends with ‘ks’  
url matches ‘/bo([a-Zaz0-9]*)s’  

### URL Parameters
The “url parameters” rule attribute can be used to create a rule which matches the URL parameters that were set on the incoming request’s URL. For example the below rule definitions would match an incoming request that was sent to a URL with the parameters ‘bookId=testBook’:  
url parameters contain parameter ‘bookId’ whose value is ‘testBook’  
url parameters contain parameter ‘bookId’ whose value contains ‘stBoo’  
url parameters contain parameter ‘bookId’ whose value starts with ‘tes’  
url parameters contain parameter ‘bookId’ whose value ends with ‘ook’  
url parameters contain parameter ‘bookId’ whose value matches ‘test([a-Zaz0-9]*)’  

### Headers
The “headers” rule attribute can be used to create a rule which matches the HTTP headers that were present on the incoming request. For example the below rule definitions would match an incoming request that had a header called ‘userId’ with the value ‘test_user’:  
headers contain entry ‘userId’ whose value is ‘test_user’  
headers contain entry ‘userId’ whose value contains ‘t_us’  
headers contain entry ‘userId’ whose value starts with ‘test’  
headers contain entry ‘userId’ whose value ends with ‘user’  
headers contain entry ‘userId’ whose value matches ‘test([a-Zaz0-9_]*)ser’  

### Body
The “body” rule attribute can be used to create a rule which matches the HTTP request body on the incoming request. For example the below rule definitions would match an incoming request that had the following request body:
```json
{
  "bookType": "hardback"
}
```
body is ‘{“bookType”:”hardback”}’  
body contains ‘back\”}’  
body starts with ‘{\“bookType\”’  
body ends with ‘\”hardback\”}’  
body matches ‘([a-Zaz0-9]*)back’