# DroidPPPwn
An android frontend that uses [PPPwn_cpp_android](https://github.com/deviato/PPPwn_cpp_android)

- It includes a GUI, PPPwn_cpp binary specifically compiled for arm-android and x86-android, plus stage1.bin + stage2.bin for all supported firmwares (7.00-11.00).
- For firmwares 9.00, 9.60, 10.00, 10.01, 11.00 `stage2.bin` is taken from [Sistr0](https://github.com/Sistr0/PPPwn) repository, i.e. you can load GoldHEN payload.
- For firmwares 7.0x, 7.5x, 8.0x, 8.5x, 9.0x, 10.50, 10.7x `stage2.bin` is taken from [EchoStretch](https://github.com/EchoStretch/ps4-hen-vtx/) `ps4-hen-vtx-pppwn`, i.e. you can load PS4HEN payload.
- You can use your own `stage2.bin` placing it to your external storage root folder through `adb push stage2.bin /sdcard/`

## Requirements
- An Android device with **`root access`**, minimum version Android KitKat 4.4
- An OTG USB Cable or Adapter
- An USB Ethernet Adapter
- An ethernet cable

Alternatively, if your device has an embedded ethernet port, you can use that, as in the case of Android TV boxes.

## Usage
- Download the [latest release](https://github.com/deviato/DroidPPPwn/releases) from this repository and install to your android phone.
- On your PS4: follow the instructions from the original [PPPwn](https://github.com/TheOfficialFloW/PPPwn/blob/master/README.md) to configure the ethernet connection.
- Start DroidPPPwn application and select your PS4 firmware.
- Press `Start` button on the app and simultaneously X on your controller when you're on the `Test Internet Connection` screen.
- Wait until the exploit reaches the stage4 and the message is printed on your monitor
- If exploit fails click `Start` button again to stop it, and repeat again the last step

Example run here: [https://youtu.be/SQT7AgTtrDY](https://youtu.be/SQT7AgTtrDY)

## About PPPwn binaries building and packaging 

### Some explanations about the binaries provided in this package.

In order to obtain execution permission I had to trick the Android Studio packaging system by including a fake `pppwn.jar` (which is a zip, you'll find in app/libs project folder) to extract all of the binaries in the default app private folder `/data/data/it.deviato.droidpppwn/lib/`. Right after installation you'll find 4 files extracted in that, they are fake libraries named `libXXX.so`, which in reality are zip archives too, each one containing a binary for a different architecture (plus the various `stage1` and `stage2` files).

Once you open the app for the first time, it recognizes your architecture and extracts the appropriate binary. The four files and related architectures are:

```
- armeabi-v7a/libpppwnkk.so	->	for 32bit armv7l, minimum sdk 19 (KitKat 4.4), libc shared build (cannot link static due to some ndk bugs)
- armeabi-v7a/libpppwn.so	->	for 32bit armv7l/armv8l, minimum sdk 21 (Lollipop 5.0), static build
- arm64-v8a/libpppwn.so		->	for 64bit armv8a+, minimum sdk 21, static build
- x86/libpppwn.so		->	for 32bit x86 minimum sdk 21, static build
- x86_64/libpppwn.so		->	for 64bit x86 minimum sdk 21, static build
```

All of the binaries are compiled via the official android NDK r25c, the latest that supports android 4.4.

If you don't like these versions or they don't work well for you, you can just replace the `pppwn` binary in the folder mentioned above with your favorite one.

**If you want to build it on your own**, I've forked the original xfangfang repository and modified its `CMakeLists.txt` to be able to compile for Android, both with NDK on your Linux machine or the Termux app directly on your device. The instructions are in the fork itself here [PPPwn_cpp_android](https://github.com/deviato/PPPwn_cpp_android).

## Known Bugs
- All previous bugs fixed

## Changelog
### 1.3.1
- Updated all stage2.bin files for ps4-hen-vtx payload to the latest version of EchoStretch (PPPwn-1.0309).
- Hen support has now been added for 7.0x firmwares too.
### 1.3
- Added a new pppwn binary build for Android x86_64 architectures.
- Added support to PS4HEN for all the remaining firmwares. Now only the 7.0x versions remain without payload support, but only with the basic PoC.
- Added two options to GUI to set optional parameters -nw (don't wait one more PADI before starting) and -rs (use CPU for more precise sleep time) for pppwn.
- Fixed wrong build for x86 32bit.
- Unified the installer with a single apk package for both standard and 64-bit-only systems.
### 1.2.3
- Recompiled all binaries updating them to the latest version of pppwn_cpp, which enables some previously non-working PS4-slim.
- Added support for Linux payload for the 11.00 firmware, through a checkbox that allows you to switch between the standard GoldHen stage2 and the LightningMods version.
I preferred to keep the standard payload loader and not incorporate ps4-linux payload into stage2.bin, to leave the choice of 2gb, 3gb, 4gb versions to you. If you want to replace the payload with your own, with preferred hardcoded ps4-linux loader, overwrite the /data/data/it.deviato.droidpppwn/lib/linux.1100 file with your own stage2.
### 1.2.2
- Updated stage2.bin files to latest version, now you have GoldHen also for 9.60.
- For the other systems, as of now, these are the included stage2.bin for each firmware:
```
- From 7.00 to 8.52     ->  PoC by EchoStretch
- 9.00                  ->  GoldHen by Sistr0
- 9.03 / 9.04           ->  LightningMods + ps4-hen-vtx payload by Sistr0 (NEEDS TESTING)
- 9.50 / 9.51 / 9.60    ->  GoldHen by Sistr0 (maybe only 9.60 working?)
- 10.00 / 10.01         ->  GoldHen by Sistr0
- 10.50 / 10.70 / 10.71 ->  LightningMods + ps4-hen-vtx payload by Sistr0 (NEEDS TESTING)
- 11.00                 ->  GoldHen by Sistr0
```
- As usual, you can always put your own stage1.bin and stage2.bin into the root folder of your internal or external storage (/storage/emulated/0 or whatever the symlink /sdcard refers to)
### 1.2.1
- Changed the method for recognizing the device architecture, which was giving wrong results in some older systems
- Added one more binary for 32bit `armv7`, now you have one for Android 4.4 built with shared libc, and one for `armv7l`/`armv8l` for Android 5.0+, static linked
- Recompiled all the other binaries with `real` static (there was an error in previous version)
- Some minor enhancements
### 1.2
- Added support for Android x86 and fixed 32bit arm-v7a and 64bit arm-v8a builds (no more bus_error)
- Recompiled all binaries using android NDK instead of Termux environment (cleaner result)
- Added the option to search and select the preferred network interface
- Fixed the issue of binaries not being installed on devices with older Android versions
### 1.1
- Added support for 32bit arm-v7a with separated binary of pppwn
- Refactored the whole project lowering minSdk to version 19, so now it can run on Android KitKat 4.4+
- Replaced stage2.bin for supported firmwares with those ones from Sistr0 repo to allow loading payloads
- Added the possibility to use your own stage2.bin
- Other small fixes
### 1.0
- Initial release
