# CEVRideShare
> An Uber-like ride share application desinged for connected electric vehicles 

## Features
- Vehicle matching with preference(cost, fast pickup, comfortability, and rating)
- Browse nearby vehicles and request matching
- Real-time trip progress update
- Single app for driver and passenger
- Driver and passenger rating 
- Customized Google Map 
- Animated vehicle marker with rotation
- Animated route preview polyline

## Feature Demos
- Flowing & fading polyline animation

    ![polyline_animation](media\polyline_animation.gif)

- Vehicle marker animation

    ![vehicle_animation](media\vehicle_animation.gif)

- Login

    ![login](media\login.png)

- Sign Up

    ![signup](media\signup.png)

- Trip planning with autocomplete address search 

    ![auto_complete_search](media\auto_complete_search.gif)

- Vehicle browse & match with cost preference

    ![trip_browse_match_demo](media\trip_browse_match_demo.gif)

- Driver side home screen 

    ![driver_home](media\driver_home.png)

- Trip in progress

    ![trip_progress](media\trip_progress.png)


## Getting started
- This project utilizes the [Secrets Gradle Plugin for Android](https://github.com/google/secrets-gradle-plugin) to hide the API key in the properties file.
Please place your API key in `local.properties` file like this:
    ```
    MAPS_API_KEY=YOUR_API_KEY
    ```

## Todos
- Driver & passenger chat
- Offline demo mode
- Integrate vehicle rental functionality

## Related projects
- [Lottie for Android](https://github.com/airbnb/lottie-android)
- [Maps SDK for Android Utility Library](https://github.com/googlemaps/android-maps-utils)
