import requests
import time

def make_http_call():
    # Replace the URL with the desired endpoint you want to call
    url = "https://kaway-server-n3ahptldka-as.a.run.app/secList/NASDAQ"
    
    print("url is",url,flush=True)

    # Make the HTTP request
    response = requests.get(url)

    # Process the response as needed
    if response.status_code == 200:
        print("HTTP call successful", flush=True)
        print("Response data:", response.text[:100],flush=True)
    else:
        print("HTTP call failed", flush=True)

# Schedule the HTTP call to be made every 10 minutes
#while True:
#    print("making hhtp call now")
#    make_http_call()
#    time.sleep(600)  # Wait for 10 minutes
    
    
if __name__ == "__main__":    
    while True:
        print('making hhtp call now',flush=True)
        print()
        print()
        print()
        make_http_call()
        time.sleep(600)  # Wait for 10 minutes
	
	
	
	
	
	
