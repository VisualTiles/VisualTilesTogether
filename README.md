# Visual Tiles Together

**Visual Tiles Together** turns your mobile phone or tablet into a VJ platform to allow realtime visual performance.

A user can add "Visual Tiles" to the main tile canvas. Each visual tile can have a drawn graphic and can have effects assigned to them by the moderator. The app can then go into "Presenter Mode" and display the main tile canvas and its tiles and tile effects. The presenter will listen for any updates to tiles (swapping tiles with new tile images) and changes to those tiles' effects.

Another feature allows users at the same event to log in and create a visual tile, allowing them to draw and share a creation of their own for everyone at the event to see. The tile they create will be added to a queue from which the moderator user can select and add to the main visual canvas.


## User Stories

### **Required** functionality:

_Login and Creating/Joining Parties_

* [x] User can create a new party and own it as the "moderator"
* [x] User can join in on a party
* [x] Implement options for joining/signing in
  * [x] Sign in using Google sign in API

_Visual Canvas_

* [x] Device can be the presenter and remain in "Presentation mode," playing the main visual canvas
* [x] Moderator can edit the visual canvas
  * [x] Add or Swap a visual tile from the "upcoming" queue
* [x] Presenting device can connect and clone display to external screens and/or cast using Chromecast
  * Nexus 5 and 7 support MHL HDMI out.


_Creating a Visual Tile_

* [x] User can click on create tile FAB to start Create Visual Tile mode
  * [x] User can draw using touch
  * [x] User can submit tile to "upcoming" queue


_Selecting Visual Tiles_

* [x] Users can see a list of "Upcoming" Visual Tiles
* [x] Users can see the list of "Now Playing" Visual Tiles
* [x] Users can vote on tiles in the "Upcoming" and "Now Playing" tabs
* [x] Moderator can add or swap out a tile from "Upcoming" to "Now Playing" (i.e., add visual tile to main presenting canvas)
* [x] Moderator can delete any visual tile


_Visual Effects

* [x] Moderator can pick a visual effect from a list of available effects
* [x] Moderator can assign an effect to a tile on the presenter canvas


_Backend, Models, Persistence, Architecture_

* [x] Research and Design
* [x] Models
* [x] Create Firebase account


**Optional** functionality:

_Login and Creating/Joining Parties_
  * [ ] Select grid size of the main presentation canvas ("stage")
  * [ ] No approval, QR code, Single click, code

_Visual Effects and Timeline_

* [ ] Add timeline feature
  * [ ] Moderator can set interval of visual effect event to fire
* [ ] Moderator can pick period length (# of beats)
* [ ] Moderator can assign tempo (beats per minute)
* [ ] Show a visual timeline of events
  * [ ] Implement drag/drop editing functionality of events on the timeline


_Visual Tile Templates_

* [ ] Twitter feed template

_Creating Visual Tile_

* [ ] User can enter text
* [ ] User can subscribe to visual effects
* [ ] User can customize an effect: user define motion attributes: color(s) & color animation speed (length each color stays on, transition time to the next color, etc), and movement for each axis x y and z.  Movement parameters for each axis are speed, angle limits, and what happens at the end of the cycle (repeat, ping-pong, stop).  We could also animate the size.  After a user defines a shape and possibly its animation, they can send it to the stage.


_Chat_

* [ ] Implement chat functionality so users can chat in a chatroom for this event
* [ ] See a list of users
* [ ] Direct message


## Week 2: Video Walkthrough

[Link to video walkthrough](https://www.dropbox.com/s/2mvbfjr2k7ff7rv/VID_20161121_111206.mp4)

## Week 1: GIF Walkthrough

![video](http://i.imgur.com/jAObAah.gif)

## Week 1: Whiteboard session

![whiteboard photo 1]](https://i.imgur.com/rWd3aYx.jpg)
![whiteboard photo 2](https://i.imgur.com/U6yv3mG.jpg)

## Initial Wireframes

![screenshot](https://github.com/VisualTiles/VisualTilesTogether/blob/master/art/wf_01.jpg)

![screenshot](https://github.com/VisualTiles/VisualTilesTogether/blob/master/art/wf_02.jpg)

![screenshot](https://github.com/VisualTiles/VisualTilesTogether/blob/master/art/wf_03.jpg)

## License

    Copyright 2016 Chris Spack, George Cohn, Javier Arboleda
