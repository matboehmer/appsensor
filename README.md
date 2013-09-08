# AppSensor

The _AppSensor_ that we provide in this project is a virtual sensor for measuring mobile application usage on the Android plattform.

The _AppSensor_ is collecting the following four types of events on mobile application usage:

* App installation
* App updates
* App removals
* App being used (determined by events open and close)

The user’s application usage is sampled as a sequence of events, i.e. every usage of an app results in one event; a user’s swapping between two or more apps also results in a list of events. In the database of a central server an event has the following attributes:
* Type of event
* Package name of the application
* Start time of event (UTC timestamp)
* Offset from local time zone to UTC (in hours)
* Usage time (milliseconds)
* Information of location API
** Longitude
** Latitude
** Accuracy
** Altitude
** ￼Speed
* Powerstate (connected / unconnected to charger)
* Powerlevel (battery load in percentage)
* Time of last screen on for sessions (UTC timestamp)
* State of headphones (plugged in: yes / no)
* Orientation of device (portrait / landscape)
* WiFi state (turned off / turned on / connected)
* Bluetooth state (turned off / turned on / connected)
* GPS state [implementation planned]
* Identification of device
** Hash of IMEI device id (deprecated, unique per device)
** Installation id (unique per installation)
** Information about device
* Model name
* Screen resolution o Android API Level
** Version of the _AppSensor_ library being used (form AndroidManifest)
* Client IP4 address (only through server script)

## Persisting of Events 

### In-memory Caching

The _AppSensor_ is currently being sampled with a frequency of 2Hz, i.e. every 500ms we capture which application is currently running. Whenever we observe a different application, we create a new event of type app usage for the current app. The _AppSensor_ starts observing app usage when device is unlocked / waked up from stand-by / screen is turned on.

To save battery and for better performance, this real-time data is saved in memory. Most of the attributes of an event are determined when an application is started (e.g. package name, type, location). Some pieces of information are constantly being used to update the event, e.g. usage time and device orientation.

### Local DB

Every time the device goes into stand-by mode, i.e. when the screen turns off, we write all data from memory into a local MySQLite database on the Android device. Further, the _AppSensor_ stops sampling app usage since the user cannot use any app when the device is locked.

### Server Upload

Synchronization with the server is done via the Android sync manager. The data is send as a CSV file, where all redundant fields are blanked to save bandwidth. On the server side, a PHP script receives the CSV file as HTTP POST. It reads the file and writes all data fields into a dedicated table of a MySQL database. Before uploading, such values that are constant but device-specific are added to the event, e.g. screen resolution or device model.