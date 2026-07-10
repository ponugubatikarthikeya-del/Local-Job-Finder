#!/usr/bin/env python3
import mysql.connector
from mysql.connector import Error

try:
    connection = mysql.connector.connect(
        host='localhost',
        user='root',
        password='y24cs275'
    )
    
    cursor = connection.cursor()
    cursor.execute("DROP DATABASE IF EXISTS Local_job_finder")
    cursor.execute("CREATE DATABASE Local_job_finder")
    connection.commit()
    print("Database reset successful!")
    cursor.close()
    connection.close()
except Error as e:
    print(f"Error: {e}")
