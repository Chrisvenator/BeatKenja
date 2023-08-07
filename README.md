# BeatKenja

Beat Saber automapping tools:

1. timing creation
3. convert any map into a timings map:
    1. without stacks
    2. with stacks
5. create a really simple linear two handed pattern
6. make a map one-handed
8. current patterns:
   1. linear
   2. "complex" (based on the input pattern)
   3. jumps (small, normal, big)
   4. 1left-2right (= twice as many blue notes as red notes)
   5. doubles
   6. stacks (currently broken)

## Installation
1. Download .jar file drom Releases. You may put it into it's own folder. It will generate a few files
2. install install [pip](https://phoenixnap.com/kb/install-pip-windows#:~:text=Installing%20PIP%20On%20Windows%201%20Step%201%3A%20Download,Environment%20Variables%20...%205%20Step%205%3A%20Configuration%20) and dependencies (only needed for onsets):
    ```bash
     pip install librosa
     pip install ffmpeg or install it from: https://ffmpeg.org/download.html
     pip install pydub
    ```

3. Open BeatKenja.jar and let it create a few files and folders. Then close it again.

4. open config.txt and change the defaultPath to your WIP folder (or whatever folder you may like). **replace the backward slashes with forward ones!!!**
   verbose shouldn't be changed except if you want to get spammed by the program

5. Now you are ready to go

## How to use
<pre>Note that I will be using map and diff interchangeably</pre>
### Onset Creation
**You need to have all prerequisites installed!**
1. Put all your desired Songs into the folder OnsetGeneration/mp3Files. Note: All your files must be **mp3** files! (.wav files will probably work too).
2. Start BeatKenja and hit Convert "MP3s to timing maps". This will analyze the song and make a timings map. The output will be saved at OnsetGeneration/output/*SongName*.<br>
  The more songs that need to be converted, the longer it will take. You can always review the prgoress when you look into the OnsetGeneration/output folder.
3. Copy all the desired Maps folders from OnsetGeneration/output/ to your WIP folder.
You can now open your maps in the editor of your choice.
4. Verify the accuracy of the note placements. No algorithm is perfect. So the chance is VERY high that the algorithm placed some notes off beat.


If you now want to make a "real" map out of these timings, then have a look at the next chapter:

----

### Map Generation
Prepare a timings difficulty. A timings difficulty is a diff where every note is a dot note and is located at the bottom left corner. There **MUST ONLY BE BLUE NOTES!!** Or else the program may not work.<br>
Here is an [Example](https://beatsaver.com/maps/31d4b) for a timing map. (to be added)<br><br>
If you are unsure to what a timing diff is, then convert an existing diff to a 1 color timing diff and then load it into your editor of your choice.<br>
<pre>Map to timing notes --> To 1 color timing notes --> SAVE MAP</pre>


If the timings difficulty is prepared, then open BeatKenja.jar and choose it.<br>
When you click on Map creator you then have a few options:<br>
+ **Create Linear Map [(Example)]()**:<br>
  A really simple linear map. There should be no DDs or resets. It gets quite boringe quite fast. The swings will **always** be alternating.
  It is possible to make a one handed Linear Map: Map creator --> one han... (top left option)
+ **Create Complex Map [(Example)]()**:<br>
  A map which can contain quite interesting patterns but the swings will **always** be alternating.<br>
  It might conatin DDs or resets. But it will give a warning if it detects some.<br>
  It is possible to make a one handed Complex Map: Map creator --> complex (top right option)
+ **Create Map [(Example)]()**:
  This is where you can have a LOT of freedom.<br>
  There are the following options to what sections should be generated: 
  <pre>complex | linear | 1-2 | 2-1 | 2-2 | small, normal, big jumps | doubles | sequence (WIP)</pre>
  **MORE WILL BE ADDED HERE** once the features actually work
  
Then at last, hit SAVE MAP.

TODO: How to get diff into the editor
TODO: Explain what load patterns does
TODO: Explain diff caching

----

### Map Utilities
+ **Make diff into a no arrow diff**
+ **Convert all flashing lights**<br>
  This converts all flashing light events into regular on events.
+ **Delete Note Type**. Blue: 1 and Red: 0
+ **fix Placements**
  This moves all placements to (by default) 1/16 of a beat
----

## Known Issues:
+ **There are holes in the map:** No Algorithm is perfect. But I will fix that sometime in the future
+ **Map won't load in Beat Saber:** There may be something wrong with a difficulty file. Just go into the editor of your choice and open and save every difficulty. That should fix it.
+ **The map I generated doesn't show up in the editor:** TBD
+ **The map didn't change after generating a new one:** There may be 2 possibilities:
+   1. The Program didn't feel like generating something
+   2. Chromapper chached the difficulty. If this is the case, then exit and reopen the diff. that should fix it.

----

## TODOs:
- [ ] creating new pattern types:
    - [x] doubles
    - [x] (small, normal big) jumps
    - [ ] saving and loading patterns (or Squences as I called them because me dumb)
    - [ ] more variation
    - [ ] stacks
- [ ] Better music onsets
- [ ] Dark Mode
- [ ] Finish README.MD
