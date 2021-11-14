import socket
import time

def main():
    # Establish connection to the Server
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(("localhost",8823))

    # Tell the server that we want to request
    # some data
    sock.send(bytearray([1]))
    sock.recv(4)

    # Send the ID we want to have the data of
    sock.send(bytearray([255,255]))

    # Receive the data
    ip = bytearray(sock.recv(1024))
    port = int(sock.recv(1024))

    # Was there an error?
    if (sock.recv(1024) == b'0'):
        print (sock.recv(1024))
    else: # No? Print the data
        print ("IP:   "+str(ip))
        print ("PORT: "+str(port))


if __name__ == "__main__":
    main()
