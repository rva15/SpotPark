# SpotPark
An android app that helps users locate paid parking lots and street parking spots along with other utilities like remembering where 
the car was parked, setting alarms for parking expiration etc. SpotPark uses crowdsourcing to notify a user when some other user is vacating
a parking spot nearby. 

## Installation
Standalone repository that can be forked and run using AndroidStudio. Currently this version of SpotPark is compatible with android phone devices running Android 7.0 (Nougat) and earlier os. 

## Main Features
SpotPark has several utilities that makes the life of the average car user a lot easier. It has features to search for parking, reserve parking, remember where the car was parked, receive notifications before the parking expires. It also keeps track of where users had parked in the past, so they could navigate back to it at a later time. Users can voluntarily report free, legal parking spots they know of so that it benefits the community. <br/>
<img src="https://github.com/rva14/SpotPark/blob/master/logo.png" width="300">

### Search and reserve parking
SpotPark uses the ParkWhiz api to display paid parking lots in the United States and redirects users to the ParkWhiz website for booking them. In addition, it shows the free parking spots reported by SpotPark community. There is an in-app user feedback system to verify the authenticity of the reported spots. Most interestingly, it displays in how many minutes a SpotPark user nearby is going to vacate his parking spot. (This was done using Google's Activity Recognition API but Google placed several restrictions on location access starting with Android Oreo, leading to the withdrawal of this feature from the app). <br/>
<img src="https://github.com/rva14/SpotPark/blob/master/find.jpg" width="500">

### Check-in at your parking spot
Users can check-in once they park the car in order for them to navigate back to it later. They can set an alarm to remind them of parking expiration, and add notes that would help them remember where they had parked. <br/>
<img src="https://github.com/rva14/SpotPark/blob/master/remember.png" width="500">

SpotPark Logo and UI design credits : Namrata Babu https://www.behance.net/namratababu




