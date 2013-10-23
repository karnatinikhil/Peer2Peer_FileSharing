Read me File

The program Simpella.java implements a simplified version of the Gnutella.
Simpella is a distributive search/file sharing protocol.

Command to run the Program

java Simpella <simpella Port> <download Port>

Fucntions Implemented

---> Open command

open <Host:Port>
 This command connects to Host that is listening on Port.

---> Update command
It PINGS all its neighbours.It updates the shared directory information of 
each.

---> Share command

share -i
 This displays the current shared directory.If no directory is shared it asks
to share a directory.
share <string>
 This shares absolute the directory mentioned as the argument.
share </directory>
This shares the current working directory appended to the argument mentioned
above if that directory exists

---> scan command
Scans the shared directory for files` information.

---> find <string>
 It looks for files containg words in the string.
Entering just find command without any argument shares all the files in the 
host`s shared directory.
   
---> list command 
Lists all the files returned by find command

---> clear <filenumber>
clear file whose number is file-no from the list. If no argument was given, clear all
files.

--->download <filenumber>
starts downloading the file specified.

--->monitor
displays the queries people are searching for

info c--> Displays Simpella Network Connections
info d--> Displays downloads that are in progress
info h--> Displays number of hosts, number of files they are sharing, and total size of those shared files
info n--> Displays Simpella statistics
info q--> Displays queries received and replies sent
info s--> Displays number and total size of shared files on this host
