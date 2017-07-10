import os
from flask import *
import win32print
import sys
import wmi
import json
import pythoncom

def get_printer_list():
    pythoncom.CoInitialize()
    c = wmi.WMI()
    online_printers = []
    offline_printers = []

    for p in c.Win32_Printer():
        if p.WorkOffline:
            offline_printers.append(p.caption)
        else:
            online_printers.append(p.caption)

    online_printers.remove('Send To OneNote 2016')
    online_printers.remove('Microsoft XPS Document Writer')
    online_printers.remove('Microsoft Print to PDF')
    online_printers.remove('Fax')

    response = "{\"Offline\": ", json.dumps(offline_printers), ",", "\"Online\":", json.dumps(online_printers), "}"

    return Response(response, status=200, mimetype='text/plain')

def get_job(printer_name):
    hprinter = win32print.OpenPrinter(printer_name)
    response = win32print.EnumJobs(hprinter, 0, 100)
    # print(response)
    json_arr = []
    for x in response:
        x['Submitted'] = 'Null'
        r = json.dumps(x)
        load_r = json.loads(r)
        json_arr.append(load_r)

    json_r = json.dumps(json_arr)
    print(json_r)
    return Response(json_r, status=200, mimetype='text/plain')

def print_document(filename, printer_name):
    print(printer_name)
    # printer_name = win32print.GetDefaultPrinter()
    if sys.version_info >= (3,):
        fp = open("./static/UPLOADFOLDER/"+filename, "rb")
        raw_data = bytes ("This is a test", "utf-8")
    else:
        fp = open("./static/UPLOADFOLDER/"+filename, "r")
        raw_data = "This is a test"

    data = fp.read()
    print(printer_name)

    hPrinter = win32print.OpenPrinter(printer_name)
    try:
        hJob = win32print.StartDocPrinter(hPrinter, 1, (filename, None, "RAW"))
        try:
            jobInfo = win32print.GetJob(hPrinter, hJob, 1)
            win32print.SetJob(hPrinter, hJob, 1, jobInfo, win32print.JOB_CONTROL_SENT_TO_PRINTER)
            win32print.StartPagePrinter(hPrinter)
            win32print.WritePrinter(hPrinter, data)
            win32print.EndPagePrinter(hPrinter)
        finally:
            win32print.EndDocPrinter(hPrinter)
    finally:
        win32print.ClosePrinter(hPrinter)

    # os.startfile("H:/Projects/College/LanPrint/static/UPLOADFOLDER/"+filename, "print")
    return Response("Printing Document", status=200, mimetype='text/plain')

def printer_details():
    printers = win32print.GetPrinterW()
    print(printers)