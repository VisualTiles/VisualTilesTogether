# Remaining tasks:

## Login activity
  * Try to quickly skip if user is logged in already
    * _Idea: use some  user state  stored in shared prefs?_
  * If in an indeterminate state show a spinner instead of login button

## Join/create group
  * Automatically fetch short code on group creation

## Main activity
  * Channel stats (some options protected)
    * share (short) link
    * users joined in channel
    * leave channel
    * delete channel
    * __stretch:__ moderator list & add moderator by email (if in channel?)
  * Basic chat room

## Moderator mode
  * Show button in main activity when a user is a moderator.
  * Two pane view.
  * Bottom/right pane is stage preview.

### Left/top pane:
    * Tab list controls which to show (or a drop down)
    * [if(landscape ? Horizontal : Vertical)
    * __Fragment:__ List of 'now playing' tiles
      * Tile is draggable onto the stage.
      * Long press to open alternate options (auto play? delete?)
      * __optional:__ On screen tiles have a green circle in corner of tile.
    * __Fragment:__ Check to select text color(s).
      * Tiles have a border color.
      * __stretch:__ More drawing effects.
    * __Fragment:__ Switch effect from list of animations.
      * Animation speed
      * Effect parameters
      * __stretch__: Tap button for rate.
    * __Fragment:__ Switch layout from list of layouts.
      * Mini preview of each layout. (Tile spaces are white blocks).
      * Clicking will limit tiles to N tiles in new layout.
      * __stretch:__ Make customizable (replace stage preview with layout editor).

### Stage pane:
    * Click tiles to remove a tile, and swaps with a random unused one.

## Present mode
  * Powered by data model (layout table; layout ID in channel table)
  * Channel has an active animation, consume it
  * Pre-cache all tile images so they immediately show when ready
  * If in 'auto-moderate' mode, use time-driven "RNG" for deterministic shuffling.
    * Time between a step
    * \# of tiles to shuffle per [# of steps]

## Drawing mode
  * Add eraser
  * Add stroke width
  * Fix clear button
  * __stretch:__ Export as [path list, paint] to serializable object.
    * Use object in firebase storage.
  * __stretch:__ Use two colors (primary/secondary)
  * __stretch:__ Allow text
  * __stretch:__ Fix circle drawing to be ellipsis from topleft-bottomright

## Animation engine
  * See whiteboard
