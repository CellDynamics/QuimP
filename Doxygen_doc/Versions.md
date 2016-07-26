# Versions

Since 2016 a new development cycle has been started. New QuimP is backward compatible with previous version of  _QuimP11_ but offering new functionalities (like support for user plugins, saving/loading state), fixed bugs and thoroughly reworked code.

The versioning scheme has been changed as well. To be more compatible with Java and Maven, the QuimP revision contains three numbers _MajorVersion_._MinorVersion_._IncrementalVersion_. Nevertheless, in order to correlate with old versioning style (_QuimP11_, _QuimP10_) we decided to set _MajorVersion_ and _MinorVersion_ to release year and month respectively. Thus, package named `QuimP-16.05.06.zip` contains QuimP software released on May 2016. 

The QuimP package contains several default plugins that are versioned separately from QuimP. 

# Past versions 

## QuimP11
Last public version from previous development cycle.
* [B] Fixed several minor bugs.
* [F] Updated and tested with ImageJ 1.49a and MATLAB 2014a
* [F] The ECMM and Q Analysis plugins will search for other .paQP in a directory and prompt to batch process files.
* [F] BOA prompts to check image scale.
* [F] BOA can read in a previous segmentation using the LOAD button
* [F] When editing segmentations the user can scroll between frames without leaving edit mode
