# -*- coding: utf-8 -*-
from flask import Flask, request
import json
import requests
import pymysql
from datetime import date

app = Flask(__name__)

headers = {'Content-Type': 'application/json'}

def manPage():
    ret = ''
    ret += '--사용법--\n'
    ret += '상품상태: 상품의 상태를 조회합니다.\n'
    ret += '상품상태변경 [salads|soups|beverages|others] id available : available이 0 이면 soldout\n'
    ret += '매진상태: 매진 상태를 조회합니다.\n'
    ret += '매진해제: 모든 매진상태를 해제합니다.\n'
    return ret

def queryOrder(order_id):
    ret = '하는중'
    succ = False
    succ = True
#    else:
#        ret = '입금실패' + str(response.status_code)
    return succ, ret

def queryStatus(where_query = None):
    succ = True
    ret = ''
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 charset='utf8',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    tables = ['salads', 'soups', 'beverages', 'others', 'salad_items']
    for table in tables:
        query = "SELECT item_id, name, available FROM %s" % table
        if where_query is not None:
            query += (' ' + where_query)
        cursor.execute(query)
        items = cursor.fetchall()
        for item in items:
            ret += ('[%s.%s] %s :%s\n' % (table, item['item_id'], item['name'].encode('utf-8'), item['available']))
    cursor.close()
    connection.close()
    if len(ret) == 0:
        ret = '항목이 없습니다.'
    return succ, ret;

def updateStatus(table, id, available):
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 charset='utf8',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    query = "update %s set available = %s where item_id = %s" % (table, available, id)
    print(query)
    cursor.execute(query)
    connection.commit()
    cursor.close()
    connection.close()
    return querySoldout()

def clearSoldout():
    connection = pymysql.connect(host = 'saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com',
                                 user = 'saladgram',
                                 passwd = 'saladgram',
                                 db = 'saladgram',
                                 charset='utf8',
                                 cursorclass = pymysql.cursors.DictCursor)  

    cursor = connection.cursor()
    tables = ['salads', 'soups', 'beverages', 'others', 'salad_items']
    for table in tables:
        query = "update %s set available = 1 where available = 0" % table
        cursor.execute(query)
    connection.commit()
    cursor.close()
    connection.close()
    return querySoldout()

def querySoldout():
    return queryStatus('where available = 0')

def handle(text):
    cmd = text.split(' ')
    if cmd[0] == u'조회' and len(cmd) == 2:
        succ, ret = queryOrder(cmd[1])
    elif cmd[0] == u'상품상태':
        succ, ret = queryStatus()
    elif cmd[0] == u'매진상태':
        succ, ret = querySoldout()
    elif cmd[0] == u'매진해제':
        succ, ret = clearSoldout()
    elif cmd[0] == u'상품상태변경' and len(cmd) == 4:
        succ, ret = updateStatus(cmd[1], cmd[2], cmd[3])
    else:
        ret = manPage()
    return ret

@app.route("/slack/", methods=['POST'])
def handle_slack_post():
    ret = ''
    text = request.form.get('text')
    ret = handle(text)
    return json.dumps({'text':ret})

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True, port=7272)
