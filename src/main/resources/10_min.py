import requests
import time

def make_http_call():
    # Replace the URL with the desired endpoint you want to call
    url = "https://kaway-server-n3ahptldka-as.a.run.app/secList/NASDAQ"
    
    print("url is",url)

    # Make the HTTP request
    response = requests.get(url)

    # Process the response as needed
    if response.status_code == 200:
        print("HTTP call successful")
    else:
        print("HTTP call failed")

# Schedule the HTTP call to be made every 10 minutes
#while True:
#    print("making hhtp call now")
#    make_http_call()
#    time.sleep(600)  # Wait for 10 minutes
    
    
if __name__ == "__main__":    
    print('making hhtp call now 1')
    while True:
        print('making hhtp call now')
        make_http_call()
        time.sleep(600)  # Wait for 10 minutes
	
	
	
	
	
	
