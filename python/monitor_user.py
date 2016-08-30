#!/usr/bin/env python
# -*- coding: utf-8 -*-

import slack
import pymysql
import time

def fetch_new_users(from_time):
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 charset='utf8',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    cursor.execute("SELECT id, addr, signup_time FROM users where signup_time > %s order by signup_time asc", (from_time))
    users = cursor.fetchall()
    
    cursor.close()
    connection.close()
    return users;

def check_new_user():
    last_signup_time = int(time.time())
    while True:
        buf = "";
        new_users = fetch_new_users(last_signup_time)
        for user in new_users:
            print(user['addr'])
            buf += '가입 %s %s' % (user['id'].encode('utf-8'), user['addr'].encode('utf-8'))
            last_signup_time = user['signup_time']
        slack.notify(buf)
        time.sleep(10)

check_new_user()
