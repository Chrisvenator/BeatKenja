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

2. install librosa (only needed for onsets):
    ```bash
     pip install librosa
    ```

3. install aubio (only needed for onsets):
    ```bash
     pip install aubio
    ```

4. Open BeatKenja.jar and let it create a few files and folders. Then close it again.

5. open config.txt and change the defaultPath to your WIP folder (or whatever folder you may like)
   verbose shouldn't be changed except if you want to get spammed by the program

7. Now you are ready to go

## How to use
<pre>Note that I will be using map and diff interchangeably</pre>
### Onset Creation
1. create the onsets with and write it into timings.txt "aubioonset song.wav > timings.txt"
2. (more will be added here)

----

### Map Generation
Prepare a timings difficulty. A timings difficulty is a diff where every note is a dot note and is located at the bottom left corner. There **MUST ONLY BE BLUE NOTES!!** Or else the program may not work.<br>
Here is an [Example](https://beatsaver.com/maps/31d4b) for a timing map. (to be added)<br><br>
If you are unsure to what a timing diff is, then convert an existing diff to a 1 color timing diff and then load it into your editor of your choice.<br>
<pre>Map to timing notes --> To 1 color timing notes --> SAVE MAP</pre>


If the timings difficulty is prepared, then open BeatKenja.jar and choose it.<br>
When you click on Map creator you then have a few options:<br>
+ **(one handed) Create Linear Map [(Example)]()**:<br>
  A really simple linear map. There should be no DDs or resets. It gets quite boringe quite fast. The swings will **always** be alternating.
+ **(one handed) Create Complex Map [(Example)]()**:<br>
  A map which can contain quite interesting patterns but the swings will **always** be alternating.<br>
  It might conatin DDs or resets. But it will give a warning if it detects some.<br>
+ **Create Map [(Example)]()**:
  This is where you can have a LOT of freedom.<br>
  There are the following options to what sections should be generated: 
  <pre>complex | linear | 1-2 | 2-1 | 2-2 | small, normal, big jumps | doubles | sequence (WIP)</pre>
  **MORE WILL BE ADDED**
  
Then at last, hit SAVE MAP.

----

### Map Utilities
+ **Make diff into a no arrow diff**
+ **Convert all flashing lights**<br>
  This converts all flashing light events into regular on events.
+ **Delete Note Type**. Blue: 1 and Red: 0
+ **fix Placements**
  This moves all placements to (by default) 1/16 of a beat
  
## TODOs:
- [ ] creating new pattern types:
    - [x] doubles
    - [x] (small, normal big) jumps
    - [ ] saving and loading patterns (or Squences as I called them because me dumb)
    - [ ] more variation
    - [ ] stacks
- [ ] Better music onsets
- [ ] Dark Mode!!!
- [ ] Finish README.MD
