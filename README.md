# MemoriesApplication
Memories is a location based social media application where the focus is put on the events happening around where the user is.

![alt text]( https://raw.githubusercontent.com/PenguinDan/MemoriesApplication/master/app/src/main/res/drawable-hdpi/memorieslogo.png?token=AOfdzqGg4jL7ZhdccoYWqLNuYHC7un28ks5aSHYHwA%3D%3D)

This application uses Firebase to both authenticate users and store their information along with their media as necessary.  The Memories application takes advantage of Firebase’s Realtime Database to create the Trending Activity and the Latest Activity.  However, Firebase’s Realtime Database have some very limited querying capabilities and we are working to switch to Firestore to be able to create a wider range of activities.
