import socket

class Communication:

    def __init__(self):
        self.host = ""
        self.port = 4444

    def connect(self):
        self.socket = socket.socket()
        self.socket.bind((self.host, self.port))

    def receive_data(self):
        print('Connecting')
        self.socket.listen(5)

        self.conn, self.addr = self.socket.accept()
        print('Connected')
        print(self.addr)
        self.recv_msg = self.conn.recv(1024)
        print(self.recv_msg)
        self.recv_msg = str(self.recv_msg.decode())
        print('Message Received = ' + self.recv_msg)

        return self.recv_msg

    def send_data(self, data):
        print("Sending Data" + data)
        self.conn.send(data.encode('utf-8'))

    def close(self):
        self.socket.close()
