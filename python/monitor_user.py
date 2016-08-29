#!/usr/bin/env python
# -*- coding: utf-8 -*-

import slack
import pymysql
import time

def fetch_users():
    min = 8
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 charset='utf8',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    cursor.execute("SELECT id, addr FROM users")
    users = cursor.fetchall()
    
    cursor.close()
    connection.close()
    return users;

def check_new_user():
    first = True;
    map_users = {};
    while True:
        current_users = fetch_users();
        if first:
            first = False;
            for user in current_users:
                map_users[user['id']] = user['addr'];
        else:
            new_users = []
            for user in current_users:
                if user['id'] not in map_users:
                    map_users[user['id']] = user['addr']
                    new_users.append(user)
            buf = "";
            for user in new_users:
                print(user['addr'])
                buf += '가입 %s %s' % (user['id'].encode('utf-8'), user['addr'].encode('utf-8'))
            slack.notify(buf)
            time.sleep(5 * 60)

check_new_user()
