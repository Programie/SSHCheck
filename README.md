# SSHCheck

Check whether you are reaching a list of SSH servers.

[![Build Status](https://travis-ci.org/Programie/SSHCheck.svg)](https://travis-ci.org/Programie/SSHCheck)

## Building

You have to use Maven to build the application.

Simply execute the following command in the root folder of your checkout:

```
mvn clean package
```

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

If no server list file is specified, the servers.json from the path of the jar file will be used.

## Server file structure

A server list can be in various formats. Currently only JSON and plain text files are supported.

### JSON

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
        },
        {
            "hostname" : "another-server.example.com",
            "username" : "me"
        },
        {
            "hostname" : "some-server.example.com"
        }
    ]
}
```

The username is retrieved in the following way:

   * "username" property from the server map
   * "username" command line option
   * "username" property from the global map
   * "user.name" system property providing the system's local username

### Plain text

```
root@server1.example.com
me@another-server.example.com
some-server.example.com
```

The username is retrieved in the following way:

   * The username before the "@" sign
   * "username" command line option
   * "user.name" system property providing the system's local username