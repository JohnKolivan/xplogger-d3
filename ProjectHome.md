This is a little program I've whipped up to help track my experience gain to try to find the best routes. I have put some work into cleaning it up to find the best exp routes for when 1.0.5 rolls around.

Source code is uploaded using SVN and is available to the public. This project was developed in Eclipse RCP version 4. It uses many Eclipse libraries, and a couple of 3rd party libraries (guava, tess4j, log4j, etc.)  At the moment it is currently bound to the Eclipse IDE.

3 requirements:
  1. you **_NEED_** Java 7 32-bit. java 6 does not provide hooks to get event notifications when a file is created on the OS
  1. you must install tesseract. grab it here http://code.google.com/p/tesseract-ocr/
  1. the minimum verticle screen resolution is 800 pixels. letterbox mode is supported, but may show the user an error due to the active portion of the screen being less than 800 pixels tall. (i.e. 1280x1024 letterboxed gives an actual resolution of 1280x720)

The core functionality works, but errors may exists.
Please leave feedback, questions, and comments.

How to use:
  1. start up the program
  1. select your screenshot directory, and hit start. This will start the process which watches for screenshots being created in your screenshot directory. (C:\Users\_USERNAME_\Documents\Diablo3\Screenshots is the screenshot directory)
  1. at the beginning of your run, take a screenshot with your mouse hovering over the exp bar. this will give a starting time and value for the run.
  1. take periodic screenshots with your mouse hovering over the exp bar.

Thats it, when the screenshots are created, they are analyzed and compared against your previous exp values and screenshot creation times. It should even continue to work if you level up during a run.

Alternatively, you may press the "Scan Existing" button to load all pre-existing images within the specified directory.

Each row in the table is considered a run. A new run automatically begins when it reads two screenshots with the exact same exp amounts, or you can simply click the new run button.

Example usage:

If you were running Alkaizer's run in act 3. You take a screenshot when you first start (Core of Arreat), then another at the start of each of the zones (damned, keeps 2, bridge, crater 2), the info for this run will be updated in the table as you take each screenshot. this will produce something similar to the image below.

The start button creates the watcher thread which simply waits for the screenshots to be created. When a screenshot is added to the directory, it notifies the program and the update is performed. The stop button kills the the watcher thread. The clear button simply removes all data from the table.

**NOTE: if you take a screenshot without your mouse over the exp bar, that screenshot will simply be ignored. the program tries to parse text in the center of the screen just above the exp bar.**

Explanation of the following screenshot:
  * each row is a run. this is a log of me doing alkaizer's run
  * each column represents a different zone. parsing the zone names from the top right doesn't work well since the background can contain artifacts which makes it difficult to use OCR
  * the values contained within the cell are experience and time spent
  * experience is measured in the millions. Likewise with exp / hr
  * the rows marked 'avg' and 'xp/hr' are the summed values for that specific zone across all runs. (i.e. in the first column, those two values represent the average exp gained, and experience gained per hour for the core of arreat)

**No memory reading is used in this program. This simply analyze's screenshots in a specific directory.**

![http://i.imgur.com/Re0IQ.png](http://i.imgur.com/Re0IQ.png)