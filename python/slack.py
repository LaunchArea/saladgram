#!/usr/bin/env python
# -*- coding: utf-8 -*-

import requests
import json

def notify(message):
    body = {'text':message}
    requests.post('https://hooks.slack.com/services/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', data=json.dumps(body))
