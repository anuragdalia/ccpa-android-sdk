# CCPA ANDROID SDK
##### CCPA Android Sdk (Unofficial and no guarantee)

Adds a link if the country of the user is unknown or california
```
if(user clicks links)
   if(user from california)
     if(okay with data selling)
        dismiss()
```

after link is clicked you can directly jump to second state by setting
```xml
<com.anurag.dalia.ccpa.ccpa_sdk.DNSMPI
        ...
        app:dnsmpi_two_states="false"
    />
```

you can also configure the texts shown in each state with the attributes listed below

add this to your root build.gradle
```gradle
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

and then add this dependency
```gradle
dependencies {
    ...
    implementation 'com.github.anuragdalia:ccpa-android-sdk:Tag'
  }
```

use this view where ever you wish to show the DNSMPI link (usually on the first screen thats shown to the user)
```xml
<com.anurag.dalia.ccpa.ccpa_sdk.DNSMPI
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
```

view configuration attributes and their formats are listed below

* dnsmpi_custom_layout : reference (layout reference.. only use if you know what you are doing)
* dnsmpi_link_color : color (color of the DNSPMI link)
* dnsmpi_two_states : boolean
* dnsmpi_state_a_text : string
* dnsmpi_state_b_text : string
* dnsmpi_ask_geo_permission : boolean

if you enable dnsmpi_ask_geo_permission
add this to the activity
```kotlin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        DNSMPI.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
```

At any point to know the preference just call
```kotlin
DNSMPI.canSellData(context) : Boolean
```

//PS ignore the fuck-all readme writing it at 4am and in a hurry :P
//PPS havent even check properly if its working perfectly fine

### Feel free to raise a PR



