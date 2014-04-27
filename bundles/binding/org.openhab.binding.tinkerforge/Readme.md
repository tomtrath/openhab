# Bugfixes
 ## 1.4.0
  * Missing updates of Items if a Tinkerforge Device is referenced in several Items
# Incompatible Changes
 ## 1.4.0
  * LCDBacklight is a sub device of LCD20x4 Bricklet (items file must be changed)
  * LCD20x4Button posts an update not a command anymore (rules must be changed)
  * IndustrialQuadRelay sub id numbering now starts from zero (items file must be changed)
# New Features
 ## 1.4.0
  * new Devices
    * Bricklet Industrial Quad Relay
    * Bricklet Industrial Digital In 4
    * Bricklet IO-16
    * support for serveral Item types
       * NumberItem
       * SwitchItem
       * ContactItem
  * handle disconnected brickds
     * on binding startup make retries every second
     * when binding is running use the Tinkerforge autoreconnect feature
 ## 1.5.0
  * new Devices
    * Remote Switch Bricklet
    * Motion Detection Bricklet
        * items file
        '''
        Contact motion      "motion [MAP(en.map):MOTION%s]" {tinkerforge="uid=m3W"}
        '''
        * sitemap file
        '''
        Text item=motion
        '''
        * en.map file
        '''
            MOTIONCLOSED=no motion
            MOTIONOPEN=montion detected
        '''
   * Bricklet Multi Touch
       * openhab.cfg
       ```
       tinkerforge:touch.uid=jUS
       tinkerforge:touch.type=bricklet_multitouch
       tinkerforge:touch.sensitivity=181
       tinkerforge:touch.recalibrate=true

       #tinkerforge:touche0.uid=jUS
       #tinkerforge:touche0.type=electrode
       #tinkerforge:touche0.subid=electrode1
       #tinkerforge:touche0.disableElectrode=true

       tinkerforge:touche1.uid=jUS
       tinkerforge:touche1.type=proximity
       tinkerforge:touche1.subid=proximity
       tinkerforge:touche1.disableElectrode=true
       ```
       * items file
       ```
        Contact electrode0      "electrode0 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode0"}
        Contact electrode1      "electrode1 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode1"}
        Contact electrode2      "electrode2 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode2"}
        Contact electrode3      "electrode3 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode3"}
        Contact electrode4      "electrode4 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode4"}
        Contact electrode5      "electrode5 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode5"}
        Contact electrode6      "electrode6 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode6"}
        Contact electrode7      "electrode7 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode7"}
        Contact electrode8      "electrode8 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode8"}
        Contact electrode9      "electrode9 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode9"}
        Contact electrode10     "electrode10 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode10"}
        Contact electrode11     "electrode11 [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=electrode11"}
        Contact proximity       "proximity [MAP(en.map):%s]" {tinkerforge="uid=jUS, subid=proximity"}
        ```
       * sitemap file
       ```
        Text item=electrode0
        Text item=electrode1
        Text item=electrode2
        Text item=electrode3
        Text item=electrode4
        Text item=electrode5
        Text item=electrode6
        Text item=electrode7
        Text item=electrode8
        Text item=electrode9
        Text item=electrode10
        Text item=electrode11
        Text item=proximity
       ```
# other changes
 ## 1.4.0
   * updated Tinkerforge API to 2.0.12
 ## 1.5.0
   * Tinkerforge API 2.1.0.2
