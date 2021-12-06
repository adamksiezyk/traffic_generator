# Test processing cycle

## Visualization

![Process cycle diagram](tr-notes.png)

## Connection - authorization

Application sends login HTTP request and receives a JWT (`JSON Web Token`)

Related code can be found in `trafficgenerator.onActivityResult`.

### authorization fields

 - login
 - password
 - name or uuid (depending on whether device already exists)

## Connection - heartbeat

After logging device into the system it is required to send HTTP POST request
to `/device/active` url.

This is handled by a fixed rate timer identified as `"keepAlive"` that issues
required request every 10 minutes and is cancelled upon logout.

### related activities

 - `trafficgenerator.onSuccessfulLogin`
 - `trafficgenerator.logOut`


## Retrieveing tasks

After receiveing JWT client subsribes to a websocket that broadcasts
notifications about new tasks being issued.

Subscribing to `/topic/device_${uuid}` url gives following message upon event:

### websocket message

```json
{
	"taskId": "long",
	"taskType": "string"
}
```

## Retrieveing tasks

Upon receiveing notification with `"taskId"` and `"taskType"` UE sends GET
request to `/device/tasks` (`serverApi.getTasks`) in order to retrieve given
tasks details. Look `trafficgenerator.newTaskReceived`.

Server responds with an array of all the tasks for a given device.

### /device/tasks reponse

```json
[
	{
		"id": "long",
		"taskType": "string",
		"status": "string",
```

## Retrieveing tasks

UE filters out all entries except for one with id received through websocket
notification.

The task is added to the execution queue:

### adding task to queue

```kt
if (tasks.isEmpty()) {
	appendStringToLog(
	"Failed to get task ${task.taskId} from all tasks"
	)
} else {
	asyncTaskExecutor.addTaskToExecutionQueue(
	tasks.first(), uuid, token, ::taskFinished
	)
}
```


## Processing

Depending on `"taskType"` one of the scripts from `trafficgenerator.scripts` is
executed.

### available "scripts"

```kt
class httprequest(private val url: String){...}
class FtpClient(
		private val host     : String,
		private val port     : Int,
		private val userName : String,
		private val password : String,
		private val remoteCwd: String
)
private fun streamingTaskHandler(
	task: GetTasksResponseDTO)
```


## Uploading results

Server provides endpoint `/device/tasks/${taskId}/upload` for uploading
arbitrarily formatted files. It is used by application agent for uploading task
results in a JSON file.

After task completion Android UE collects statistics in a file named
`"task_${task.id}_result.json"` and sends it over to the server.

## Uploading results

Results file is created by a JSON serializer -
`GetTasksResponseDTO.Serializer()`

### data structure / class

```kt
data class GetTasksResponseDTO(
	val id: Long,
	val taskType: String,
	val status: String,
	val fileUrl: String?,
	val orderStart: String,
	val orderEnd: String?,
	val device: Device
) {
...  class Serializer { }
}
```

# TBD

### message

 - client info
 - availability

## Server response

### message

 - script

## Processing

 - allocate threads
 - execute tasks
 - receive test results
   + time benchmarks
   + booleans (pass/fail)

## Issueing response to the server

### message

 - test results
   + pass/fail
   + execution times

## Disconnecting

### message

 - client info
 - disconnect info
