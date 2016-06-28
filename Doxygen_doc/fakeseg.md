# Outline segmentation {#FakeSeg}

\author p.baniukiewicz
\date 28 Jun 2016
\tableofcontents

# About

QuimP supports converting black-white masks into Snakes making possible to use variety of external
segmentation algorithms.

# Segmentation details

For details see:

1. \ref uk.ac.warwick.wsbc.QuimP.geom.TrackOutline "TrackOutline" - converting objects to ROIs in one frame
2. \ref uk.ac.warwick.wsbc.QuimP.FakeSegmentation "FakeSegmentation" - grouping ROIs in chains related to \ref uk.ac.warwick.wsbc.QuimP.SnakeHandler "SnakeHandler"
3. \ref uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi "SegmentedShapeRoi" - holds extra information about ROi and converts it to list of points   