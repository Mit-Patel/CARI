# DbHelper handles the database operations
#from DbHelper import DbHelper
import Rpi.GPIO as GPIO

class RaspberryPi:
    db = Dbhelper("iot.db")
    db.connect_to_db()
    
    def __init__(self):
        GPIO.setmode(GPIO.Board)
        GPIO.setwarnings(False)
        GPIO.setup(10,GPIO.IN)
        GPIO.setup(16,GPIO.OUT)
        GPIO.setup(18,GPIO.OUT)
        GPIO.setup(2,GPIO.OUT)
        GPIO.setup(12,GPIO.OUT)
           
    def changeState(self,pn,state):
        GPIO.output(pn,state)
        row = db.executeQuery("update appliances set status = " + state + " where pin = " + pn)
        if row.rowcount is not None:  # update success
            return 1
        else:  # update failed
            return 0
    
    @staticmethod
    def changeStateMotion(self,pn,state):
        GPIO.output(pn,state)
        row = db.executeQuery("update appliances set status = " + state + " where pin = " + pn)
        if row.rowcount is not None:  # update success
            return 1
        else:  # update failed
            return 0

    def destroy(self):
        GPIO.cleanup()
        