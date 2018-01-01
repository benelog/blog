#!/usr/bin/env python
# copied from https://github.com/jsakamoto/MarkdownPresenter/blob/master/start-presenter-for-unix.sh
import os
import webbrowser
import time
try:
  import SimpleHTTPServer
  import thread
except:
  import http.server as SimpleHTTPServer
  import _thread as thread

def launch():
  time.sleep(1)
  webbrowser.open("http://localhost:8000/")

thread.start_new_thread(launch, ())

# start mini HTTP daemon.

os.chdir("output")
SimpleHTTPServer.test(HandlerClass=SimpleHTTPServer.SimpleHTTPRequestHandler)

