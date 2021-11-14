import socket
import time

def checkError(code, sock):
    if code == b'0':
        code = sock.recv(4)
        print (f"ERROR: {code}")
    else:
        return


def main():
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(("localhost",8823))

    sock.send(bytearray([0]))
    response = sock.recv(4)
    
    sock.send(bytearray([127,0,0,1]))
    response = sock.recv(4)
    checkError(response, sock)

    time.sleep(0.5)
    sock.send(bytearray([23,32]))
    response = sock.recv(4)
    checkError(response, sock)
    
    time.sleep(0.5)
    sock.send(bytearray([255,255]))
    response = sock.recv(4)
    checkError(response, sock)
    
    time.sleep(0.5)
    sock.send(bytearray([0]))
    response = sock.recv(4)
    if response == b'1':
        print ("Registrierung erfolgreich!")
    else:
        print ("Registrierung fehlgeschalgen!")
        print (f"Code: {sock.recv(4)}")
    

if __name__ == "__main__":
    main()
