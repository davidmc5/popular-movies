# Popular Movies

## Project Overview
Build an app to allow users to discover the most popular movies playing. 

## Why this Project

Building this app requires understanding of the foundational elements of programming for Android. This app communicates with the Internet and provides a responsive and delightful user experience.


## Main goals

- Fetch data from the Internet with the MovieDB API.
- Use adapters and custom list layouts to populate list views.
- Incorporate libraries to simplify the amount of code.


## Main implementation steps

- Build a UI layout for multiple Activities.
- Launch these Activities via Intent.
- Fetch data from The MovieDB API.

## Requirements to compile the app

- This app requires a key to access the API of The Movie Database (TMDb).
- Obtain your personal key from [The Movie DB](https://www.themoviedb.org/account/signup)
- The key is an alphanumeric string stored in the `keys.xml` file. Create this file in the `app/res/values/` directory, add the `<resources>` section stated below and replace `paste_your_key_here` with your alphanumeric key.

```
<resources>
    <string name="tmdb_key">"paste_your_key_here"</string>
</resources>
```


