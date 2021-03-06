#!/usr/bin/env python
# -*- coding: utf-8 -*-

import slack
import pymysql
import time

def check_5min():
    min = 8
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    now = int(round(time.time()))
    howlong = min * 60
    cursor.execute("SELECT order_id, order_type FROM orders where order_type <= 3 and status = 1 and reservation_time < %s + %s", (now, howlong))
    orders = cursor.fetchall()
    
    if len(orders) > 0:
        buf = """
            %d분 이내 예약인 배달/픽업 %d 건
        """ % (min, len(orders))

        buf += 'order_id : '
        for order in orders:
            if order['order_type'] == 1:
                buf += ('%d(픽업),' % (order['order_id']))
            elif order['order_type'] == 2:
                buf += ('%d(배달),' % (order['order_id']))
            else:
                buf += ('%d,' % (order['order_id']))
        slack.notify(buf)

    cursor.close()
    connection.close()

check_5min()
