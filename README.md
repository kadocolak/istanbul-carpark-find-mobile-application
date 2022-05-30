# İstanbul parking lot find mobile application
## What does this app do?

It filters the private and public car parks close to your current location in Istanbul for you. It shows you proximity, capacity, price, location and traffic map. As a user, you can get directions to whichever destination you want to go.

## What technologies were used?

-> Google Maps API <br />
-> TomTom API <br /> 
-> Retrofit Library <br />
-> Volley Library <br />


## Database - where is the data source, how does it connect?

There is no local database or save location. Connection is established via web services.

Public parking lot web service in Istanbul Metropolitan Municipality;

https://api.ibb.gov.tr/ispark/park

Private parking lot web service in my personal web site;

https://kadircolak.com/proje/otopark/home/ozelotopark

## Want a membership? Is it paid to use?

No to all

## Which programming language was used?

Java 

## How can I use the mobile application?

First of all, you have to give location sharing permission in the application. Then, when you have an internet connection, open the mobile application, the map will automatically take your current location, while a virtual zone with a diameter of 5000 meters is created in the background, and those in this area are placed in a separate series in both private and public parking lots.

The distance, price and occupancy information of the public parking lots in this list are displayed by touching the marker object on the map. For private parking lots, only proximity data is shown in the same click scenario. For both directions - navigation process is shown to open Google Maps app.
