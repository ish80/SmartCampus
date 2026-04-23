# SmartCampus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey) for managing campus rooms and IoT sensors. Developed as part of the 5COSC022W Client-Server Architectures coursework at the University of Westminster.

---

## API Design Overview

The API follows a resource-based REST architecture, versioned under '/api/v1'. There are three core resources:

- **Rooms** — physical spaces on campus, each with an ID, name, capacity, and a list of sensor IDs deployed in them.
- **Sensors** — IoT devices assigned to rooms, identified by type (e.g. CO2, Temperature, Occupancy) with a status and a current value.
- **Sensor Readings** — historical data points recorded by a sensor, exposed as a nested sub-resource under sensors.


### Key Design Decisions

- **In-memory storage** using static 'HashMap' and 'ArrayList' via a shared 'DataStore' class. No database is used.
- **Bidirectional linking**: when a sensor is registered, its ID is automatically added to the parent room's 'sensorIds' list.
- **Sub-resource locator pattern**: sensor readings are handled by a dedicated 'SensorReadingResource' class, keeping the codebase modular.
- **Custom exception mapping**: every error condition returns a structured JSON response with an appropriate HTTP status code. Raw stack traces are never exposed.
- **Request/response logging**: a JAX-RS filter logs every incoming request method and URI, and every outgoing status code.

### HTTP Status Codes Used

| Status | Meaning in this API |
|--------|---------------------|
| 200 | Successful GET |
| 201 | Resource created (POST) |
| 204 | Resource deleted (DELETE) |
| 404 | Resource not found |
| 409 | Room cannot be deleted if sensors are still assigned |
| 415 | Unsupported media type (non-JSON request body) |
| 422 | Sensor references a roomId that does not exist |
| 403 | Sensor is in MAINTENANCE, cannot accept readings |
| 500 | Unexpected server error |

---

## Prerequisites

- **Java 8** or higher
- **Apache Maven 3.6+**
- **Apache Tomcat 9.x** (or compatible servlet container)

---

## Build and Launch Instructions

### Step 1 — Clone the repository

'''
git clone https://github.com/ish80/smartCampus.git
'''

### Step 2 — Open the project in NetBeans

### Step 3 — Build the project

Right-click on project and select "Clean and Build"

### Step 4 — Run the project

Right-click on project and select "Run"

### Step 5 — Verify the server is running

Netbeans will automatically open the browser with the body of the page displaying Hello World!

---

## Sample curl Commands

### 1. Discover the API

'''
curl -X GET http://localhost:8080/SmartCampus/api/v1
'''

Returns API version info and links to'/SmartCampus/api/v1/rooms' and'/SmartCampus/api/v1/sensors'.

---

### 2. Create a room

'''
curl -X POST http://localhost:8080/SmartCampus/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "r01", "name": "Library", "capacity": 50}'
'''

Returns '201 Created' with the new room object.

---

### 3. Register a sensor in that room

'''
curl -X POST http://localhost:8080/SmartCampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "s01", "type": "CO2", "status": "OPERATIONAL", "currentValue": 412.5, "roomId": "r01"}'
'''

Returns '201 Created'. The sensor is linked to room 'r01' and the room's 'sensorIds' list is updated automatically.

---

### 4. Add a sensor reading

'''
curl -X POST http://localhost:8080/SmartCampus/api/v1/sensors/s01/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 450.0}'
'''

Returns '201 Created'. The sensor's'currentValue' is updated to'450.0'.

---

### 5. Get all sensors filtered by type

'''
curl -X GET "http://localhost:8080/SmartCampus/api/v1/sensors?type=CO2"
'''

Returns a list of all sensors with type'CO2' (case-insensitive).

---

### 6. Attempt to delete a room that still has sensors (409 error)

'''
curl -X DELETE http://localhost:8080/SmartCampus/api/v1/rooms/r01
'''

Returns '409 Conflict' - the room cannot be deleted while 's01' is still assigned to it.

---

### 7. Register a sensor with status MAINTENANCE

'''
curl -X POST http://localhost:8080/SmartCampus/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "s02", "type": "Temperature", "status": "MAINTENANCE", "currentValue": 21.5, "roomId": "r01"}'
'''

Returns '201 Created'.

---

### 8. Attempt to post a reading to a sensor in MAINTENANCE (403 error)

'''
curl -X POST http://localhost:8080/SmartCampus/api/v1/sensors/s02/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 999.0}'
'''

If the sensor status is 'MAINTENANCE', returns '403 Forbidden'.

---

## Report — Question & Answers

### Part 1: Service Architecture & Setup

**Q1. In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

By default, JAX-RS creates a new instance of each resource class for every incoming HTTP request. Instance variables are reset on each request, so they can't hold shared state between calls. This is why the project uses a separate 'DataStore' class with static 'HashMap' fields static members belong to the class itself and persist across requests regardless of how many resource instances are created.

