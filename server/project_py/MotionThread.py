from threading import *
# DbHelper handles the database operations
from DbHelper import DbHelper
from RaspberryPi import RaspberryPi
import Rpi.GPIO as GPIO


    
class Motion(Thread):
    def __init__(self):
         self.db = DbHelper("iot.db")
         self.db.connect_to_db()
          
    def run(self):
        while(True):
            row = db.executeQuery("Select is_motion from house where id = 1")
            if row is not None:
                is_motion = row.fetchone()[0]
                if(is_motion == 1):
                    #GPIO.add_event_detect(SENSOR_PIN,GPIO.RISING,callback=motionDetect)
                    if(GPIO.input(10)):                                    
                        RaspberryPi.changeStateMotion(16,0)
                        RaspberryPi.changeStateMotion(18,0)
                        RaspberryPi.changeStateMotion(2,0)
        
                    else:
                        RaspberryPi.changeStateMotion(16,1)
                        RaspberryPi.changeStateMotion(18,1)
                        RaspberryPi.changeStateMotion(2,1)
        
            
    def motionDetect():
        rpi.changeState(16,1)
        rpi.changeState(18,1)
        rpi.changeState(2,1)
        
                
        