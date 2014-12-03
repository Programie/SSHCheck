# SSHCheck

Check whether you are reaching a list of SSH servers.

## Usage

Call the jar file from the command line.

```
java -jar sshcheck.jar --help
```

This will print all available command line options.

```
java -jar sshcheck.jar --serverlist /path/to/servers.json
```

This will Start to check all servers configured in the given server list file (/path/to/servers.json).

## Server file structure

The server list is a JSON file which must have the following structure:

```
{
    "global" :
    {
        "username" : "fallback username"
    },
    "servers" :
    [
        {
            "hostname" : "server1.example.com",
            "username" : "root"
        }
    ]
}
```

The username is retrieved in the following way:

   * "username" property from the server map
   * "username" property from the global map
   * "user.name" system property providing the system's local username