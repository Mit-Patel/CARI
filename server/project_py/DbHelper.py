import sqlite3
import socket


class DbHelper:
    def __init__(self,database):
        self.database = database        

    def connect_to_db(self):
        self.conn = sqlite3.connect(self.database)        
        
    def executeQuery(self,query):
        result = self.conn.cursor().execute(query)
        self.conn.commit()
        return result

    def executeParameterQuery(self,query,value):
        result = self.conn.execute(query,value)
        return result
    
    def fetchAll(self,table):
        print('Fetching Data')
        query = "SELECT * FROM {}".format(table)
        result = self.conn.execute(query)
        return result

    def fetchField(self,column,table):
        query = "Select {} from {}".format(column,table)
        result = self.conn.execute(query)
        return result

    def destroy(self):
        self.conn.close()
            