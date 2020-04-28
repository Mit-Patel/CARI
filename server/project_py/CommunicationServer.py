import sqlite3
import socket
import time as t

# import Rpi.GPIO as GPIO
# import RaspberryPi

# Communication handles the client - server comunications
from Communication import Communication

# DbHelper handles the database operations
from DbHelper import DbHelper

#from RaspberryPi import RaspberryPi
from RaspberryPi import RaspberryPi

#
from MotionThread import Motion
def main():
    # Communication object
    communication = Communication()
    # connecting to the client
    communication.connect()
    # connecting with database
    db = DbHelper("iot.db")
    db.connect_to_db()
    # receiving data from client and processing the data
    while 1:
        motion = Motion()
        motion.start()
        # receive the message
        recv_msg = communication.receive_data()
        # calls the handleRequest function to handle the message
        send_msg = handle_request(db, recv_msg)
        print(send_msg)

        communication.send_data(send_msg)


# this function handles the message and performs the operation related to the message


def handle_request(db, recv_msg):
    # variable for response
    response = ""
    # split the message
    start = recv_msg.split(";")
    # distribute the splitted message into variables of opcode and data
    opcode = int(start[0])
    data = start[1:]

    # performing operation based on the value of opcode

    if opcode == 0:
        # login operation
        row = db.executeQuery(
            "select id,house_id,is_admin,fname,lname,username,password,email from users where username = '" + data[
                0] + "' and password = '" + data[1] + "'").fetchone()

        if row is None:  # login failed
            response = "1;"
        else:  # login success
            response = "0;" + str(row[0]) + ";" + str(row[1]) + ";" + str(row[2]) + ";" + str(row[3]) + ";" + str(
                row[4]) + ";" + str(row[5]) + ";" + str(row[6]) + ";" + str(row[7]) + ";"
        return response
    elif opcode == 1:
        # insert operation
        row = db.executeQuery(
            "insert into users(fname,lname,username,password,email,house_id,is_admin) values('" + data[0] + "','" +
            data[1] + "','" + data[2] + "','" + data[3] + "','" + data[4] + "'," + data[5] + ",0)")

        if row.lastrowid is not None:  # insert success
            response = "2;"
        else:  # insert failed
            response = "3;"
        return response
    elif opcode == 2:
        # get update data operation
        row = db.executeQuery("select fname,lname,username,password,email from users where id = " + data[0]).fetchone()

        if row is not None:  # data found
            response = "4;" + str(row[0]) + ";" + str(row[1]) + ";" + str(row[2]) + ";" + str(row[3]) + ";" + str(
                row[4]) + ";"
        else:  # data not found
            response = "-1;"
        return response
    elif opcode == 3:
        # get update data operation
        row = db.executeQuery(
            "update users set fname='" + data[1] + "',lname='" + data[2] + "',username='" + data[3] + "',password='" +
            data[4] + "',email='" + data[5] + "' where id = " + data[0])

        if row.rowcount is not None:  # update success
            response = "5;"
        else:  # update failed
            response = "6;"
        return response
    elif opcode == 4:
        # get all users data operation
        row = db.executeQuery(
            "select id, fname, lname from users where is_admin = 0 and house_id = " + data[0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"
            # set response
            response = "7;" + data_str
        else:  # no data found
            response = "8;"
        return response
    elif opcode == 5:
        # delete user operation
        row = db.executeQuery("delete from users where id = " + data[0])

        if row.rowcount != -1:  # deleted
            response = "9;"
        else:  # not deleted
            response = "10;"
        return response
    elif opcode == 6:
        # get rooms operation
        row = db.executeQuery(
            "select id,name,room_no,type from rooms where house_id = " + data[0]
            + " and id not in (select room_id from user_privileges where user_id = " + data[1]
            + " and is_whole_room = 1)").fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";" + str(x[3]) + ";"
            # set response
            response = "11;" + data_str
        else:  # no data found
            response = "12;"
        return response
    elif opcode == 7:
        # get appliance data operation
        row = db.executeQuery(
            "select id, name,appliance_no,type from appliances where type = '" + data[0]
            + "' and room_id = " + data[1]
            + " and id not in (select appliance_id from user_privileges where room_id = " + data[1]
            + " and user_id =" + data[2] + ")").fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"  + str(x[3]) + ";"
            # set response
            response = "13;" + data_str
        else:  # no data found
            response = "14;"
        return response
    elif opcode == 8:
        # all appliances data operation
        row = db.executeQuery(
            "select id, name from appliances where room_id = " + data[0]
            + " and id not in (select appliance_id from user_privileges where room_id = " + data[0]
            + " and user_id =" + data[1] + ")").fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";"
            # set response
            response = "15;" + data_str
        else:  # no data found
            response = "16;"
        return response
    elif opcode == 9:
        # insert privilege operation
        row = db.executeQuery(
            "insert into user_privileges(user_id,room_id,appliance_id,is_whole_room) values(" + data[0] + "," +
            data[1] + "," + data[2] + "," + data[3] + ")")

        if row.lastrowid is not None:  # insert success
            response = "17;"
        else:  # insert failed
            response = "18;"
        return response
    elif opcode == 10:
        # get the room denied to the specific user
        row = db.executeQuery(
            "Select id,name,room_no,type from rooms where id in " +
            "(Select distinct room_id from user_privileges where user_id = "
            + data[0] + ")"
        )

        if row.rowcount != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";" + str(x[3]) + ";"
            # set response
            response = "19;" + data_str
        else:  # select failure
            response = "20;"

        return response
    elif opcode == 11:
        # get the light denied for specific room to specific user
        row = db.executeQuery("Select appliance_id,is_whole_room from user_privileges where room_id ="
                              + data[0] + " and user_id = " + data[1])

        value = row.fetchone()

        if value is not None:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            # here the fetchone will return a tuple containing appliance_id and is_whole_room
            # so first extracting is_whole_room and the geeting its value as core type
            if str(value[1]) == "1":
                data = db.executeQuery(
                    "Select id,name,appliance_no from appliances where type = 'bulb' and room_id = " + data[0])
            else:
                data = db.executeQuery(
                    "Select id,name,appliance_no from appliances where type = 'bulb' and id IN" + "(Select appliance_id from user_privileges where room_id =" +
                    data[0] + " and user_id = " + data[1] + ")")
            for x in data:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"
            # set response
            response = "21;" + data_str
        else:  # insert failure
            response = "22;"

        return response
    elif opcode == 12:
        # get the fan denied for specific room to specific user
        row = db.executeQuery(
            "Select appliance_id,is_whole_room from user_privileges where room_id = " + data[0] + " and user_id = " +
            data[1])
        value = row.fetchone()

        if value is not None:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            if str(value[1]) == "1":
                data = db.executeQuery(
                    "Select id,name,appliance_no from appliances where type = 'fan' and  room_id = " + data[0])
            else:
                data = db.executeQuery(
                    "Select id,name,appliance_no from appliances where type = 'fan' and id in (Select appliance_id from user_privileges where room_id =" +
                    data[0] + " and user_id = " + data[1] + ")")
            for x in data:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"
            # set response
            response = "23;" + data_str
        else:  # insert failure
            response = "24;"

        return response
    elif opcode == 13:
        # revoke entire room which was denied
        row = db.executeQuery("Delete  from user_privileges where room_id = " + data[0] + " and user_id = " + data[1])

        if row.lastrowid is not None:  # delete success
            response = "25;"
        else:  # delete failed
            response = "26;"
        return response
    elif opcode == 14:
        # revoke specific appliance which was denied

        room_row = db.executeQuery(
            "Select appliance_id,is_whole_room from user_privileges where room_id = " + data[1] + " and user_id = " +
            data[2])
        value = room_row.fetchone()
        if value is not None:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            if str(value[1]) == "1":
                all_appliance = db.executeQuery("Select id from appliances where room_id = " + data[1]
                                                + " and id <>" + data[0]).fetchall()

                for x in all_appliance:
                    # insert remaining appliance
                    db.executeParameterQuery("Insert into user_privileges Values(null,?,?,?,0)",
                                             (data[2], data[1], str(x[0])))
                    # delete room_level privilege
                row = db.executeParameterQuery(
                    "Delete from user_privileges where user_id = ? and room_id = ? and appliance_id is null ",
                    (data[2], data[1]))
            else:
                row = db.executeQuery(
                    "Delete  from user_privileges where appliance_id = " + data[0] + " and user_id = " + data[2])

            if row.lastrowid is not None:  # delete appliance
                response = "27;"
            else:  # delete specific appliance
                response = "28;"
        else:
            response = "28;"
        return response
    elif opcode == 15:
        # add room
        row = db.executeQuery(
            "insert into rooms(name,room_no,house_id,type) values('" + data[0] + "','" + data[1] + "','" + data[2] + "','"+ data[3] +"')")

        if row.lastrowid is not None:  # insert success
            response = "29;"
        else:  # insert failed
            response = "30;"
        return response
    elif opcode == 16:
        # delete room
        row = db.executeQuery("delete from rooms where id = " + data[0] + " and house_id = " + data[1])

        if row.lastrowid is not None:  # delete success
            response = "31;"
        else:  # delete failed
            response = "32;"
        return response
    elif opcode == 17:
        # update room
        row = db.executeParameterQuery("update rooms set name = ?,room_no = ?, type= ? where id = ?",
                                       (data[0], data[1], data[2],data[3]))

        if row.lastrowid is not None:  # update success
            response = "33;"
        else:  # update failed
            response = "34;"
        return response
    elif opcode == 18:
        # appliance add
        
        pin = db.executeParamterQuery("Select pin from appliance_empty where room_id = ? and type = ?",(data[2],data[3]))
        
        if pin.lastrowid is not None:#Appliance can be added
            pin_num = pin.fetchone()[0]
            row = db.executeParameterQuery(
            "insert into appliances(name,appliance_no,room_id,type,house_id,pin) values(?,?,?,?,?,?)",
            (data[0], data[1], data[2], data[3], data[4],pin_num))

            if row.lastrowid is not None:  # insert success
                #delete the record from appliance_empty because it is now assigned
                db.executeParamterQuery("Delete from appliance_empty where room_id = ? and pin = ?",(data[2],pin_num))
                response = "35;"
            else:  # insert failed
                response = "36;"
        else:
            response = "36";
        return response
    elif opcode == 19:
        # appliance remove
        #data[0] = id
        #data[1] = house_id
        
        #get the pin of appliance
        pin = db.executeQuery("Select pin,type,room_id from appliances where id = " + data[0])
        row = pin.fetchone()
        pin_num = row[0]
        app_type = row[1]
        room_id = row[2]
        
        #insert into appliance_empty
        row = db.executeParamterQuery("insert into appliance_empty(room_id,pin,type) Values(?,?,?)",(room_id,pin_num,app_type))
        
        if row.lastrowid is not None:
            #insert success
            row = db.executeParameterQuery("delete from appliances where id = ? and house_id = ?", (data[0], data[1]))

            if row.lastrowid is not None:  # delete success
                response = "37;"
            else:  # delete failed
                response = "38;"
        else:
            response = "38;"
        return response
    elif opcode == 20:
        # appliance update
        
        #check whether the current room and new room has the same value
        
        room = db.executeParameterQuery("Select pin,type,room_id from appliances where id = ?",(data[4]))
        row = pin.fetchone()
        pin_num = row[0]
        app_type = row[1]
        room_id = row[2]
        if( room_id != data[2] ):
            #check if appliance can be added to room
            
            pin = db.executeParamterQuery("Select room_id,pin,type from appliance_empty where room_id = ? and type = ?",(data[2],data[3]))
            
            if pin.lastrowid is not None:#Appliance can be added
                
                pin_num = pin.fetchone()[0]
                row = db.executeParameterQuery("update appliances set name = ?,appliance_no=?,room_id = ?,type=?,pin = ? where id = ? and house_id = ?",
                (data[0], data[1], data[2], data[3], data[4], data[5],pin_num))

                if row.lastrowid is not None:  # update success
                    #delete the record from appliance_empty because it is now assigned
                    db.executeParamterQuery("Delete from appliance_empty where room_id = ? and pin = ?",(data[2],pin_num))
                    #insert the new record of the room because it is now empty
                    db.executeParamterQuery("insert into appliance_empty(room_id,pin,type) Values(?,?,?)",(room_id,pin_num,app_type))
                    response = "39;"
                else:  # update failed
                    response = "40;"
            else:  # update failed
                    response = "40;"
        else:  # update failed
                response = "40;"
        return response
    elif opcode == 21:
        # get All the rooms of specific house
        # get rooms operation
        row = db.executeQuery("select id,name,room_no from rooms where house_id = " + data[0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"
            # set response
            response = "41;" + data_str
        else:  # no data found
            response = "42;"
        return response
    elif opcode == 22:
        # get All the lights of this house
        row = db.executeQuery(
            "select id,appliance_no,name,room_id from appliances where type='bulb' and house_id = " + data[
                0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";" + str(x[3]) + ";"
            # set response
            response = "43;" + data_str
        else:  # no data found
            response = "44;"
        return response
    elif opcode == 23:
        # get All the fans of this house
        row = db.executeQuery(
            "select id,appliance_no,name,room_id from appliances where type='fan' and house_id = " + data[0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";" + str(x[3]) + ";"
            # set response
            response = "45;" + data_str
        else:  # no data found
            response = "46;"
        return response
    elif opcode == 24:
        # get all users data operation
        row = db.executeQuery(
            "select id, fname, lname from users where id in (select distinct user_id from user_privileges) and is_admin = 0 and house_id = " + data[0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                data_str += str(x[0]) + ";" + str(x[1]) + ";" + str(x[2]) + ";"
            # set response
            response = "47;" + data_str
        else:  # no data found
            response = "48;"
        return response
    elif opcode == 25:
        # bulb turn on
        row = db.executeQuery("select id,status,pin from appliances where id = " + data[0]).fetchall()

        if len(row) != 0:  # data found
            # prepare a ; separated string of all the data
            data_str = ""
            for x in row:
                if(int(x[1]) == 1):
                    rpi = Rasp()                                    
                    success = rpi.changeState(x[2],0)
                    rpi.destroy()
                    row = db.executeQuery("update appliances set status = 0 where id = " + data[0])

                    response = "49;0;"
                else:                    
                    rpi = Rasp()                                    
                    success = rpi.changeState(x[2],1)
                    rpi.destroy()
                    row = db.executeQuery("update appliances set status = 1 where id = " + data[0])

                    response = "49;1"
        else:  # no data found
            response = "50;"
        return response
    
    elif opcode == 26:
         # fan turn on or off
         '''data[0] = id
            data[1] = status
            data[2] = speed
         '''
         row = db.executeQuery("select id,status,pin,speed from appliances where id = " + data[0]).fetchall()
         if len(row) != 0:  # data found
             # prepare a ; separated string of all the data
             data_str = ""
             for x in row:
                 #turn off the fan
                 if(int(data[1]) == 1):
                     '''rpi = Rasp()                                    
                     success = rpi.changeState(x[2],0)
                     rpi.destroy()'''
                     row = db.executeQuery("update appliances set status = 0 where id = " + data[0])
                     response = "50;0;"
                
                 #turn on the fan in the specific speed
                 elif(int(data[1]) == 0):
                     rpi = Rasp()                                    
                     success = rpi.changeFanSpeed(x[2],20*int(data[2]))
                     rpi.destroy()
                     row = db.executeQuery("update appliances set status = 1 and speed = "+data[2]+"where id = " + data[0])

                     response = "50;1"
         else:  # no data four
             response = "50;"
         return response   
    elif opcode == 99:        
        response = "99;"
        return response


if __name__ == '__main__':
    main()
