# MemoriesApplication
Memories is a location based social media application where the focus is put on the events happening around where the user is.

<p align="center"><img src="https://raw.githubusercontent.com/PenguinDan/MemoriesApplication/master/app/src/main/res/drawable-hdpi/memorieslogo.png?token=AOfdzocliDdEArStdqpATQJdRDmxvnVlks5aSHkiwA%3D%3D" width="200" height="200" /></p>

This application uses Firebase to both authenticate users and store their information along with their media as necessary.  The Memories application takes advantage of Firebase’s Realtime Database to create the Trending Activity and the Latest Activity.  However, Firebase’s Realtime Database have some very limited querying capabilities and we are working to switch to Firestore to be able to create a wider range of activities.

Application Features Include:
* A login page where users can either choose to login through Google or through their own account.
* An account creation page where users can create a Memories account
* A Trending Page where the most popular media based on likes and the user's current location are presented
* A Latest Page where the latest events happening around the user are presented
* A User Page where the user can store their own backgrounds, profile image, and a list of medias they have stored
* A search feature where users can input a location and find a list of medias that have been uploaded from there
* The ability to use the phone's hardware to take pictures

Upcoming Features and Changes:
* Change to Firebase Firestore instead of Firebase Realtime Database to perform more powerful queries
* Addition of Facebook's login 
* Creation of application's own camera function to be able to take both images or videos through one activity
* A strong search activity from the ability to perform more complex queries
* Simple bug fix of User Page where their medias are being duplicated