The practical consequence of this design is thread safety. With multiple requests arriving simultaneously, all of them read from and write to the same static maps. The current implementation uses regular 'HashMap', which is fine for single-user testing but unsafe under concurrent load. A production system would need 'ConcurrentHashMap' or explicit synchronization blocks to prevent lost updates and corrupted reads.

---

**Q2. Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

HATEOAS means responses include links that clients can follow to navigate the API. The discovery endpoint at 'GET /api/v1' returns links to '/api/v1/rooms' and '/api/v1/sensors'. Clients that follow those links don't need to hardcode URLs if the path structure ever changes, they adapt automatically.

The practical benefit over static documentation is that the links are always in sync with the actual API. Docs go stale; responses don't. For client developers, starting at the root endpoint and following links is also more intuitive than hunting through a PDF spec to figure out the correct URL format.

---

### Part 2: Room Management

**Q3. When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

Returning only IDs keeps responses small but forces the client to make a separate request for each ID to get any useful data this is the N+1 problem. If there are 200 rooms and a facilities dashboard needs to display names and capacities, that's 200 extra HTTP round-trips.

Returning full objects costs more bandwidth upfront but saves all those follow-up calls. For most real use cases, where clients need the details immediately, full objects are the better trade-off. This implementation returns full 'Room' objects from 'GET /api/v1/rooms'.

---

**Q4. Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

The first 'DELETE' on an existing room removes it and returns '204 No Content'. A second 'DELETE' on the same ID returns '404 Not Found' because the room is already gone.

The server state doesn't change after the first call the room stays deleted so the operation is idempotent in terms of effect. The response code differs between calls, which is technically not fully idempotent by strict HTTP definition. This is a common practical trade-off: the '404' gives the client useful feedback rather than silently returning success when there was nothing to delete.

---

### Part 3: Sensor Operations & Linking

**Q5. We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

JAX-RS compares the request's 'Content-Type' header against the '@Consumes' annotation before the method runs. If they don't match, the framework returns '415 Unsupported Media Type' and the resource method is never invoked. No application code runs, and no exception mapper is needed. This is a clean contract enforced entirely at the framework level.

---

**Q6. You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g.,/api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

'/sensors?type=CO2' makes it clear that filtering is an optional modification of the '/sensors' collection. The resource identity stays '/sensors' regardless of whether you filter or not. '/sensors/type/CO2' implies a sub-resource, which is misleading and it creates a naming conflict with the existing '/{sensorId}' path parameter. What if a sensor has the id '"type"'?

Path parameters are for identifying specific resources. Query parameters are for refining what comes back. The query approach is also easier to extend: adding a second filter like '?type=CO2&status=OPERATIONAL' requires no URL structure changes.

---

### Part 4: Sub-Resources

**Q7. Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con- troller class?**

Rather than defining 'GET /sensors/{id}/readings' and 'POST /sensors/{id}/readings' directly inside 'SensorResource', the locator method hands off to a separate 'SensorReadingResource' instance scoped to the sensor ID. That class is responsible for everything readings-related.

The benefit is separation of concerns. Each class has one job. If you put every nested path into a single controller, it grows into a file that's hard to read, test, and change without affecting something else. Sub-resources let you compose the API in independent modules that can be developed and tested in isolation.

---

### Part 5: Error Handling & Logging

**Q8. Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

'404' means the endpoint you're trying to reach doesn't exist. '422' means the endpoint is fine and the JSON arrived intact, but something inside the payload is semantically wrong. When a client registers a sensor with a 'roomId' that doesn't match any room, the '/sensors' endpoint is working correctly, the problem is the value inside the request body.

Using '404' here would mislead the client into thinking they called the wrong URL. '422' tells them precisely what went wrong: the request was understood, but the referenced resource doesn't exist.

---

**Q9. From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

A stack trace reveals class names, package names, method names, line numbers, and often third-party library versions. An attacker can use this to identify which frameworks and versions are running, look up known CVEs for those versions, understand internal code structure to craft targeted payloads, and infer where input validation is missing from the call stack.

The 'GenericExceptionMapper' in this project addresses this by catching all 'Throwable' instances, logging the full trace server-side, and returning only a generic '500 Internal Server Error' message to the client. The client learns nothing about the internals.

---

**Q10. Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

A filter registered once applies to every request and response automatically. If you add log statements to every method individually, you have to remember to add them to new methods, you can't change the log format without editing multiple files, and each resource class gets cluttered with code that has nothing to do with its actual job.

Filters keep cross-cutting concerns logging, authentication, metrics out of business logic. They're also easier to toggle or extend. Adding rate limiting or tracing later means touching one class, not every resource in the project.

---
