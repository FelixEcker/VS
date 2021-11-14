# VS
A very simple form of a DNS. Instead of using strings as domains, it uses a 16-Bit number. Originally for a small, scrapped side project.

## Running
The server can simply be executed within a Terminal/Commandline using `java -jar`, it only requires there to be a settings.properties file within the working directory.

### Settings
```properties
# Example Server-Settings (settings.properties)
# "port": The Port under which the server runs
# "ping_enable": Enable the Ping-Routine
# "ping.max_noreplies": How many consecutive run-throughs of the ping-routine
#                       a server is allowed to not respond
# "ping.timeout": The time it takes for a ping to timeout in milliseconds
# "ping.sleep_length": The time the Ping-Routine stops for after every run-through (milliseconds)

port=8823
ping_enable=true
ping.max_noreplies=4
ping.timeout=10000
ping.sleep_length=120000
```

## How To Use
### Registering
In order to register a new entry, we first have to tell the server that we want to do so. This is done
by simply sending it a byte of value `0`. As soon as the server has responded we can send over the 4 bytes
of the ip address at once. If the server responds with a `1` we can go on to send over 2 bytes containing
the port. If the server again responds with a `1` we can finally send over 2 bytes containing the id for the
entry. If the server responds with a `1` again we can go on to finalize the registry by sending over a single
byte with any value. The server sends over one final response telling us if the process was sucessfull or not.

### Requesting
If we want to request some data, the first byte we sand has to be a `1`. After the server has responded we can
send of the 2 bytes which make up the id. Now the server will send over 4 bytes that contain the ip and after that
2 bytes which are the port. After this the server sends a `1`or a `0` telling us if there was an error or not.

### Server Responses
If in the process of registering or requesting data, the server responds with either a `0` or a `1`.
`0` means that an error has occured and the next byte contains the error code. If the response is a `1`,
there is no error and you can continue on as normal.

#### Error Codes
`0x0`: Unknown Error
`0x1`: ID Already Taken
`0x2`: Received invalid data (usually no enough)
`0x3`: Invalid ID
`0x4`: No Space (There is no space for creating a new Entry)
