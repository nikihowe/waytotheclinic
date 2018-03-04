# waytotheclinic

SECTION 0: INTRODUCTION

This code consists in several methods which help one to prepare maps for, and debug new functionality of,
the Android app called Way To The Clinic, which provides maps for the main building of Addenbrooke's Hospital
along with text and visual directions to all the main Clinics and Wards, as well as indoor location tracking.
The code for the main app can be found at https://github.com/team-papa/waytotheclinic.

If you are here, you are probably trying to do one of the following:

1) Modify the current maps used by the Android app (or add new ones),
2) Test new features before implementing them in the code for the Android app.

---------------------------------------------------------------------------------------

SECTION 1: HOW THE MAPS WORK

The word "map" refers to a Java representation of Vertices (corresponding
to rooms or intersections in Addenbrooke's Hospital) connected to each other by
Edges (which correspond to hallways, lifts, stairs, and short outdoor paths
at the Hospital). This document explains how to extract the map from a bitmap
image of the hospital floor. If you are trying to understand how to display maps
in the app, please see the documentation of the main app at the link given above.

This technique can be applied to any square png image, though we use a resolution
of 960x960 as it strikes a nice balance between being large enough to read text
but small enough that it's not hard to work with on a mid-power computer.

It is worth noting that unless one wants to modify the internal positioning (GPS) code,
one is advised, if making map updates, to do so in such a way that a square bitmap
of the new map overlaps with the maps we are currently using (titled LevelXMappingBitmap.png,
where X = level) - otherwise the app will not be able to correctly place one on the map
without modifying other code.

Given a 960x960 PNG (eg: Levels/Level2MappingBitmap.png), we open it in photo-editing
software (we used GIMP for these images). We ultimately want to turn it into
something the computer can read automatically (eg: Levels/Level2FinalCol.png).

To do this, we open a new trasparency layer and manually trace out the walkable regions
of the PNG image. We label nodes with colours as follows (the colours are represented
by a hexadecimal int, of the form 0xAARRGGBB where AA = alpha value (transparency),
RR = red value, GG = green value, BB = blue value):

0xFF808080 -> "grey"
0xFFFF00XX -> "red" (the XX can take on any value)
0xFF002AFF -> "blue"
0xFF0000FF -> "blue" (it is preferable to use this one)
0xFFFFFFXX -> "yellow"
0xFFCC00FF -> "pink"
0xFF295F29 -> "darkgreen"
0xFF00FFXX -> "green"
0xFF000000 -> "black"
0xFFFFFFFF -> "white"
0xFFFF9000 -> "orange"
0xFF00FFFF -> "lightblue"
0xFF7F007F -> "purple"

These different colours encode different things. In particular:

white     -> wall/out-of-bounds (cannot walk here - every other colour can be walked on)
black     -> path (can walk here, but there is nothing of interest here)
grey      -> node (can walk here, and there is something of interest (like a clinic))
red       -> lift
yellow    -> stairs
blue      -> toilet
darkgreen -> accessible toilet
orange    -> entrance to the Hospital
purple    -> cash machine
pink      -> food court / coffee / restaurant
green     -> a special colour of node used for connecting hallways which are on diagonals
lightblue -> (currently not used)

As seen by following Levels/Level2FinalCol.png as an example, on can use pixels of these
colours to note the locations we want to be able to navigate to/from, and also the
places we are allowed to walk in between them. Note that one cannot turn a corner on black,
so every corner or intersection must have a colour other than black (grey is the
default choice here).

An important note is that our software can only automatically connect vertices if they
are up, right, left, or down of one another. In particular, with the description provided
up to here, it would seem we have no way to connect diagonals automatically,
nor to we connect stairs or lifts across different floors. This is where the XX comes
into play in the green (diagonals), red (lifts), and yellow (stairs) nodes. Every green
node with XX = 01 will be connected with every other green node with XX = 01. Similarly
for XX = 02, etc. Similarly, every red node with XX = 01 will be connected with every
other red node with XX = 01, etc. Similar for the yellow nodes. In this way, we can
connect arbitrary nodes by setting them to the same colour value. We avoid creating
"transporting" lifts and stairs by using a different XX value for each stairwell/"liftwell"
(and only one XX value for each diagonal we are connecting).

Ok, so now we have a list of PNGs represeting the maps, and a list of PNGs representing the
"linemaps" of extracted information. Now we feed them into our system in a fashion
similar to that shown in the main method of our VertexFinder class.

The three important steps are:
- load vertices from the linemap (performed by loadVertices)
- load edges from the linemap (performed by loadEdges)
- load labels (we decide where from)

We have two options when loading labels. The first is to do it by hand. This is achieved by calling
loadLabels with the "loadLabelsFromFile" flag set to false. In this case, the user will
be prompted to enter one or more labels for each of the nodes. They will be provided with
a little picture of the area surrounding the node in question, with a little red cross
on the node itself.

The second option is to lode the nodes from a file. The file is called "Levels/labels.txt".
Take a look at the current labels.txt to see what the format is. Remember than the level
is zero-indexed (so 0 means level 1).

The last step is to save the file in a .ser format. Be careful that whatevery you're
reading the .ser file in with is in the same package as the one which you wrote
it with, beacuse otherwise it won't be able to deserialise it!

---------------------------------------------------------------------------------------

SECTION 2: FEATURE TESTING

The main app, which goes on a phone, is at the link mentioned in the introduction.
All the routing code in the app, however, comes from the MapSearch class here
(in the app it is called PathFinder). The two most important methods are
getPath and getTextDirections. getPath uses an A* search algorithm to find
the shortest path between two nodes (using a modified version of 3D
manhattan distance which also takes into account the cost of stairs/lift).
getTextDirections, given a path expressed as a list of Edges, returns
a string of text directions. This would be a clear area of improvement
as it is still somewhat rudimentary.

In general, the MapSearch class can be used to test things like routing
and text directions without having to wait for the app to load onto the phone.

---------------------------------------------------------------------------------------

Please write to nhrh2 AT cam DOT ac DOT uk with questions.