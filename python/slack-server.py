# -*- coding: utf-8 -*-
from flask import Flask, request
import json
import requests
from datetime import date

app = Flask(__name__)

headers = {'Content-Type': 'application/json'}

def manPage():
    ret = ''
    ret += '--사용법--\n'
    ret += '조회 order_id\n'
    return ret

def queryOrder(order_id):
    ret = '하는중'
    succ = False
    succ = True
#    else:
#        ret = '입금실패' + str(response.status_code)
    return succ, ret

def handle(text):
    cmd = text.split(' ')
    if len(cmd) > 1:
        if cmd[0] == u'조회' and len(cmd) == 2:
            succ, ret = queryOrder(cmd[1])
        else:
            ret = manPage()
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
