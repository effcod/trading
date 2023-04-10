from flask import Flask
from configparser import ConfigParser
from ibc.client import InteractiveBrokersClient

app = Flask(__name__)
ibc_client = InteractiveBrokersClient

#https://github.com/areed1192/interactive-brokers-api

@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'

def init():
    # Initialize the Parser.
    config = ConfigParser()

    # Read the file.
    config.read('config/config.ini')

    account_number = config.get('interactive_brokers_paper', 'paper_account')
    account_password = config.get('interactive_brokers_paper', 'paper_password')

    return InteractiveBrokersClient(
        account_number=account_number,
        password=account_password
    )

if __name__ == '__main__':
    ibc_client = init()
    app.run()
