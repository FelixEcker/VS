import socket
import time

# This simple script tests the register-routine
# of the Server.

def checkError(code, sock):
    if code == b'0':
        code = sock.recv(4)
        print (f"ERROR: {code}")
    else:
        return


def main():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(("localhost",8823))

    # Tell the server that we want to register
    sock.send(bytearray([0]))
    response = sock.recv(4)

    # Send over the IP to register
    sock.send(bytearray([127,0,0,1]))
    response = sock.recv(4)
    checkError(response, sock)

    # These waits are mostly optional.
    # Here they are used for debugging
    # on the server side
    time.sleep(0.5) 
    sock.send(bytearray([23,32])) # Send the Port to register
    response = sock.recv(4)
    checkError(response, sock)
    
    time.sleep(0.5)
    sock.send(bytearray([255,255])) # Send the IP to save this registry under
    response = sock.recv(4)
    checkError(response, sock)
    
    time.sleep(0.5)
    sock.send(bytearray([0])) # Send a byte (of any value) to the server to finish the registration
    response = sock.recv(4)
    if response == b'1':
        print ("Registrierung erfolgreich!")
    else:
        print ("Registrierung fehlgeschalgen!")
        print (f"Code: {sock.recv(4)}")
    

if __name__ == "__main__":
    main()
