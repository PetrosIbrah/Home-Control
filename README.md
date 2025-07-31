## What is Home Control
Home Control is a Server-Client project that permits the user to control any or all Desktop devices in the same Network. The Server is a java Console project. The Client is an Android application project built in Android Studio with Java. 

## How to Run
In the 'Packages' folder you will find an .apk and a .jar file. 
  1. Download & Install the apk on your android device.
  2. Now send the jar file to any Devices you want to remotely manage in your network
  3. In every of these devices type, in the same directory as the jar file, the following:
    <pre>java -jar HomeControlServer.jar</pre>
  4. In the Home control android Application click on the pc Status tab and click on Add device
  5. For each device add a Label name the Mac Address and the device Ip.
  6. Click the check box next to the devices you want to manage, change to the commands tab and select any action.

## What it does. 
- Echo
  - Returns the device name and the operating system it is currently using
- Restart
  - Restarts the targeted devices
- Shutdown
  - Shut's down the targeted devices
- Restore
  - Shut's down the targeted devices
  - Wake's them after one minute
- Wake on Lan
  - Wakes all targeted devices using the WOL method

## Requirements
- An Android device
- A device on the same Network
  - Java 22
- In order for Wake on Lan and Restore to function properly the device is required to use Ethernet connection and have Wake on Lan capabilities enabled from Bios as well as Device Manager
