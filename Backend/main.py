import os
from flask import *
import printer
from werkzeug.utils import secure_filename
import wmi

UPLOAD_FOLDER = './static/UPLOADFOLDER'
# printer.get_job("Brother DCP-T300")

# TOPIC_DICT = Content()
app = Flask(__name__)
app._static_folder = "static"
printer_name = ""
@app.route('/')

@app.route('/upload/', methods=["POST"])
def upload():
    global printer_name
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
    # print(request.form['printer_name'])
    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            print(request.files)
            d = Response("Error",status=201,mimetype='application/json')
            return d
        file = request.files['file']
        # printer_name = request.form['printer_name']
        # if user does not select file, browser also
        # submit a empty part without filename
        if file.filename == '':
            d = Response("No filename",status=201,mimetype='application/json')
            return d
        if file:
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return printer.print_document(filename, printer_name)

@app.route('/set_printer/', methods=["POST"])
def set_printer():
    global printer_name
    printer_name = request.form['printer_name']
    return Response("Printer Set", status=200, mimetype='text/plain')

@app.route('/printers/', methods=["GET"])
def printers():
    if request.method == 'GET':
        return printer.get_printer_list()

@app.route('/jobs/', methods=["POST"])
def jobs():
    printer_name = request.form['printer_name']
    return printer.get_job(printer_name)

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000, debug=True)
    #app.run()


